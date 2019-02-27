package hu.uszeged.inf.wlab.stunner.sync.communication.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * Custom JSON deserializer for <code>Boolean</code> values to retrieve original boolean values from numeric values. Numeric values are used
 * in JSON messages to be aware of non-java client compatibility like iOS platform. Values are identified according to the corresponding
 * fields defined in <code>BooleanJsonValue</code> on these platforms.
 */
public class BooleanJsonDeserializer extends JsonDeserializer<Boolean> {

	@Override
	public Boolean deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		if (parser.getCurrentToken().isNumeric()) {
			// try to retrieve boolean value from numeric value
			final int value = parser.getIntValue();
			if (value == BooleanJsonValue.TRUE) {
				// Boolean.TRUE is identified
				return true;
			} else if (value == BooleanJsonValue.FALSE) {
				// Boolean.FALSE is identified
				return false;
			}
		} else if (parser.getCurrentToken().equals(JsonToken.VALUE_NULL)) {
			// The passed value is NULL, return NULL
			return null;
		}
		// Throw exception if non of the above conditions were valid.
		// This means that the original value is not correctly serialized.
		throw new IllegalArgumentException("Token is not serialized correctly! Valid values are: NULL, " + BooleanJsonValue.TRUE + " or "
				+ BooleanJsonValue.FALSE
				+ ". Current value: " + parser.getText());
	}
}