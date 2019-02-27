package hu.uszeged.inf.wlab.stunner.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Bound service to enable the interaction between the OS and the authenticator component.
 * 
 * @author szelezsant
 */
public class AuthenticatorService extends Service {
	/** The authenticator instance. */
	private StunItAuthenticator authenticator;

	@Override
	public void onCreate() {
		super.onCreate();
		authenticator = new StunItAuthenticator(this);
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return authenticator.getIBinder();
	}
}