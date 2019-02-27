package hu.uszeged.inf.wlab.stunner.service.resultreceiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * {@link ResultReceiver} implementation to follow the IntentService +
 * ResultReceiver pattern when using RESTful servlet calls. This class is
 * responsible to deliver the results to the caller via the specified
 * {@link Handler} object.
 * 
 * @author szelezsant
 */
public class ServiceResultReceiver extends ResultReceiver {

	/** The actual {@link Receiver} interface implementation. */
	private Receiver receiver;

	/**
	 * Constructor.
	 * 
	 * @param handler
	 *            - the {@link Handler} object to pass deliver messages
	 */
	public ServiceResultReceiver(final Handler handler) {
		super(handler);
	}

	/**
	 * Sets the receiver.
	 * 
	 * @param receiver
	 *            - the actual {@link Receiver} implementation
	 */
	public void setReceiver(final Receiver receiver) {
		this.receiver = receiver;
	}

	@Override
	protected void onReceiveResult(final int resultCode, final Bundle resultData) {
		if (null != receiver) {
			receiver.onRecieveResult(resultCode, resultData);
		}
	}

	/**
	 * Interface to implement the actual logic when receiving service result.
	 * 
	 * @author szelezsant
	 */
	public interface Receiver {
		/**
		 * Delivers the results of the servlet call.
		 * 
		 * @param resultCode
		 *            - the result code from MciResult.
		 * @param data
		 *            - the bundled data.
		 */
		public void onRecieveResult(int resultCode, final Bundle data);
	}
}
