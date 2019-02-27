package hu.uszeged.inf.wlab.stunner.utils.enums;

import hu.uszeged.inf.wlab.stunner.R;

/**
 * Enumeration for the possible results of the discovery test.
 * 
 * @author szelezsant
 */
public enum NatDiscoveryResult {
	/** No NAT is present, open access to the internet. */
	OPEN_ACCESS(0, R.string.open_access),
	/** Firewall blocks UDP packages. */
	FIREWALL_BLOCKS(1, R.string.firewall_blocks),
	/** Symmetric firewall is present. */
	SYMMETRIC_FIREWALL(2, R.string.symmetric_firewall),
	/** Full cone NAT handles connections. */
	FULL_CONE(3, R.string.full),
	/** Restricted cone NAT handles connections. */
	RESTRICTED_CONE(4, R.string.restricted),
	/** Port restricted cone NAT handles connections. */
	PORT_RESTRICTED_CONE(5, R.string.port_restricted),
	/** Symmetric cone NAT handles connections. */
	SYMMETRIC_CONE(6, R.string.symmetric),
	/** Result contains error. */
	ERROR(-1, R.string.error),
	/** Unknown result. */
	UNKNOWN(-2, R.string.unknown),
	/** Did not start */
	DID_NOT_STARTED(-3, R.string.did_not_start);

	/** Unique code to identify each discoveryResult element. */
	private int code;

	/** The identifier of the associated string resource which contains the human-readable description. */
	private int resourceId;

	/**
	 * Constructor.
	 * 
	 * @param code - the code.
	 * @param resourceId - the string resource identifier.
	 */
	NatDiscoveryResult(final int code, final int resourceId) {
		this.code = code;
		this.resourceId = resourceId;
	}

	/**
	 * Gets the code of the active element.
	 * 
	 * @return the unique code.
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Gets the associated resource identifier.
	 * 
	 * @return resource id.
	 */
	public int getResourceId() {
		return this.resourceId;
	}

	/**
	 * Gets the corresponding {@link NatDiscoveryResult} element. Throws {@link IllegalArgumentException} if the supplied code is invalid.
	 * 
	 * @param code - the code.
	 * @return discoveryResult
	 */
	public static NatDiscoveryResult getByCode(final int code) {
		for (final NatDiscoveryResult current : NatDiscoveryResult.values()) {
			if (current.getCode() == code) {
				return current;
			}
		}
		throw new IllegalArgumentException("Invalid result code supplied!");
	}
}
