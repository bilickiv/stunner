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

package de.javawi.jstun.test.demo.ice;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.javawi.jstun.util.Address;
import de.javawi.jstun.util.UtilityException;

@SuppressWarnings("PMD")
public class Candidate implements Comparable<Object> {
	// The ieft-mmusic-ice-12 draft is not non-ambigious about the number of types.
	// Chapter 5.1 defines 3 and 4 types on page 16 and page 17, respectively.
	public enum CandidateType {
		Local, ServerReflexive, PeerReflexive, Relayed
	};

	private final DatagramSocket socket;
	private final CandidateType type;
	private short componentId;
	private int priority;
	private int foundationId;
	private Candidate base;
	private boolean isInUse;

	public Candidate(final Address address, final short componentId) throws SocketException, UnknownHostException, UtilityException {
		this.socket = new DatagramSocket(0, address.getInetAddress());
		this.type = CandidateType.Local;
		this.componentId = componentId;
		this.priority = 0;
		this.base = this;
		this.isInUse = false;
	}

	public Candidate(final Address address, final CandidateType type, final short componentId, final Candidate base)
			throws SocketException, UnknownHostException, UtilityException {
		this.socket = new DatagramSocket(0, address.getInetAddress());
		this.type = type;
		setComponentId(componentId);
		this.priority = 0;
		this.base = base;
		this.isInUse = false;
	}

	public void setBase(final Candidate base) {
		this.base = base;
	}

	public Candidate getBase() {
		return base;
	}

	public CandidateType getCandidateType() {
		return type;
	}

	public void setComponentId(final short componentId) {
		if (componentId < 1 || componentId > 256) {
			throw new IllegalArgumentException(componentId + " is not between 1 and 256 inclusive.");
		}
		this.componentId = componentId;
	}

	public short getComponentId() {
		return componentId;
	}

	public void setFoundationId(final int foundationId) {
		this.foundationId = foundationId;
	}

	public int getFoundationId() {
		return foundationId;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	public Address getAddress() throws UtilityException {
		return new Address(socket.getLocalAddress().getAddress());
	}

	public int getPort() {
		return socket.getLocalPort();
	}

	public void setInUse(final boolean isInUse) {
		this.isInUse = isInUse;
	}

	public boolean getInUse() {
		return isInUse;
	}

	@Override
	public int compareTo(final Object arg0) {
		final Candidate cand = (Candidate) arg0;
		return cand.getPriority() - getPriority();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		}
		if (((Candidate) o).socket.equals(socket) && ((Candidate) o).base.equals(base)) {
			return true;
		}
		return false;
	}
}
