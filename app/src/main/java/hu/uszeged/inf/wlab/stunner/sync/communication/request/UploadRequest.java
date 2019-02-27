package hu.uszeged.inf.wlab.stunner.sync.communication.request;

import hu.uszeged.inf.wlab.stunner.utils.Constants;

import java.io.Serializable;
import java.util.List;

/**
 * The request used to upload the discoveries.
 * 
 * @author szelezsant
 */
public class UploadRequest implements Serializable {

	/**
	 * Generated version UID.
	 */
	private static final long serialVersionUID = 7670079349459380003L;

	/** The device identifier. Hashed. */
	private String deviceId;

	/** The list of serialized objects to upload. */
	private List<Record> records;

	/**
	 * Constructor.
	 * 
	 * @param deviceId - the HASHED identifier of the device.
	 * @param records - the collection of records to be uploaded.
	 */
	public UploadRequest(final String deviceId, final List<Record> records) {
		this.deviceId = deviceId;
		this.records = records;
	}

	/**
	 * Gets the package name.
	 * 
	 * @return package
	 */
	public String getPackage() {
		return Constants.PACKAGE;
	}

	/**
	 * Gets the API key.
	 * 
	 * @return key
	 */
	public String getApiKey() {
		return Constants.API_KEY;
	}

	/**
	 * Gets the device id.
	 * 
	 * @return device id.
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets the device id.
	 * 
	 * @param deviceId - the id to set.
	 */
	public void setDeviceId(final String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Gets the serialized records.
	 * 
	 * @return records
	 */
	public List<Record> getRecords() {
		return records;
	}

	/**
	 * Sets the records.
	 * 
	 * @param records - the records to upload
	 */
	public void setRecords(final List<Record> records) {
		this.records = records;
	}
}
