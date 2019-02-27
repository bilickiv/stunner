package hu.uszeged.inf.wlab.stunner.sync.communication.json;

/**
 * Common settings for custom <code>Boolean</code> JSON serializer and deserializer.
 */
public final class BooleanJsonValue {

	/** The JSON element representing <code>TRUE</code> value for boolean attributes. */
	public static final int TRUE = 1;

	/** The JSON element representing <code>FALSE</code> value for boolean attributes. */
	public static final int FALSE = 0;

	/** Default constructor. */
	private BooleanJsonValue() {
		super();
	}
}
