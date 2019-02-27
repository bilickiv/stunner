package hu.uszeged.inf.wlab.stunner.screens.settings;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.screens.bookmarks.BookmarksActivity;
import hu.uszeged.inf.wlab.stunner.screens.discovery.MainActivity;
import hu.uszeged.inf.wlab.stunner.service.StateManagerService;
import hu.uszeged.inf.wlab.stunner.utils.Constants;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * {@link PreferenceActivity} to display the available settings.
 *
 * @author szelezsant
 *
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private final static String TAG = "SettingsActivity";
	private boolean oldValue;
	private Boolean value = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		Preference checkBox = findPreference(Constants.PREF_KEY_BACKGROUND_SERVICE);
		checkBox.setOnPreferenceChangeListener(checkboxListener);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		oldValue = preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
	}

	@Override
	protected void onResume() {
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	/**
	 * If not changes, not startActivity
	 */
	@Override
	public void onBackPressed() {
		if(oldValue != value) {
			if(value){
				final Intent serviceStarter = new Intent(SettingsActivity.this, StateManagerService.class);
				serviceStarter.setAction(Constants.ACTION_SERVICE_TOGGLED);
				if(Build.VERSION.SDK_INT >= 28){
					if(isRestricted()){
						final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int which) {
								if (DialogInterface.BUTTON_POSITIVE == which) {
									openSettings();
									setPreference(false);
								}else if(DialogInterface.BUTTON_NEGATIVE == which){
									setPreference(false);
								}
								dialog.dismiss();
							}
						};
						new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.background_restriction).setMessage(R.string.background_restricted)
								.setNegativeButton(android.R.string.cancel, listener).setPositiveButton(R.string.settings, listener).create().show();
					}else{
						StateManagerService.enqueueWork(SettingsActivity.this, serviceStarter);
						Log.d(TAG, "BackgroundService is running");
					}
				}else{
					StateManagerService.enqueueWork(SettingsActivity.this, serviceStarter);
					Log.d(TAG, "BackgroundService is running");
				}
			} else {
				Log.d(TAG, "BackgroundService is not running");
				final Intent serviceStarter = new Intent(SettingsActivity.this, StateManagerService.class);
				serviceStarter.setAction(Constants.ACTION_SERVICE_TOGGLED_OFF);
				StateManagerService.enqueueWork(SettingsActivity.this, serviceStarter);
			}
			Toast.makeText(SettingsActivity.this, "Application reload!", Toast.LENGTH_LONG).show();
			final Intent homeIntent = new Intent(this, MainActivity.class);
			homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(homeIntent);
		}else{
			Log.d("SettingsActivity", "No change");
			super.onBackPressed();
		}
		Log.d(TAG, "onBackPressed!");
	}


	private Preference.OnPreferenceChangeListener checkboxListener = new Preference.OnPreferenceChangeListener() {
		public boolean onPreferenceChange(final Preference preference, final Object newValue) {
			Log.d(TAG, "Preference value " + preference.getKey() + " changed to " + newValue.toString());
			if(newValue.toString()=="false"){
				value = false;
			}else{
				value = true;
			}
			return true;
		}
	};

	public void setPreference(boolean value){
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor editor = preferences.edit();
		editor.putBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, value);
		editor.commit();
		finish();
	}


	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {}

	@RequiresApi(api = Build.VERSION_CODES.P)
	public boolean isRestricted(){
		if(Build.VERSION.SDK_INT >= 28) {
			ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			if (activityManager.isBackgroundRestricted()) {
				return true;
			}
		}
		return false;
	}

	private void openSettings() {
		startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
	}
}
