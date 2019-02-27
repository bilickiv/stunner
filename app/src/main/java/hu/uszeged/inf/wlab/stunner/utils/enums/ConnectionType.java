package hu.uszeged.inf.wlab.stunner.utils.enums;

/**
 * Enumeration to store the possible connection modes.
 * 
 * @author szelezsant
 * 
 */
public enum ConnectionType {
    /** Mobile connection. */
    MOBILE(0),
    /** Wifi connection. */
	WIFI(1),
	/** Other connection */
	OTHER(2),
    /** No connection */
    NO_CONNECTION(-1),
	/** Unknown. */
	UNKNOWN(-2);

	/** The code. */
	private int code;

	/**
	 * Constructor.
	 * 
	 * @param code
	 *            - the code.
	 */
	ConnectionType(final int code) {
		this.code = code;
	}

	/**
	 * Gets the code.
	 * 
	 * @return code.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Gets the corresponding {@link ConnectionType} element.
	 * 
	 * @param code
	 *            - the code to match
	 * @return {@link ConnectionType}
	 */
	public static ConnectionType getByCode(final int code) {
		switch (code) {
		case 1:
			return WIFI;
		case 0:
			return MOBILE;
		case 2:
		    return OTHER;
        case -1:
            return NO_CONNECTION;
        default:
			return UNKNOWN;
		}
	}
}
