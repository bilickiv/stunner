package hu.uszeged.inf.wlab.stunner.service.broadcastreceiver;

import hu.uszeged.inf.wlab.stunner.service.StateManagerService;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryPluggedState;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryStatusChargingState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServiceTriggerBroadcastReceiver extends BroadcastReceiver {

	private final String TAG = "ServiceTriggerBReceiver";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		/* copy intent payload and start the discovey service */
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isBackgroundServiceRunnable = preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
		if(isBackgroundServiceRunnable) {
			final Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			final Bundle extras = batteryIntent.getExtras();
			boolean stateManagerStart = false;
			if(isOnCharging(context)){
				Log.d(TAG, "onReceive " +intent.getAction() + " batteryStatus:onCharger");
				stateManagerStart = true;
			} else {
				Log.d(TAG, "onReceive " +intent.getAction() + " batteryStatus:notCharging");
				switch (intent.getAction()){
					case Intent.ACTION_SHUTDOWN :
					case Intent.ACTION_REBOOT:
					case Intent.ACTION_BOOT_COMPLETED:
					case Intent.ACTION_POWER_DISCONNECTED:
					case Intent.ACTION_POWER_CONNECTED:
					case Intent.ACTION_TIME_CHANGED:
					case Intent.ACTION_DATE_CHANGED:
					case Intent.ACTION_TIMEZONE_CHANGED:
						stateManagerStart = true;
						break;
					default:
						break;
				}
			}
			if(stateManagerStart) {
				final Intent serviceStarter = new Intent(context, StateManagerService.class);
				serviceStarter.putExtras(intent);
				serviceStarter.setAction(intent.getAction());
				StateManagerService.enqueueWork(context,serviceStarter);
			}
		}
	}

	public boolean isOnCharging(final Context context) {
		final Bundle extras =  context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getExtras();
		BatteryStatusChargingState chargingState = BatteryStatusChargingState.getByCode(extras.getInt(BatteryManager.EXTRA_STATUS, -1));
		BatteryPluggedState pluggedState = BatteryPluggedState.getByCode(extras.getInt(BatteryManager.EXTRA_PLUGGED, -1));
		if (isOnCharging(chargingState,pluggedState)) {
			return true;
		} else {
			return  false;
		}
	}
	public boolean isOnCharging(BatteryStatusChargingState chargingState, BatteryPluggedState pluggedState){
		if (chargingState == BatteryStatusChargingState.CHARGING || chargingState == BatteryStatusChargingState.FULL
				|| pluggedState == BatteryPluggedState.AC || pluggedState == BatteryPluggedState.USB
				|| pluggedState == BatteryPluggedState.WIRELESS) {
			return true;
		} else {
			return  false;
		}
	}
}
