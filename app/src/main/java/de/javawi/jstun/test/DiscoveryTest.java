/*
 * This file is part of JSTUN. 
 * 
 * Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * This software is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in this distribution.
 */

package de.javawi.jstun.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.util.Log;
import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.attribute.MessageAttributeException;
import de.javawi.jstun.attribute.MessageAttributeParsingException;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.header.MessageHeaderParsingException;
import de.javawi.jstun.util.UtilityException;

public class DiscoveryTest {

	public static final String LOG = "STUN_LOG";
	public static final String LOG_TAG = "JSTUN";

	InetAddress iaddress;
	String stunServer;
	int port;
	int timeoutInitValue = 300; // ms
	MappedAddress ma = null;
	ChangedAddress ca = null;
	boolean nodeNatted = true;
	DatagramSocket socketTest1 = null;
	DiscoveryInfo di = null;

	public DiscoveryTest(final InetAddress iaddress, final String stunServer, final int port) {
		super();
		this.iaddress = iaddress;
		this.stunServer = stunServer;
		this.port = port;
	}

	public DiscoveryInfo test() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException,
			MessageAttributeException, MessageHeaderParsingException {
		ma = null;
		ca = null;
		nodeNatted = true;
		socketTest1 = null;
		di = new DiscoveryInfo(iaddress);

		if (firstTest()) {
			if (secondTest()) {
				if (firstTestRedo()) {
					thridTest();
				}
			}
		}

		socketTest1.close();

		return di;
	}

	/**
	 * @param log
	 *            log message.
	 */
	private void sendBroadCast(final String log) {
		Log.d(LOG_TAG, log);
	}

