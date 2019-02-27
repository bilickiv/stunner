package hu.uszeged.inf.wlab.stunner.service.handler;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.utils.dtos.DiscoveryDTO;
import hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders.DiscoveryInfoBuilder;
import hu.uszeged.inf.wlab.stunner.utils.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
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
import de.javawi.jstun.test.DiscoveryInfo;
import de.javawi.jstun.util.UtilityException;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryExitStatus;

import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * {@link Handler} implementation to receive messages from the runner thread.
 * 
 * @author szelezsant
 * 
 */
public final class DiscoveryThreadHandler extends Handler {
    public static final String TAG = "DiscoveryThreadHandler";
	/** Initial timeout value in milliseconds. */
	private static final int INIT_TIMEOUT_VALUE = 300;
	/** Constant log message shown when the test fails for some reason. */
	private static final String TEST_FAILED = "Failed to execute test!";
	/** Constant log message shown when the response header contains error. */
	private static final String MESSAGE_HEADER_ERROR = "Message header contains an Errorcode message attribute.";
	/** The size of the buffer used to receive messages. */
	private static final int BUFFER_SIZE = 200;
	/** Error code used when the received response is incomplete. */
	private static final int ERROR_INCOMPLETE_RESPONSE = 700;
	/** The maximal increment of timeout in milliseconds. */
	private static final int MAX_TIMEOUT_INCREMENT = 1600;

	/** The result receiver to send back the results to the caller (UI). */
	private final ResultReceiver receiver;

	/** The {@link InetAddress} object containing the STUN server address. */
	private InetAddress iaddress;
	/** The URL address of the STUN server. */
	private String stunServer;
	/** The port of the STUN server. */
	private int port;

	/** The {@link MappedAddress} object to track address changes. */
	private MappedAddress mappedAddress = null;
	/** The {@link ChangedAddress} object to track address changes. */
	private ChangedAddress changedAddress = null;
	/** Flag to indicate if the device is from behind of a NAT. */
	private boolean nodeNatted = true;
	/** The socket to establish communication with server. */
	private DatagramSocket socketTest1 = null;
	/** The info object to store the results. */
	private DiscoveryInfo discoInfo = null;
	/** The dto which stores the gathered information. */
	private DiscoveryDTO discoveryDTO;
	/** The {@link Context} instance to gain access to system resources. */
	private final Context context;
	/** The give up limit of each test. */
	private final int giveUpLimit;
    /** The PARSE Object that contains all the logs */
    private ParseObject measurement;

