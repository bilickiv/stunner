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

package de.javawi.jstun.attribute;

import de.javawi.jstun.util.Utility;
import de.javawi.jstun.util.UtilityException;

@SuppressWarnings("PMD")
public class Username extends MessageAttribute {
	String username;

	public Username() {
		super(MessageAttribute.MessageAttributeType.Username);
	}

	public Username(final String username) {
		super(MessageAttribute.MessageAttributeType.Username);
		setUsername(username);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public byte[] getBytes() throws UtilityException {
		int length = username.length();
		// username header
		if (length % 4 != 0) {
			length += 4 - length % 4;
		}
		// message attribute header
		length += 4;
		final byte[] result = new byte[length];
		// message attribute header
		// type
		System.arraycopy(Utility.integerToTwoBytes(typeToInteger(type)), 0, result, 0, 2);
		// length
		System.arraycopy(Utility.integerToTwoBytes(length - 4), 0, result, 2, 2);

		// username header
		final byte[] temp = username.getBytes();
		System.arraycopy(temp, 0, result, 4, temp.length);
		return result;
	}

	public static Username parse(final byte[] data) {
		final Username result = new Username();
		final String username = new String(data);
		result.setUsername(username);
		return result;
	}
}
