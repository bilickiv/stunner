package hu.uszeged.inf.wlab.stunner.sync.communication.json;

import hu.uszeged.inf.wlab.stunner.sync.communication.response.ErrorCode;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Serializer for {@link ErrorCode} values.
 * 
 * @author szelezsant
 */
public class ErrorCodeJsonSerializer extends JsonSerializer<ErrorCode> {

	@Override
	public void serialize(final ErrorCode value, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
			throws IOException {
		if (null == value) {
			// serialize as NULL if the value is NULL
			jsonGenerator.writeObject(null);
		}
		// serialize the private index instead of the concrete enum value
		jsonGenerator.writeObject(value.getCode());
	}
}
