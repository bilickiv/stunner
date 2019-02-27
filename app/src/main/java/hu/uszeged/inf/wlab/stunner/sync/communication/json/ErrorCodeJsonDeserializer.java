package hu.uszeged.inf.wlab.stunner.sync.communication.json;

import hu.uszeged.inf.wlab.stunner.sync.communication.response.ErrorCode;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * Deserializer for {@link ErrorCode} values.
 * 
 * @author szelezsant
 */
public class ErrorCodeJsonDeserializer extends JsonDeserializer<ErrorCode> {

	@Override
	public ErrorCode deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
		if (parser.getCurrentToken().equals(JsonToken.VALUE_NULL)) {
			// if the value is NULL, return NULL
			return null;
		} else if (parser.getCurrentToken().isNumeric()) {
			// try to resolve concrete enum value from index
			final int value = parser.getIntValue();
			final ErrorCode result = ErrorCode.resolveValueFromIndex(value);
			if (null != result) {
				// Enum value is successfully resolved
				return result;
			}
		}
		// Throw exception if non of the above conditions were valid.
		// This means that the original value is not correctly serialized.
		throw new IllegalArgumentException(
				"Token is not serialized correctly! Valid values are: NULL or valid index values from enum! Current value: "
						+ parser.getText());
	}
}
