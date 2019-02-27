package hu.uszeged.inf.wlab.stunner.application;

import hu.uszeged.inf.wlab.stunner.R;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.android.gms.analytics.ExceptionReporter;
//import com.google.android.gms.analytics.GAServiceManager;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import com.parse.Parse;

import org.webrtc.PeerConnectionFactory;

/**
 * @author rakz
 */
public class StunApplication extends Application {
    public final String TAG = "StunApplication";
	/** Analytics tracker. */
	private static Tracker mGaTracker;

	/** Analytics instance. */
	private static GoogleAnalytics mGaInstance;

	@Override
	public void onCreate() {
		MultiDex.install(getApplicationContext());
		super.onCreate();
		// Get the GoogleAnalytics singleton. Note that the SDK uses
		// the application context to avoid leaking the current context
		mGaInstance = GoogleAnalytics.getInstance(this);
		// Use the GoogleAnalytics singleton to get a Tracker.
		//mGaTracker = mGaInstance.newTracker(this.getResources().getString(R.string.ga_trackingId));
		mGaTracker = mGaInstance.newTracker(R.xml.global_tracker);

		Parse.enableLocalDatastore(this);
		Parse.initialize(this);

		Log.d(TAG, PeerConnectionFactory.initializeAndroidGlobals(getApplicationContext(), true, true, true)
				? "Success initAndroidGlobals" : "Failed initAndroidGlobals");

		/*Parse.initialize(new Parse.Configuration.Builder(this)
				.applicationId("myAppId")
				.server("http://637c3e6b.ngrok.io/parse/")
				.build()
		);*/

		final UncaughtExceptionHandler myHandler = new ExceptionReporter(
				mGaTracker, Thread.getDefaultUncaughtExceptionHandler(), this); // Current default uncaught exception handler.

		// Make myHandler the new default uncaught exception handler.
		Thread.setDefaultUncaughtExceptionHandler(myHandler);

		((ExceptionReporter) myHandler).setExceptionParser(new GAExceptionReporter());
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}


}
