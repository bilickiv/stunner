package hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders;

import hu.uszeged.inf.wlab.stunner.utils.Constants;

import java.util.Locale;

/**
 * Utility class to perform parsing operations.
 * 
 * @author szelezsant
 */
public final class Parser {

	/** Shifts the bytes of the ip address. */
	private static final int IPV4_BIT_SHIFTER = 8;
	/** The mask used when shifting bits. */
	private static final int SHIFT_MASK = 0xff;

	/**
	 * Hidden constructor.
	 */
	private Parser() {

	}

	/**
	 * Parses the supplied IP address to the specified {@link Constants#IP_FORMAT format}.
	 * 
	 * @param ipAddress - the address represented with an integer value.
	 * @return the formatted address as a string
	 */
	public static String parseIp(final int ipAddress) {
		int shiftPosition = 0;
		return String.format(Locale.getDefault(), Constants.IP_FORMAT, ipAddress & SHIFT_MASK, ipAddress >> ++shiftPosition
				* IPV4_BIT_SHIFTER & SHIFT_MASK, ipAddress >> ++shiftPosition * IPV4_BIT_SHIFTER & SHIFT_MASK, ipAddress >> ++shiftPosition
				* IPV4_BIT_SHIFTER & SHIFT_MASK);
	}

}
