package hu.uszeged.inf.wlab.stunner.screens.discovery;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.screens.bookmarks.DeleteBookmark;
import hu.uszeged.inf.wlab.stunner.service.StateManagerService;
import hu.uszeged.inf.wlab.stunner.utils.dtos.DiscoveryDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.MobileNetInfoDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.WifiInfoDTO;
import hu.uszeged.inf.wlab.stunner.screens.bookmarks.BookmarksActivity;
import hu.uszeged.inf.wlab.stunner.screens.settings.SettingsActivity;
import hu.uszeged.inf.wlab.stunner.service.resultreceiver.ServiceResultReceiver;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryResult;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
//import android.support.v7.app.ActionBarActivity;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements ServiceResultReceiver.Receiver {

	private static final String TAG = "MainActivity";

	/**
	 * String to declare the entry of the shared preferences when the user decides to accepts the terms.
	 */
	private static final String DISCLAIMER_ACCEPTED = "accepted";

	/** Receiver to receive messages from the running service. */
	private ServiceResultReceiver receiver;
	/** Flag to indicate the current state of the ui. */
	private boolean resultsShown = false;

	/** Discovery dto to represent the discovered data. */
	private DiscoveryDTO discoveryDTO;
	/** Stores the WIFI information. */
	private WifiInfoDTO wifiInfoDTO;
	/** Stores the cellular information. */
	private MobileNetInfoDTO mobileNetDTO;
	/** Textview that indicates whether we are behind NAT. */
	private TextView natHeader;
	/** Textview that indicates nat type. */
	private TextView natType;
	/** Textview that indicates public IP. */
	private TextView natIP;
	/** Textview that indicates connection type WIFI or mobile. */
	private TextView phoneConnectionType;
	/** Textview that indicates phones IP. */
	private TextView phoneIP;
	/** Textview that indicates carrier if SIM is present. */
	private TextView phoneCarrier;
	/** Textview that indicates mobile data connection type. */
	private TextView phoneDataConnectionType;
	/** Textview that indicates MAC address. */
	private TextView phoneMAC;
	/** Textview that indicates connected WIFI SSID if connected. */
	private TextView phoneSSID;
	/** Textview that indicates WIFI link speed if connected. */
	private TextView phoneLinkSpeed;
	/** Textview that indicates WIFI signal strength if connected. */
	private TextView phoneSingalStrength;
	/** Connector item to cloud. */
	private View connector;
	/** Connection mode indicator. */
	private ImageView connectionMode;
	/** Textview that indicates information about stun server address. */
	private TextView internetInfo;
	/** Textview for lo progress indication. */
	private TextView log;
	/** Holder for actual server address. */
	private String serverAddress;
	/** The actionBar menu. */
	private Menu menu;

	private boolean pushSaveMeasurement;

	private boolean isMobileNetworkConnected;
	/*
	 * activity lifecycle mgmt
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		receiver = new ServiceResultReceiver(new Handler());

		((ImageView) findViewById(R.id.phone)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View view) {
				startStateManagerService(Constants.ACTION_USER);
			}
		});

		final AccountManager accountManager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		if (accountManager.addAccountExplicitly(new Account(Constants.DUMMY_ACCOUNT, Constants.ACCOUNT_TYPE), null, null)) {
			Log.i(getClass().getSimpleName(), "dummy account registered successfully");
		}

		natHeader = (TextView) findViewById(R.id.natHeader);
		natType = (TextView) findViewById(R.id.natType);
		natIP = (TextView) findViewById(R.id.natIP);

		phoneConnectionType = (TextView) findViewById(R.id.phoneConnectionType);

		phoneIP = (TextView) findViewById(R.id.phoneIP);

		phoneCarrier = (TextView) findViewById(R.id.phoneCarrier);
		phoneDataConnectionType = (TextView) findViewById(R.id.phoneDataConnectionType);

		phoneMAC = (TextView) findViewById(R.id.phoneMAC);
		phoneSSID = (TextView) findViewById(R.id.phoneSSID);
		phoneLinkSpeed = (TextView) findViewById(R.id.phoneLinkSpeed);
		phoneSingalStrength = (TextView) findViewById(R.id.phoneSingalStrength);

		connector = findViewById(R.id.connector);
		connectionMode = (ImageView) findViewById(R.id.connectionMode);

		internetInfo = (TextView) findViewById(R.id.internetInfo);

		log = (TextView) findViewById(R.id.log);

		isMobileNetworkConnected = false;


		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (!preferences.contains(DISCLAIMER_ACCEPTED)) {
			firstRunDisclaimer();
		}
		if (preferences.contains(DISCLAIMER_ACCEPTED) && !preferences.contains(Constants.PREF_KEY_BACKGROUND_SERVICE)) {
			showBackgroundReportingDisclaimer();
		}

		if (preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE,false)) {
			startStateManagerService(Constants.ACTION_STATE_CHECK);
		}
		// try {
		// saveDatabase();
		// } catch (final IOException e) {
		// e.printStackTrace();
		// }
	}


    @Override
	protected void onResume() {
		Log.d(TAG,"onResume");
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE,false)) {
			startStateManagerService(Constants.ACTION_STATE_CHECK);
		}		super.onResume();
		receiver.setReceiver(this);
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause");
		receiver.setReceiver(null);
		super.onPause();
	}

	/*
	 * menu handling
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu newMenu) {
		getMenuInflater().inflate(R.menu.main, newMenu);
		this.menu = newMenu;
		return true;
	}

	@Override
	public void onDestroy(){
		Log.d(TAG,"onDestroy");
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE,false)) {
			startStateManagerService(Constants.ACTION_STATE_CHECK);
		}
		//mDatabase.removeValue();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (R.id.action_show_bookmarks == item.getItemId()) {
			startActivity(new Intent(this, BookmarksActivity.class));
			return true;
		} else if (R.id.action_bookmark == item.getItemId()) {
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = preferences.edit();
			if (null != discoveryDTO) {
				pushSaveMeasurement = preferences.getBoolean("pushSaveMeasurement", Boolean.parseBoolean(""));

				/**Delete bookmark, pushSaveMeasurement is true, if the current bookmark has been added*/
				if(pushSaveMeasurement){
					DeleteBookmark deleteBookmarks = new DeleteBookmark();
					deleteBookmarks.deleteLastBookmark(this);
					Toast.makeText(MainActivity.this, "Bookmark removed",
							Toast.LENGTH_LONG).show();
					pushSaveMeasurement = false;
					editor.putBoolean("pushSaveMeasurement", pushSaveMeasurement);
					Log.d(TAG, "commit editor false");
					editor.commit();
				}else{

					/** Add bookmark*/
					ParseObject bookmarks = new ParseObject("SavedMeasurements");
					Toast.makeText(MainActivity.this, "Bookmark added",
							Toast.LENGTH_LONG).show();
					SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = new Date(discoveryDTO.getTimeStamp());
					bookmarks.put("date", f.format(date));
					bookmarks.put("localIP", discoveryDTO.getLocalIP());
					bookmarks.put("publicIP", discoveryDTO.getNatResultsDTO().getPublicIP());
					bookmarks.put("provider", discoveryDTO.getMobileDTO().getCarrier());
					bookmarks.put("result", getString(discoveryDTO.getNatResultsDTO().getDiscoveryResult().getResourceId()));
					bookmarks.put("connInterface", discoveryDTO.getConnectionMode());
					if(discoveryDTO.getWifiDTO() != null)
						if(discoveryDTO.getWifiDTO().getSsid() != null)
							bookmarks.put("ssid", (String)phoneSSID.getText());
					bookmarks.put("network", discoveryDTO.getMobileDTO().getNetworkType());
					bookmarks.pinInBackground();
					TextView tv = findViewById(R.id.action_bookmark);
					pushSaveMeasurement = true;
					editor.putBoolean("pushSaveMeasurement", pushSaveMeasurement);
					Log.d(TAG, "commit editor true");
					editor.commit();
				}
			}
			return true;
		} else if (R.id.action_settings == item.getItemId()) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRecieveResult(final int resultCode, final Bundle data) {
		final View natLabels = findViewById(R.id.natLabels);
		final View phoneLabels = findViewById(R.id.phoneLables);
		switch (resultCode) {
			case Constants.RESULT_CONNECTION_START:
				Log.i(getClass().getSimpleName(), "onReceiveResult: connectivity start");

				serverAddress = data.getString(Constants.KEY_SERVER_ADDRESS) + ":" + data.getInt(Constants.KEY_SERVER_PORT);
				onTestStart();
				break;
			case Constants.RESULT_CONNECTION_ERROR:
				Log.i(getClass().getSimpleName(), "onReceiveResult: connectivity error");

				if (data.containsKey(Constants.KEY_CONNECTION_ENABLED)) {
					final DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {

						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							/* if he declines the offer, finish the activity */
							if (DialogInterface.BUTTON_NEGATIVE == which) {
								onConnectionError(true);
								/* else navigate to the wifi settings */
							} else if (DialogInterface.BUTTON_POSITIVE == which) {
								onConnectionError(false);
							}
							dialog.dismiss();
						}
					};
					new AlertDialog.Builder(this).setMessage(R.string.no_connection).setTitle(R.string.error)
							.setNegativeButton(android.R.string.cancel, dialogListener)
							.setPositiveButton(R.string.settings, dialogListener).create().show();

				} else if (data.containsKey(Constants.KEY_CONNECTION_READY)) {
					onConnectionNotReady();
				}
				break;

			case Constants.RESULT_CONNECTION_OK:
				Log.i(getClass().getSimpleName(), "onReceiveResult: connectivity finished");

				final String localIP = data.getString(Constants.KEY_IP_ADDRESS);
				mobileNetDTO = data.getParcelable(Constants.KEY_MOBILE_DTO);
				wifiInfoDTO = data.getParcelable(Constants.KEY_WIFI_DTO);

				phoneIP.setVisibility(View.INVISIBLE);
				phoneCarrier.setVisibility(View.INVISIBLE);
				phoneDataConnectionType.setVisibility(View.GONE);
				phoneLinkSpeed.setVisibility(View.GONE);
				phoneSingalStrength.setVisibility(View.GONE);
				phoneSSID.setVisibility(View.GONE);
				phoneMAC.setVisibility(View.GONE);
				natHeader.setVisibility(View.GONE);
				natIP.setVisibility(View.GONE);
				natType.setVisibility(View.GONE);

				natLabels.setVisibility(View.GONE);
				phoneLabels.setVisibility(View.GONE);

				phoneConnectionType.setText("We are working...");

				onTestComplete(localIP);
				break;

			case Constants.RESULT_STUN_LOG:
				Log.i(getClass().getSimpleName(), "onReceiveResult: update log");

				updateLog(data.getString(Constants.KEY_DATA));
				break;

			case Constants.RESULT_STUN_ERROR:
				Log.i(getClass().getSimpleName(), "onReceiveResult: stun test error");

				onStunError();
				break;

			case Constants.RESULT_STUN_OK:
				Log.i(getClass().getSimpleName(), "onReceiveResult: stun test finished");

				discoveryDTO = data.getParcelable(Constants.KEY_DATA);

				if(isMobileNetworkConnected){
					phoneConnectionType.setText(R.string.mobile_data_connected);
				}else{
					phoneConnectionType.setText(R.string.wifi_connected);
				}

				phoneIP.setVisibility(View.VISIBLE);
				phoneCarrier.setVisibility(View.VISIBLE);
				phoneConnectionType.setVisibility(View.VISIBLE);
				phoneDataConnectionType.setVisibility(View.VISIBLE);
				phoneLinkSpeed.setVisibility(View.VISIBLE);
				phoneSingalStrength.setVisibility(View.VISIBLE);
				phoneSSID.setVisibility(View.VISIBLE);
				phoneMAC.setVisibility(View.VISIBLE);
				natHeader.setVisibility(View.VISIBLE);
				natIP.setVisibility(View.VISIBLE);
				natType.setVisibility(View.VISIBLE);

				natLabels.setVisibility(View.VISIBLE);
				phoneLabels.setVisibility(View.VISIBLE);

				doPostExecute();

				break;

			default:
				break;
		}
	}

	private void startStateManagerService(String action){
		final Intent serviceStarter = new Intent(MainActivity.this, StateManagerService.class);
		serviceStarter.setAction(action);
		serviceStarter.putExtra(Constants.KEY_RECEIVER, receiver);
		StateManagerService.enqueueWork(this,serviceStarter);
		Log.d(TAG,"StateManagerService should start! "+action);
	}

	/**
	 * Sets the UI to initial state.
	 */
	private void onTestStart() {
		enableUI(false);
		final MenuItem bookmarkMenuItem = menu.findItem(R.id.action_bookmark);
		if (resultsShown) {
			bookmarkMenuItem.setVisible(false);
			slideToShowText(false, discoveryDTO.getNatResultsDTO().getDiscoveryResult());
			initNatType();
		}

		setServerAddress(serverAddress);
		showLog(true);
		showWifiLoading(true);

		/* hide the marker and set to default state. */
		bookmarkMenuItem.setVisible(false);
	}

	/**
	 * Invoked when the test failed.
	 *
	 * @param userDeclined - flag to indicate if the user has declined to turn on an adapter.
	 */
	private void onConnectionError(final boolean userDeclined) {
		Toast.makeText(this, R.string.test_failed_please_try_again, Toast.LENGTH_SHORT).show();
		showWifiLoading(false);
		enableUI(true);
		showLog(false);

		if (!userDeclined) {
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		}
	}

	/**
	 * Invoked when the device is trying to connect to the internet, but not succeed yet.
	 */
	private void onConnectionNotReady() {
		Toast.makeText(this, R.string.test_failed_please_try_again, Toast.LENGTH_SHORT).show();
		showWifiLoading(false);
		enableUI(true);
		showLog(false);
	}

	/**
	 * Invoked when the connection test is complete. Updates the UI to display the new data.
	 *
	 * @param localIP - the local IP address.
	 */
	private void onTestComplete(final String localIP) {
		showRemoteLoading(true);
		phoneIP.setText(null == localIP ? getString(R.string.n_a) : localIP);

		if (wifiInfoDTO.getSsid() == "N/A") {
			isMobileNetworkConnected = true;
			phoneSSID.setText(R.string.n_a);
			phoneLinkSpeed.setText(R.string.n_a);
			phoneSingalStrength.setText(R.string.n_a);
		} else {
			isMobileNetworkConnected = false;
			phoneSSID.setText(wifiInfoDTO.getSsid() == null ? getString(R.string.n_a) : wifiInfoDTO.getSsid());
			phoneLinkSpeed.setText(wifiInfoDTO.getBandwidth() == null ? getString(R.string.n_a) : wifiInfoDTO.getBandwidth());
			phoneSingalStrength.setText(wifiInfoDTO.getRssi() == 0 ? getString(R.string.n_a) : wifiInfoDTO.getRssi()
					+ getString(R.string.decibel));
		}

		// mandatory data always shown
		phoneCarrier.setText(mobileNetDTO.getCarrier() == null ? getString(R.string.n_a) : mobileNetDTO.getCarrier());
		phoneDataConnectionType.setText(mobileNetDTO.getCarrier() == null ? getString(R.string.n_a) : mobileNetDTO.getNetworkType());
		phoneMAC.setText(wifiInfoDTO.getMacAddress() == null ? getString(R.string.n_a) : wifiInfoDTO.getMacAddress());
		pushSaveMeasurement = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		pushSaveMeasurement = false;
		editor.putBoolean("pushSaveMeasurement", pushSaveMeasurement);
		editor.commit();
	}

	/**
	 * Invoked when the STUN test fails.
	 */
	private void onStunError() {
		Toast.makeText(this, R.string.test_failed_please_try_again, Toast.LENGTH_SHORT).show();
		startInprogressAnimation(false);
		enableUI(true);
		showLog(false);
	}

	/**
	 * Invoked when the STUN test has finished.
	 */
	private void doPostExecute() {

		if (discoveryDTO.getNatResultsDTO().getDiscoveryResult().equals(NatDiscoveryResult.OPEN_ACCESS)) {
			natHeader.setText(R.string.open_access);
			natType.setText(R.string.n_a);
			natIP.setText(phoneIP.getText());
		} else {
			natHeader.setText(R.string.nat_detected);
			natIP.setText(discoveryDTO.getNatResultsDTO().getPublicIP());
			natType.setText(getString(discoveryDTO.getNatResultsDTO().getDiscoveryResult().getResourceId()));
		}
		setNatType(discoveryDTO.getNatResultsDTO().getDiscoveryResult());

		startInprogressAnimation(false);
		showLog(false);
		slideToShowText(true, discoveryDTO.getNatResultsDTO().getDiscoveryResult());
		menu.findItem(R.id.action_bookmark).setVisible(true);
	}

	/**
	 * Sets the STUN server's address.
	 *
	 * @param text - the server address.
	 */
	private void setServerAddress(final String text) {
		final Animation serverFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		internetInfo.setText(text);
		internetInfo.startAnimation(serverFadeIn);
	}

	/**
	 * @param show whether fade in or out log.
	 */
	private void showLog(final boolean show) {
		final Animation logFadeInOut = AnimationUtils.loadAnimation(this, show ? R.anim.fade_in : R.anim.fade_out);
		if (show) {
			log.setText("");
		} else {
			logFadeInOut.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(final Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationRepeat(final Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(final Animation animation) {
					log.setText("");
				}
			});
		}
		logFadeInOut.setFillAfter(true);
		logFadeInOut.setFillEnabled(true);
		log.startAnimation(logFadeInOut);
	}

	/**
	 * @param currentLog current log row by jstun.
	 */
	public void updateLog(final String currentLog) {
		if (log.getText().length() > 0) {
			log.setText(log.getText() + "\n" + getString(R.string.stun_console) + currentLog);
		} else {
			log.setText(getString(R.string.stun_console) + currentLog);
		}
	}

	/**
	 * @param start based on this wifi signal and cloud animation and phone clickability is set.
	 */
	private void startInprogressAnimation(final boolean start) {
		showWifiLoading(start);
		showRemoteLoading(start);
		enableUI(!start);
	}

	/**
	 * Enables the ui controls according to the given flag.
	 *
	 * @param enabled - indicates the enabled state
	 */
	private void enableUI(final boolean enabled) {
		((ImageView) findViewById(R.id.phone)).setEnabled(enabled);
		menu.findItem(R.id.action_settings).setVisible(enabled);
		menu.findItem(R.id.action_show_bookmarks).setVisible(enabled);
	}

	/**
	 * @param isLoading - the flag to determine if the loading is in progress.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showWifiLoading(final boolean isLoading) {
		final ImageView wifi = (ImageView) findViewById(R.id.wifisignal);

		final AnimationDrawable wifiFrameAnimation = (AnimationDrawable) wifi.getBackground();
		if (isLoading) {
			wifiFrameAnimation.start();
		} else {
			// reset animation to first
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				wifi.setBackgroundDrawable(null);
			} else {
				wifi.setBackground(null);
			}
			wifi.setBackgroundResource(R.drawable.wifisignal);
			wifiFrameAnimation.stop();
		}
	}

	/**
	 * @param isLoading - the flag to determine if the loading is in progress.
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void showRemoteLoading(final boolean isLoading) {
		final ImageView cloud = (ImageView) findViewById(R.id.cloud);
		cloud.setBackgroundResource(R.drawable.cloud);

		final AnimationDrawable cloudFrameAnimation = (AnimationDrawable) cloud.getBackground();
		if (isLoading) {
			cloudFrameAnimation.start();
		} else {
			// reset animation to first
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				cloud.setBackgroundDrawable(null);
			} else {
				cloud.setBackground(null);
			}
			cloud.setBackgroundResource(R.drawable.cloud_standby);
			cloudFrameAnimation.stop();
		}
	}

	/**
	 * Slides down and up elements to show result text.
	 *
	 * @param show - flag to toggle animations.
	 * @param result - the {@link NatDiscoveryResult} object to gain information from.
	 */
	private void slideToShowText(final boolean show, final NatDiscoveryResult result) {
		final View phone = findViewById(R.id.phoneContainer);
		final View remote = findViewById(R.id.remoteContainer);
		final View glory = findViewById(R.id.glory);
		final View labels = findViewById(R.id.labelsContainer);

		final Animation animationDown = AnimationUtils.loadAnimation(this, show ? R.anim.slide_out_down : R.anim.slide_in_down);
		animationDown.setFillAfter(true);
		animationDown.setFillEnabled(true);

		final Animation animationUp = AnimationUtils.loadAnimation(this, show ? R.anim.slide_out_up : R.anim.slide_in_up);
		animationUp.setFillAfter(true);
		animationUp.setFillEnabled(true);

		final Animation animationUpGlory = AnimationUtils.loadAnimation(this, show ? R.anim.slide_out_up : R.anim.slide_in_up);
		animationUpGlory.setFillAfter(true);
		animationUpGlory.setFillEnabled(true);

		final Animation animationFadein = AnimationUtils.loadAnimation(this, show ? R.anim.fade_in : R.anim.fade_out);
		animationFadein.setFillAfter(true);
		animationFadein.setFillEnabled(true);

		if (show) {
			labels.startAnimation(animationFadein);
			phone.startAnimation(animationDown);
			if (!NatDiscoveryResult.OPEN_ACCESS.equals(result)) {
				glory.startAnimation(animationUpGlory);
				remote.startAnimation(animationUp);
			}
		} else {
			phone.startAnimation(animationDown);
			if (!NatDiscoveryResult.OPEN_ACCESS.equals(result)) {
				glory.startAnimation(animationUpGlory);
				remote.startAnimation(animationUp);
			}
			labels.startAnimation(animationFadein);
		}

		resultsShown = show;
	}

	/**
	 * Initializes back NAT bubble visibility and connector visibility is invisible.
	 */
	private void initNatType() {
		if (discoveryDTO != null && NatDiscoveryResult.OPEN_ACCESS.equals(discoveryDTO.getNatResultsDTO().getDiscoveryResult())) {
			final Animation animationFadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			connector.startAnimation(animationFadein);
		}
		final Animation animationFadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		connectionMode.setImageResource(R.drawable.nat_unknown);
		connectionMode.startAnimation(animationFadein);
	}

	/**
	 * @param result this result is the basis what should be set in the bubble.
	 */
	private void setNatType(final NatDiscoveryResult result) {
		final Animation animationFadein = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		switch (result) {
			case OPEN_ACCESS:
				final Animation animationFadeout1 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
				final Animation animationFadeout2 = AnimationUtils.loadAnimation(this, R.anim.fade_out);
				animationFadeout1.setFillAfter(true);
				animationFadeout1.setFillEnabled(true);
				animationFadeout2.setFillAfter(true);
				animationFadeout2.setFillEnabled(true);
				connector.startAnimation(animationFadeout1);
				connectionMode.startAnimation(animationFadeout2);
				break;
			case FULL_CONE:
				connectionMode.setImageResource(R.drawable.nat_full_cone);
				connectionMode.startAnimation(animationFadein);
				break;
			case RESTRICTED_CONE:
				connectionMode.setImageResource(R.drawable.nat_address_restricted);
				connectionMode.startAnimation(animationFadein);
				break;
			case PORT_RESTRICTED_CONE:
				connectionMode.setImageResource(R.drawable.nat_address_port_restricted);
				connectionMode.startAnimation(animationFadein);
				break;
			case SYMMETRIC_CONE:
				connectionMode.setImageResource(R.drawable.nat_symmetric);
				connectionMode.startAnimation(animationFadein);
				break;
			case ERROR:
			case UNKNOWN:
			case SYMMETRIC_FIREWALL:
			case FIREWALL_BLOCKS:
			default:
				connectionMode.setImageResource(R.drawable.nat_unknown);
				connectionMode.startAnimation(animationFadein);
				break;
		}
	}

	/**
	 * Disclaimer shown on first run.
	 */
	private void firstRunDisclaimer() {
		final Builder builder = new Builder(this);
		builder.setTitle(R.string.disclaimer);
		builder.setMessage(Html.fromHtml(Constants.LICENSE));
		builder.setPositiveButton(R.string.accept, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface currentDialog, final int button) {
				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				final Editor editor = preferences.edit();
				editor.putBoolean(DISCLAIMER_ACCEPTED, true);
				editor.commit();
				currentDialog.dismiss();

				showBackgroundReportingDisclaimer();
			}
		});
		builder.setNegativeButton(R.string.deny, new OnClickListener() {
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@Override
			public void onClick(final DialogInterface arg0, final int arg1) {
				MainActivity.this.finish();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					final Uri packageUri = Uri.parse("package:" + getPackageName());
					final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
					startActivity(uninstallIntent);
				}
			}
		});
		builder.setCancelable(false);
		builder.show();
	}

	/**
	 * Shows the disclaimer dialog about the background service.
	 */
	private void showBackgroundReportingDisclaimer() {
		final Builder builder = new Builder(this);
		builder.setCancelable(false);

		builder.setPositiveButton(R.string.accept, new OnClickListener() {
			@Override
			public void onClick(final DialogInterface currentDialog, final int button) {
				final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
				final Editor editor = preferences.edit();
				if(Build.VERSION.SDK_INT >= 28) {
					if(isRestricted()) {
						editor.putBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, false);
					} else {
						editor.putBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
					}
				}else{
					editor.putBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
				}				editor.putBoolean(Constants.PREF_KEY_PARSE_SERVICE, true);
				editor.commit();
				startStateManagerService(Constants.ACTION_REGISTER_ALARMS_FIRST_RUN);
				currentDialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.deny, new OnClickListener() {
			@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			@Override
			public void onClick(final DialogInterface arg0, final int arg1) {
				MainActivity.this.finish();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					final Uri packageUri = Uri.parse("package:" + getPackageName());
					final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
					startActivity(uninstallIntent);
				}
			}
		});

		builder.setTitle(R.string.disclaimer);
		builder.setMessage(R.string.background_disclaimer);
		builder.create().show();
	}

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

}
