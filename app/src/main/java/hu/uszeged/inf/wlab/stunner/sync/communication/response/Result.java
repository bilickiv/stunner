package hu.uszeged.inf.wlab.stunner.sync.communication.response;

import hu.uszeged.inf.wlab.stunner.sync.communication.json.BooleanJsonDeserializer;
import hu.uszeged.inf.wlab.stunner.sync.communication.json.BooleanJsonSerializer;
import hu.uszeged.inf.wlab.stunner.sync.communication.json.ErrorCodeJsonDeserializer;
import hu.uszeged.inf.wlab.stunner.sync.communication.json.ErrorCodeJsonSerializer;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Upload response.
 * 
 * @author szelezsant
 */
public class Result implements Serializable {

	/** Generated serial version UID. */
	private static final long serialVersionUID = -3166109651736428841L;

	/** Whether the request was successfully processed or not. If not, the error code is passed with the response. */
	@JsonSerialize(using = BooleanJsonSerializer.class)
	@JsonDeserialize(using = BooleanJsonDeserializer.class)
	private boolean success;

	/**
	 * If the request processing was unsuccessful the error code describes the root cause. If the request was succeessfully processed thi
	 * field is NULL.
	 */
	@JsonSerialize(using = ErrorCodeJsonSerializer.class)
	@JsonDeserialize(using = ErrorCodeJsonDeserializer.class)
	private ErrorCode errorCode;

	/**
	 * Default constructor.
	 */
	public Result() {
		super();
	}

	/**
	 * Constructor with attribute initialization.
	 * 
	 * @param success the request was successfully processed or not
	 * @param errorCode the root cause of the failure if the request processing was unsuccessful.
	 */
	public Result(final boolean success, final ErrorCode errorCode) {
		super();
		this.success = success;
		this.errorCode = errorCode;
	}

	/**
	 * @return whether the request was successfully processed or not
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success whether the request was successfully processed or not to set
	 */
	public void setSuccess(final boolean success) {
		this.success = success;
	}

	/**
	 * @return the error code
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode the error code to set
	 */
	public void setErrorCode(final ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
}
