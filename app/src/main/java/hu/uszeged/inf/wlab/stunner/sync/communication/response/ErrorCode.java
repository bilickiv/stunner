package hu.uszeged.inf.wlab.stunner.sync.communication.response;

/**
 * Enumeration containing the possible error codes.
 * 
 * @author szelezsant
 */
public enum ErrorCode {

	/** Request error. */
	CLIENT_ERROR(400),
	/** Server error. */
	SERVER_ERROR(500);

	/** The numeric code of the error. */
	private int code;

	/**
	 * Constructor with attribute initialization.
	 * 
	 * @param code the code of the error
	 */
	private ErrorCode(final int code) {
		this.code = code;
	}

	/**
	 * @return the code of the error
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the corresponding enum value for a given index.
	 * 
	 * @param searchIndex the index for which the enum value is requested.
	 * @return the resolved enum value or NULL if index is not associated for any of the enum values
	 */
	public static ErrorCode resolveValueFromIndex(final int searchIndex) {
		for (final ErrorCode code : values()) {
			if (code.getCode() == searchIndex) {
				return code;
			}
		}
		return null;
	}
}
