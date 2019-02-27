package hu.uszeged.inf.wlab.stunner.sync.communication.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Custom JSON serializer for <code>Boolean</code> values to use numeric values instead of original values. Numeric values are used in JSON
 * messages to be aware of non-java client compatibility like iOS platform. Values are identified according to the corresponding fields
 * defined in <code>BooleanJsonValue</code> on these platforms.
 */
public class BooleanJsonSerializer extends JsonSerializer<Boolean> {

	@Override
	public void serialize(final Boolean value, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
			throws IOException {
		if (null == value) {
			// serialize NULL if the value is NULL
			jsonGenerator.writeObject(null);
		}
		if (value) {
			// serialize numeric value for Boolean.TRUE
			jsonGenerator.writeObject(BooleanJsonValue.TRUE);
		} else {
			// serialize numeric value for Boolean.FALSE
			jsonGenerator.writeObject(BooleanJsonValue.FALSE);
		}
	}
}
