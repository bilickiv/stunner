package hu.uszeged.inf.wlab.stunner.sync.communication.response;

import java.io.Serializable;

/**
 * The result of the upload process.
 * 
 * @author szelezsant
 */
public class UploadResponse implements Serializable {

	/**
	 * Generated Id.
	 */
	private static final long serialVersionUID = 2863426176048283147L;

	/** The response. */
	private Result result;

	/**
	 * @return the response
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * @param response the response to set
	 */
	public void setResult(final Result response) {
		this.result = response;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Result [result=" + result + "]";
	}

}