	private boolean firstTest() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException,
			MessageHeaderParsingException {
		int timeSinceFirstTransmission = 0;
		int timeout = timeoutInitValue;
		while (true) {
			try {
				// Test 1 including response
				socketTest1 = new DatagramSocket(new InetSocketAddress(iaddress, 0));
				socketTest1.setReuseAddress(true);
				socketTest1.connect(InetAddress.getByName(stunServer), port);
				socketTest1.setSoTimeout(timeout);

				final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();

				final ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);

				final byte[] data = sendMH.getBytes();
				final DatagramPacket send = new DatagramPacket(data, data.length);
				socketTest1.send(send);
				sendBroadCast("Test 1: Binding Request sent.");

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					socketTest1.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}

				ma = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
				ca = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
				final ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					di.setError(ec.getResponseCode(), ec.getReason());
					sendBroadCast("Message header contains an Errorcode message attribute.");
					return false;
				}
				if (ma == null || ca == null) {
					di.setError(700,
							"The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");
					sendBroadCast("Response does not contain a Mapped Address or Changed Address message attribute.");
					return false;
				} else {
					di.setPublicIP(ma.getAddress().getInetAddress());
					if (ma.getPort() == socketTest1.getLocalPort() && ma.getAddress().getInetAddress().equals(socketTest1.getLocalAddress())) {
						sendBroadCast("Node is not natted.");
						nodeNatted = false;
					} else {
						sendBroadCast("Node is natted.");
					}
					return true;
				}
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < 7900) {
					sendBroadCast("Test 1: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > 1600) {
						timeoutAddValue = 1600;
					}
					timeout = timeoutAddValue;
				} else {
					// node is not capable of udp communication
					sendBroadCast("Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					di.setBlockedUDP();
					sendBroadCast("Node is not capable of UDP communication.");
					return false;
				}
			}
		}
	}

	private boolean secondTest() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException,
			MessageAttributeException, MessageHeaderParsingException {
		int timeSinceFirstTransmission = 0;
		int timeout = timeoutInitValue;
		while (true) {
			try {
				// Test 2 including response
				final DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));
				sendSocket.connect(InetAddress.getByName(stunServer), port);
				sendSocket.setSoTimeout(timeout);

				final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();

				final ChangeRequest changeRequest = new ChangeRequest();
				changeRequest.setChangeIP();
				changeRequest.setChangePort();
				sendMH.addMessageAttribute(changeRequest);

				final byte[] data = sendMH.getBytes();
				final DatagramPacket send = new DatagramPacket(data, data.length);
				sendSocket.send(send);
				sendBroadCast("Test 2: Binding Request sent.");

				final int localPort = sendSocket.getLocalPort();
				final InetAddress localAddress = sendSocket.getLocalAddress();

				sendSocket.close();

				final DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
				receiveSocket.connect(ca.getAddress().getInetAddress(), ca.getPort());
				receiveSocket.setSoTimeout(timeout);

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					receiveSocket.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}
				final ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					di.setError(ec.getResponseCode(), ec.getReason());
					sendBroadCast("Message header contains an Errorcode message attribute.");
					return false;
				}
				if (!nodeNatted) {
					di.setOpenAccess();
					sendBroadCast("Node has open access to the Internet (or, at least the node is behind a full-cone NAT without translation).");
				} else {
					di.setFullCone();
					sendBroadCast("Node is behind a full-cone NAT.");
				}
				return false;
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < 7900) {
					sendBroadCast("Test 2: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > 1600) {
						timeoutAddValue = 1600;
					}
					timeout = timeoutAddValue;
				} else {
					sendBroadCast("Test 2: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					if (!nodeNatted) {
						di.setSymmetricUDPFirewall();
						sendBroadCast("Node is behind a symmetric UDP firewall.");
						return false;
					} else {
						// not is natted
						// redo test 1 with address and port as offered in the
						// changed-address message attribute
						return true;
					}
				}
			}
		}
	}

	private boolean firstTestRedo() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException,
			MessageHeaderParsingException {
		int timeSinceFirstTransmission = 0;
		int timeout = timeoutInitValue;
		while (true) {
			// redo test 1 with address and port as offered in the
			// changed-address message attribute
			try {
				// Test 1 with changed port and address values
				socketTest1.connect(ca.getAddress().getInetAddress(), ca.getPort());
				socketTest1.setSoTimeout(timeout);

				final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();

				final ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);

				final byte[] data = sendMH.getBytes();
				final DatagramPacket send = new DatagramPacket(data, data.length);
				socketTest1.send(send);
				sendBroadCast("Test 1 redo with changed address: Binding Request sent.");

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					socketTest1.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}
				final MappedAddress ma2 = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
				final ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					di.setError(ec.getResponseCode(), ec.getReason());
					sendBroadCast("Message header contains an Errorcode message attribute.");
					return false;
				}
				if (ma2 == null) {
					di.setError(700, "The server is sending an incomplete response (Mapped Address message attribute is missing). The client should not retry.");
					sendBroadCast("Response does not contain a Mapped Address message attribute.");
					return false;
				} else {
					if (ma.getPort() != ma2.getPort() || !ma.getAddress().getInetAddress().equals(ma2.getAddress().getInetAddress())) {
						di.setSymmetric();
						Log.d(LOG_TAG, "Node is behind a symmetric NAT.");
						return false;
					}
				}
				return true;
			} catch (final SocketTimeoutException ste2) {
				if (timeSinceFirstTransmission < 7900) {
					sendBroadCast("Test 1 redo with changed address: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > 1600) {
						timeoutAddValue = 1600;
					}
					timeout = timeoutAddValue;
				} else {
					sendBroadCast("Test 1 redo with changed address: Socket timeout while receiving the response.  Maximum retry limit exceed. Give up.");
					return false;
				}
			}
		}
	}

	private void thridTest() throws UtilityException, SocketException, UnknownHostException, IOException, MessageAttributeParsingException,
			MessageAttributeException, MessageHeaderParsingException {
		int timeSinceFirstTransmission = 0;
		int timeout = timeoutInitValue;
		while (true) {
			try {
				// Test 3 including response
				final DatagramSocket sendSocket = new DatagramSocket(new InetSocketAddress(iaddress, 0));
				sendSocket.connect(InetAddress.getByName(stunServer), port);
				sendSocket.setSoTimeout(timeout);

				final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();

				final ChangeRequest changeRequest = new ChangeRequest();
				changeRequest.setChangePort();
				sendMH.addMessageAttribute(changeRequest);

				final byte[] data = sendMH.getBytes();
				final DatagramPacket send = new DatagramPacket(data, data.length);
				sendSocket.send(send);
				sendBroadCast("Test 3: Binding Request sent.");

				final int localPort = sendSocket.getLocalPort();
				final InetAddress localAddress = sendSocket.getLocalAddress();

				sendSocket.close();

				final DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
				receiveSocket.connect(InetAddress.getByName(stunServer), ca.getPort());
				receiveSocket.setSoTimeout(timeout);

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[200], 200);
					receiveSocket.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
				}
				final ErrorCode ec = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (ec != null) {
					di.setError(ec.getResponseCode(), ec.getReason());
					sendBroadCast("Message header contains an Errorcode message attribute.");
					return;
				}
				if (nodeNatted) {
					di.setRestrictedCone();
					sendBroadCast("Node is behind a restricted NAT.");
					return;
				}
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < 7900) {
					sendBroadCast("Test 3: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > 1600) {
						timeoutAddValue = 1600;
					}
					timeout = timeoutAddValue;
				} else {
					sendBroadCast("Test 3: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					di.setPortRestrictedCone();
					sendBroadCast("Node is behind a port restricted NAT.");
					return;
				}
			}
		}
	}
}