	/**
	 * Constructor which allows to send messages via the receiver object.
	 * 
	 * @param looper
	 *            - the looper
	 * @param receiver
	 *            - the receiver
	 * @param context
	 *            - the context
	 */
	public DiscoveryThreadHandler(final Looper looper, final ResultReceiver receiver, final Context context) {
		super(looper);
		this.receiver = receiver;
		this.context = context;
		giveUpLimit = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PREF_KEY_TIMEOUT, Constants.DEFAULT_TIMEOUT);
		Log.i(getClass().getSimpleName(), String.format("timeout value = %d", giveUpLimit));
        //this.measurement = measurement;
	}

	public DiscoveryThreadHandler(Looper looper, final Context context) {
		super(looper);
		this.receiver = null;
		this.context = context;
		giveUpLimit = PreferenceManager.getDefaultSharedPreferences(context).getInt(Constants.PREF_KEY_TIMEOUT, Constants.DEFAULT_TIMEOUT);
		Log.i(getClass().getSimpleName(), String.format("timeout value = %d", giveUpLimit));
		//this.measurement = measurement;
	}

	@Override
	public void handleMessage(final Message msg) {
		final Bundle arguments = (Bundle) msg.obj;
		final long startId = arguments.getLong(Constants.KEY_START_ID);
		Log.d(getClass().getSimpleName(), "handleMessage - startId=" + startId);

		stunServer = arguments.getString(Constants.KEY_SERVER_ADDRESS);
		port = arguments.getInt(Constants.KEY_SERVER_PORT);
		discoveryDTO = arguments.getParcelable(Constants.KEY_DATA);

        //measurement.put("stunIpPort", stunServer + ":" + port);
        //measurement.put("stunPort", port);

		Log.d(TAG, startId+" randomized STUN server address: " + stunServer + ":" + port);

		try {
			Log.d(TAG, startId+" starting STUN test...");
			iaddress = InetAddress.getByName(discoveryDTO.getLocalIP());
			runTest();
			if (null == discoInfo) {
				sendResult(Constants.RESULT_STUN_ERROR, null);
				discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.ERROR);
			} else {
				final DiscoveryInfoBuilder infoBuilder = new DiscoveryInfoBuilder(discoInfo, context);
				// just invoked to set nat type
				infoBuilder.getInfo();
				discoveryDTO.getNatResultsDTO().setDiscoveryResultCode(infoBuilder.getDiscoveryResultCode());
				discoveryDTO.getNatResultsDTO().setPublicIP(null == discoInfo.getPublicIP() ? context.getString(R.string.n_a) : discoInfo.getPublicIP().getHostAddress());
				discoveryDTO.getNatResultsDTO().setSTUNserver(stunServer + ":" + port);
				discoveryDTO.getNatResultsDTO().setLastDiscovery(System.currentTimeMillis());
				discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.END_SUCCESSFUL);
				Log.d(TAG, startId+" STUN test has ended, result=" + discoveryDTO.getNatResultsDTO().getDiscoveryResult());
			}
		} catch (final SocketException socketException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.SOCKET_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, socketException);
		} catch (final UnknownHostException unknownHostException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.UNKNOWN_HOST_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, unknownHostException);
		} catch (final MessageAttributeParsingException messageAttributeParsingException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.MESSAGE_ATTRIBUTE_PARSING_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, messageAttributeParsingException);
		} catch (final MessageHeaderParsingException messageHeaderParsingException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.MESSAGE_HEADER_PARSING_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, messageHeaderParsingException);
		} catch (final UtilityException utilityException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.UTILITY_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, utilityException);
		} catch (final IOException ioException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.IO_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, ioException);
		} catch (final MessageAttributeException messageAttributeException) {
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.MESSAGE_ATTRIBUTE_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, messageAttributeException);
		} catch (final NullPointerException nullPointerException){
			discoveryDTO.getNatResultsDTO().setExitStatus(NatDiscoveryExitStatus.NULL_POINTER_EXCEPTION);
			Log.e(getClass().getSimpleName(), TEST_FAILED, nullPointerException);
		}
		if (context instanceof TestFinishedListener) {
			final Bundle resultBundle = new Bundle();
			resultBundle.putLong(Constants.KEY_START_ID, startId);
			resultBundle.putParcelable(Constants.KEY_DATA, discoveryDTO);
			resultBundle.putInt(Constants.KEY_ID,arguments.getInt(Constants.KEY_ID));
			if (null != receiver) {
				resultBundle.putParcelable(Constants.KEY_RECEIVER, receiver);
			}
			((TestFinishedListener) context).onTestFinished(resultBundle);
		}
	}

	/**
	 * Starts the test.
	 * 
	 * @throws UtilityException
	 *             when the test fails for some reason.
	 * @throws IOException
	 *             when there is problem in communication.
	 * @throws MessageAttributeException
	 *             when there is an error in the response.
	 * @throws MessageHeaderParsingException
	 *             when there is an error while parsing message header.
	 */
	private void runTest() throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException, NullPointerException {
		mappedAddress = null;
		changedAddress = null;
		nodeNatted = true;
		socketTest1 = null;
		discoInfo = new DiscoveryInfo(iaddress);
		Log.d(TAG,"start of runTest");
		if (firstTest(measurement) && secondTest(measurement) && firstTestRedo(measurement)) {
			thridTest(measurement);
		}
		if(measurement != null) {
			String outString = "";
			for (String key : measurement.keySet()) {
				outString += key + " " + measurement.getString(key) + " ";
			}
			Log.d(TAG, outString);
		}
		//measurement.saveInBackground();
        /*measurement.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateLog("elmentettem...");
            }
        });*/
        Log.d(TAG,"end of runTest");
        socketTest1.close();
	}

	/**
	 * Starts the first test for UDP availability.
	 * 
	 * @return true if UDP is enabled, false otherwise.
	 * @throws UtilityException
	 *             when the test fails for some reason.
	 * @throws IOException
	 *             when there is problem in communication.
	 * @throws MessageAttributeException
	 *             when there is an error in the response.
	 * @throws MessageHeaderParsingException
	 *             when there is an error while parsing message header.
	 */
	private boolean firstTest(ParseObject pObj) throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException, NullPointerException {
		int timeSinceFirstTransmission = 0;
		int timeout = INIT_TIMEOUT_VALUE;
        JSONArray ja = new JSONArray();
		while (true) {
            JSONObject jo = new JSONObject();
            JSONObject jo3 = new JSONObject();
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

                /*try {
                    jo.put("type", "sent");
                    jo.put("timestamp", (new Date()).getTime());
                    jo.put("id", sendMH.getTransactionID());
                    ja.put(jo);
                } catch(Exception ex) {
                }*/

				updateLog("Test 1: Binding Request sent.");

                JSONObject jo2 = new JSONObject();

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
					socketTest1.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
                    try {
                        jo2.put("type", "received");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", receiveMH.getTransactionID());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
				}

				mappedAddress = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
				changedAddress = (ChangedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
				final ErrorCode errorCode = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);

                //pObj.put("t1_mappedAddress", mappedAddress.getAddress().toString());
                //pObj.put("t1_changedAddress", changedAddress.getAddress().toString());
                //pObj.put("t1_noOfSending", count);

				if (errorCode != null) {
					discoInfo.setError(errorCode.getResponseCode(), errorCode.getReason());
					updateLog(MESSAGE_HEADER_ERROR);
                    try {
                        jo2.put("type", "error");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", errorCode.getResponseCode());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }

					return false;
				}

				if (mappedAddress == null || changedAddress == null) {
					discoInfo
							.setError(ERROR_INCOMPLETE_RESPONSE,
									"The server is sending an incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");
					updateLog("Response does not contain a Mapped Address or Changed Address message attribute.");
                    try {
                        try {
                            jo2.put("type", "error");
                            jo2.put("timestamp", (new Date()).getTime());
                            jo2.put("id", ERROR_INCOMPLETE_RESPONSE);
                            ja.put(jo2);
                        } catch(Exception ex) {
                        }
                        measurement.put("firstTest", ja);
                    } catch(Exception ex) {
                    }
					return false;
				} else {
					discoInfo.setPublicIP(mappedAddress.getAddress().getInetAddress());
					if (mappedAddress.getPort() == socketTest1.getLocalPort()
							&& mappedAddress.getAddress().getInetAddress().equals(socketTest1.getLocalAddress())) {
						updateLog("Node is not natted.");
						nodeNatted = false;
					} else {
						updateLog("Node is natted.");
					}
					updateLog("Source address is: " + InetAddress.getByName(stunServer).getHostAddress());
					updateLog("Mapped address is: " + mappedAddress.getAddress().toString());
					updateLog("Changed address is: " + changedAddress.getAddress().toString());
                    try {
                        measurement.put("firstTest", ja);
                    } catch(Exception ex) {
                    }
					return true;
				}
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < giveUpLimit) {
					updateLog("Test 1: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > MAX_TIMEOUT_INCREMENT) {
						timeoutAddValue = MAX_TIMEOUT_INCREMENT;
					}
					timeout = timeoutAddValue;
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                    } catch(Exception ex) {
                    }
				} else {
					// node is not capable of udp communication
					updateLog("Test 1: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					discoInfo.setBlockedUDP();
					updateLog("Node is not capable of UDP communication.");
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                    } catch(Exception ex) {
                    }
					return false;
				}
			}
		}
	}

	/**
	 * Starts the second test to check NAT presence.
	 * 
	 * @return true if the node is natted, false otherwise.
	 * @throws UtilityException
	 *             when the test fails for some reason.
	 * @throws IOException
	 *             when there is problem in communication.
	 * @throws MessageAttributeException
	 *             when there is an error in the response.
	 * @throws MessageHeaderParsingException
	 *             when there is an error while parsing message header.
	 */
	private boolean secondTest(ParseObject pObj) throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException, NullPointerException {
		int timeSinceFirstTransmission = 0;
		int timeout = INIT_TIMEOUT_VALUE;
        JSONArray ja = new JSONArray();

		while (true) {
            JSONObject jo = new JSONObject();
            JSONObject jo3 = new JSONObject();
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

                try {
                    jo.put("type", "sent");
                    jo.put("timestamp", (new Date()).getTime());
                    jo.put("id", sendMH.getTransactionID());
                    ja.put(jo);
                } catch(Exception ex) {
                }

				updateLog("Test 2: Binding Request sent.");

				final int localPort = sendSocket.getLocalPort();
				final InetAddress localAddress = sendSocket.getLocalAddress();

                //pObj.put("t2_localPort", localPort);
                //pObj.put("t2_localAddress", localAddress.getHostAddress());
                //pObj.put("t2_noOfSending", count);

				sendSocket.close();

				final DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
				receiveSocket.connect(changedAddress.getAddress().getInetAddress(), changedAddress.getPort());
				receiveSocket.setSoTimeout(timeout);

                JSONObject jo2 = new JSONObject();

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
					receiveSocket.receive(receive);
					receiveSocket.close();
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
                    try {
                        jo2.put("type", "received");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", receiveMH.getTransactionID());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
				}
				final ErrorCode errorCode = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (errorCode != null) {
					discoInfo.setError(errorCode.getResponseCode(), errorCode.getReason());
					updateLog(MESSAGE_HEADER_ERROR);
                    try {
                        jo2.put("type", "error");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", errorCode.getResponseCode());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
					return false;
				}
				if (nodeNatted) {
					discoInfo.setFullCone();
					updateLog("Node is behind a full-cone NAT.");
				} else {
					discoInfo.setOpenAccess();
					updateLog("Node has open access to the Internet (or, at least the node is behind a full-cone NAT without translation).");
				}
                try {
                    measurement.put("secondTest", ja);
                } catch(Exception ex) {
                }
				return false;
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < giveUpLimit) {
					updateLog("Test 2: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > MAX_TIMEOUT_INCREMENT) {
						timeoutAddValue = MAX_TIMEOUT_INCREMENT;
					}
					timeout = timeoutAddValue;
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                    } catch(Exception ex) {
                    }
				} else {
					updateLog("Test 2: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					if (nodeNatted) {
						// not is natted
						// redo test 1 with address and port as offered in
						// the changed-address message attribute
                        try {
                            jo3.put("type", "timeout");
                            jo3.put("timestamp", (new Date()).getTime());
                            ja.put(jo3);
                            measurement.put("secondTest", ja);
                        } catch(Exception ex) {
                        }
						return true;
					} else {
						discoInfo.setSymmetricUDPFirewall();
						updateLog("Node is behind a symmetric UDP firewall.");
                        try {
                            jo3.put("type", "timeout");
                            jo3.put("timestamp", (new Date()).getTime());
                            ja.put(jo3);
                            measurement.put("secondTest", ja);
                        } catch(Exception ex) {
                        }
						return false;
					}
				}
			}
		}
	}

	/**
	 * Starts the test to test NAT type.
	 * 
	 * @return true if the node is restricted, false otherwise.
	 * @throws UtilityException
	 *             when the test fails for some reason.
	 * @throws IOException
	 *             when there is problem in communication.
	 * @throws MessageAttributeException
	 *             when there is an error in the response.
	 * @throws MessageHeaderParsingException
	 *             when there is an error while parsing message header.
	 */
	private boolean firstTestRedo(ParseObject pObj) throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException, NullPointerException {
		int timeSinceFirstTransmission = 0;
		int timeout = INIT_TIMEOUT_VALUE;
        JSONArray ja = new JSONArray();
		while (true) {
            JSONObject jo = new JSONObject();
            JSONObject jo3 = new JSONObject();
			// redo test 1 with address and port as offered in the
			// changed-address message attribute
			try {
				// Test 1 with changed port and address values
				socketTest1.connect(changedAddress.getAddress().getInetAddress(), changedAddress.getPort());
				socketTest1.setSoTimeout(timeout);

				final MessageHeader sendMH = new MessageHeader(MessageHeader.MessageHeaderType.BindingRequest);
				sendMH.generateTransactionID();

				final ChangeRequest changeRequest = new ChangeRequest();
				sendMH.addMessageAttribute(changeRequest);

				final byte[] data = sendMH.getBytes();
				final DatagramPacket send = new DatagramPacket(data, data.length);
				socketTest1.send(send);
				updateLog("Test 1 redo with changed address: Binding Request sent.");

                try {
                    jo.put("type", "sent");
                    jo.put("timestamp", (new Date()).getTime());
                    jo.put("id", sendMH.getTransactionID());
                    ja.put(jo);
                } catch(Exception ex) {
                }

                JSONObject jo2 = new JSONObject();
				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
					socketTest1.receive(receive);
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
                    try {
                        jo2.put("type", "received");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", receiveMH.getTransactionID());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
				}
				final MappedAddress ma2 = (MappedAddress) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);

                //pObj.put("t1r_mappedAddress", ma2.getAddress().toString());
                //pObj.put("t1r_noOfSending", count);

				final ErrorCode errorCode = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				boolean returnValue = true;
				if (errorCode != null) {
					discoInfo.setError(errorCode.getResponseCode(), errorCode.getReason());
					updateLog(MESSAGE_HEADER_ERROR);
					returnValue = false;
                    try {
                        jo2.put("type", "error");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", errorCode.getResponseCode());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
				}
				if (returnValue && ma2 == null) {
					discoInfo.setError(ERROR_INCOMPLETE_RESPONSE,
							"The server is sending an incomplete response (Mapped Address message attribute is missing). The client should not retry.");
					updateLog("Response does not contain a Mapped Address message attribute.");
					returnValue = false;
                    try {
                        jo2.put("type", "error");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", errorCode.getResponseCode());
                        ja.put(jo2);
                    } catch (Exception ex) {
                    }
				} else {
					if (returnValue
							&& (mappedAddress.getPort() != ma2.getPort() || !mappedAddress.getAddress().getInetAddress()
									.equals(ma2.getAddress().getInetAddress()))) {
						discoInfo.setSymmetric();
						updateLog("Node is behind a symmetric NAT.");
						returnValue = false;
					}
				}
                try {
                    measurement.put("firstTestRedo", ja);
                } catch(Exception ex) {
                }
				return returnValue;
			} catch (final SocketTimeoutException ste2) {
				if (timeSinceFirstTransmission < giveUpLimit) {
					updateLog("Test 1 redo with changed address: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > MAX_TIMEOUT_INCREMENT) {
						timeoutAddValue = MAX_TIMEOUT_INCREMENT;
					}
					timeout = timeoutAddValue;
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                    } catch(Exception ex) {
                    }
				} else {
					updateLog("Test 1 redo with changed address: Socket timeout while receiving the response.  Maximum retry limit exceed. Give up.");
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                        measurement.put("firstTestRedo", ja);
                    } catch(Exception ex) {
                    }
					return false;
				}
			}
		}
	}

	/**
	 * Starts the test to determine NAT restrictions.
	 * 
	 * @throws UtilityException
	 *             when the test fails for some reason.
	 * @throws IOException
	 *             when there is problem in communication.
	 * @throws MessageAttributeException
	 *             when there is an error in the response.
	 * @throws MessageHeaderParsingException
	 *             when there is an error while parsing message header.
	 */
	private void thridTest(ParseObject pObj) throws UtilityException, IOException, MessageAttributeException, MessageHeaderParsingException, NullPointerException {
		int timeSinceFirstTransmission = 0;
		int timeout = INIT_TIMEOUT_VALUE;
        JSONArray ja = new JSONArray();
		while (true) {
            JSONObject jo = new JSONObject();
            JSONObject jo3 = new JSONObject();
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

                try {
                    jo.put("type", "sent");
                    jo.put("timestamp", (new Date()).getTime());
                    jo.put("id", sendMH.getTransactionID());
                    ja.put(jo);
                } catch(Exception ex) {
                }

                JSONObject jo2 = new JSONObject();

				updateLog("Test 3: Binding Request sent.");

				final int localPort = sendSocket.getLocalPort();
				final InetAddress localAddress = sendSocket.getLocalAddress();

                //pObj.put("t3_localPort", localPort);
                //pObj.put("t3_localAddress", localAddress.getHostAddress());
                //pObj.put("t3_noOfSending", count);

				sendSocket.close();

				final DatagramSocket receiveSocket = new DatagramSocket(localPort, localAddress);
				receiveSocket.connect(InetAddress.getByName(stunServer), changedAddress.getPort());
				receiveSocket.setSoTimeout(timeout);

				MessageHeader receiveMH = new MessageHeader();
				while (!receiveMH.equalTransactionID(sendMH)) {
					final DatagramPacket receive = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
					receiveSocket.receive(receive);
					receiveSocket.close();
					receiveMH = MessageHeader.parseHeader(receive.getData());
					receiveMH.parseAttributes(receive.getData());
                    try {
                        jo2.put("type", "received");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", receiveMH.getTransactionID());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
				}
				final ErrorCode errorCode = (ErrorCode) receiveMH.getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
				if (errorCode != null) {
					discoInfo.setError(errorCode.getResponseCode(), errorCode.getReason());
					updateLog(MESSAGE_HEADER_ERROR);
                    try {
                        jo2.put("type", "error");
                        jo2.put("timestamp", (new Date()).getTime());
                        jo2.put("id", errorCode.getResponseCode());
                        ja.put(jo2);
                    } catch(Exception ex) {
                    }
					return;
				}
				if (nodeNatted) {
					discoInfo.setRestrictedCone();
					updateLog("Node is behind a restricted NAT.");
                    try {
                        measurement.put("thirdTest", ja);
                    } catch(Exception ex) {
                    }
					return;
				}
			} catch (final SocketTimeoutException ste) {
				if (timeSinceFirstTransmission < giveUpLimit) {
					updateLog("Test 3: Socket timeout while receiving the response.");
					timeSinceFirstTransmission += timeout;
					int timeoutAddValue = timeSinceFirstTransmission * 2;
					if (timeoutAddValue > MAX_TIMEOUT_INCREMENT) {
						timeoutAddValue = MAX_TIMEOUT_INCREMENT;
					}
					timeout = timeoutAddValue;
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                    } catch(Exception ex) {
                    }
				} else {
					updateLog("Test 3: Socket timeout while receiving the response. Maximum retry limit exceed. Give up.");
					discoInfo.setPortRestrictedCone();
					updateLog("Node is behind a port restricted NAT.");
                    try {
                        jo3.put("type", "timeout");
                        jo3.put("timestamp", (new Date()).getTime());
                        ja.put(jo3);
                        measurement.put("thirdTest", ja);
                    } catch(Exception ex) {
                    }
					return;
				}
			}
		}
	}

	/**
	 * Sends message via the receiver - if initialized.
	 * 
	 * @param resultCode
	 *            - the result code
	 * @param messageBundle
	 *            - the bundle object containing additional data.
	 */
	private void sendResult(final int resultCode, final Bundle messageBundle) {
		if (null != receiver) {
			receiver.send(resultCode, messageBundle);
		}
	}

	/**
	 * Updates the log on the UI.
	 * 
	 * @param message
	 *            - the message to be displayed
	 */
	private void updateLog(final String message) {
		final Bundle messageBundle = new Bundle();
		messageBundle.putString(Constants.KEY_DATA, message);
		sendResult(Constants.RESULT_STUN_LOG, messageBundle);
		//Log.d(TAG,"updateLog "+discoveryDTO.getRecordID());
	}

	/**
	 * Interface to signal the service that the test has finished.
	 * 
	 * @author szelezsant
	 * 
	 */
	public interface TestFinishedListener {
		/**
		 * Invoked when the test finished.
		 * 
		 * @param results
		 *            - {@link Bundle} object which contains the discoveryDTO,
		 *            containing the results, and may contain the resultReceiver
		 *            to deliver the results to the caller activity. The bundle
		 *            also has to contain the startId of the process under the
		 *            key of {@link Constants#KEY_START_ID}.
		 */
		public void onTestFinished(final Bundle results);
	}
}
