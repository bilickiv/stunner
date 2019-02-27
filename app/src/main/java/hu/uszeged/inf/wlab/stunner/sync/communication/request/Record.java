package hu.uszeged.inf.wlab.stunner.sync.communication.request;

import hu.uszeged.inf.wlab.stunner.utils.dtos.DiscoveryDTO;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

/**
 * Record class to store the timestamp and the json encoded discovery.
 * 
 * @author szelezsant
 */
public class Record {
	public static final String TAG = "Record";
	/** The json encoded discovery. */
	private String data;

	/**
	 * Constructor.
	 * 
	 * @param discovery - the discovery object.
	 */
	public Record(final DiscoveryDTO discovery) {
		try {
			data = new ObjectMapper().writeValueAsString(discovery);
		} catch (final JsonGenerationException e) {
			Log.e(TAG,": error while generating json: "+ e.toString());
		} catch (final JsonMappingException e) {
			Log.e(TAG,": error while mapping json: "+ e.toString());
		} catch (final IOException e) {
			Log.e(TAG,": IO exception while generating json: "+ e.toString());
		}
	}

	public Record(final String discovery) {
		data = discovery;
	}

	/**
	 * The json encoded discovery.
	 * 
	 * @return json string
	 */
	public String getData() {
		return data;
	}
}