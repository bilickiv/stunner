package hu.uszeged.inf.wlab.stunner.service;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.JobIntentService;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import hu.uszeged.inf.wlab.stunner.BuildConfig;
import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders.MobileNetworkInfoBuilder;
import hu.uszeged.inf.wlab.stunner.screens.discovery.infobuilders.Parser;
import hu.uszeged.inf.wlab.stunner.service.triggerjobservice.StateManagerServiceTriggerJobService;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.GeneralResource;
import hu.uszeged.inf.wlab.stunner.utils.dtos.BatteryInfoDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.DiscoveryDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.MobileNetInfoDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.UptimeInfoDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.WifiInfoDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryHealth;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryPluggedState;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryStatusChargingState;
import hu.uszeged.inf.wlab.stunner.utils.enums.ConnectionType;
import hu.uszeged.inf.wlab.stunner.utils.enums.DetailedNetworkStatus;
import hu.uszeged.inf.wlab.stunner.utils.enums.NatDiscoveryResult;
import hu.uszeged.inf.wlab.stunner.utils.enums.DiscoveryTriggerEvents;
import hu.uszeged.inf.wlab.stunner.utils.enums.MobileNetType;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;
import hu.uszeged.inf.wlab.stunner.utils.enums.WifiState;

public class StateManagerService extends JobIntentService {
    public static final String TAG = "StateManagerService";
    /**
     * Unique job ID for this service.
     */
    public static final int JOB_ID = 23025205;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, StateManagerService.class, JOB_ID, work);
    }

    private Long startTimeStamp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        Log.d(TAG, "StateManagerService work " + intent.getAction());
        startTimeStamp = System.currentTimeMillis();
        ServiceMonitorActions serviceMonitorActions = ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY;
        String remotePeerID =  Constants.PREF_STRING_VALUE_EMPTY;
        String messageType = Constants.PREF_STRING_VALUE_EMPTY;
        String messageData = Constants.PREF_STRING_VALUE_EMPTY;
        DiscoveryTriggerEvents triggerEvent = DiscoveryTriggerEvents.UNKNOWN;
        ResultReceiver receiver = null; // Receiver from MainActivity
        GsonBuilder builder = new GsonBuilder();//.setPrettyPrinting();
        Gson gson = builder.create();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String prevStateJSON = preferences.getString(Constants.KEY_DISCOVERY_DTO,"null");
        DiscoveryDTO prevStateDTO;
        if( prevStateJSON == null || prevStateJSON.equals("null") ){
            Log.d(TAG, "Firstrun "+prevStateJSON);
            prevStateDTO = new DiscoveryDTO();
        } else {
            prevStateDTO = gson.fromJson(prevStateJSON, DiscoveryDTO.class);
        }
        // handle action cases
        switch (intent.getAction()) {
            case Constants.ACTION_STATE_CHECK:
                triggerEvent = DiscoveryTriggerEvents.SCHEDULED_STATE_CHECK;
                break;
            case Constants.ACTION_USER:
                triggerEvent = DiscoveryTriggerEvents.USER;
                receiver = intent.getParcelableExtra(Constants.KEY_RECEIVER);
                serviceMonitorActions = ServiceMonitorActions.USER_TRIGGERED_SERVICE_START;
                break;
            case ConnectivityManager.CONNECTIVITY_ACTION:
                triggerEvent = DiscoveryTriggerEvents.CONNECTION_CHANGED;
                break;
            case Intent.ACTION_AIRPLANE_MODE_CHANGED:
                triggerEvent = DiscoveryTriggerEvents.AIRPLANE_MODE_CHANGED;
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                triggerEvent = DiscoveryTriggerEvents.BATTERY_POWER_DISCONNECTED;
                break;
            case Intent.ACTION_POWER_CONNECTED:
                if (DiscoveryTriggerEvents.BATTERY_POWER_CONNECTED == DiscoveryTriggerEvents.getByCode(prevStateDTO.getTriggerCode())){
                    triggerEvent = DiscoveryTriggerEvents.SCHEDULED_STATE_CHECK;
                } else{
                    triggerEvent = DiscoveryTriggerEvents.BATTERY_POWER_CONNECTED;
                }
                break;
            case Intent.ACTION_SHUTDOWN:
                triggerEvent = DiscoveryTriggerEvents.ACTION_SHUTDOWN;
                break;
            case Intent.ACTION_REBOOT:
                triggerEvent = DiscoveryTriggerEvents.REBOOT;
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                triggerEvent = DiscoveryTriggerEvents.BOOT_COMPLETED;
                break;
            case Intent.ACTION_TIME_CHANGED:
                triggerEvent = DiscoveryTriggerEvents.TIME_CHANGED;
                break;
            case Intent.ACTION_DATE_CHANGED:
                triggerEvent = DiscoveryTriggerEvents.DATE_CHANGED;
                break;
            case Intent.ACTION_TIMEZONE_CHANGED:
                triggerEvent = DiscoveryTriggerEvents.TIMEZONE_CHANGED;
                break;
            case Constants.ACTION_SERVICE_TOGGLED :
                triggerEvent = DiscoveryTriggerEvents.SERVICE_TOGGLED;
                break;
            case Constants.ACTION_SERVICE_TOGGLED_OFF :
                triggerEvent = DiscoveryTriggerEvents.SERVICE_TOGGLED_OFF;
                break;
            case Constants.ACTION_REGISTER_ALARMS_FIRST_RUN :
                triggerEvent = DiscoveryTriggerEvents.FIRST_START;
                break;
            case Constants.ACTION_FIREBASE_MESSAGE_IS_RECEIVED:
                triggerEvent = DiscoveryTriggerEvents.FIREBASE_MESSAGE_IS_RECEIVED;
                serviceMonitorActions = ServiceMonitorActions.FIREBASE_MESSAGE_IS_RECEIVED;
                remotePeerID = intent.getExtras().getString(Constants.KEY_REMOTE_PEER_ID);
                messageType = intent.getExtras().getString(Constants.KEY_TYPE);
                messageData = intent.getExtras().getString(Constants.KEY_MESSAGE_DATA);
                break;
            default:
                Log.e(TAG, "Unhandled trigger event: " + intent.getAction());
                break;
        }

        // discover the recent state if necessary
        DiscoveryDTO recStateDTO;
        recStateDTO = dicover(prevStateDTO, triggerEvent);
        triggerEvent = DiscoveryTriggerEvents.getByCode(recStateDTO.getTriggerCode());
        boolean saveDiscovery;
        switch (triggerEvent) {
            case FIREBASE_MESSAGE_IS_RECEIVED:
            case SCHEDULED_STATE_CHECK:
                saveDiscovery = false;
                break;
            default:
                saveDiscovery = true;
                break;
        }
        // if USER than send connection info to MainActivity
        if(serviceMonitorActions == ServiceMonitorActions.USER_TRIGGERED_SERVICE_START){
            if(isOnlineStatus()) {
                final Bundle resultBundle = new Bundle();
                resultBundle.putString(Constants.KEY_IP_ADDRESS, recStateDTO.getLocalIP());
                resultBundle.putParcelable(Constants.KEY_WIFI_DTO, recStateDTO.getWifiDTO());
                resultBundle.putParcelable(Constants.KEY_MOBILE_DTO, recStateDTO.getMobileDTO());
                receiver.send(Constants.RESULT_CONNECTION_OK, resultBundle);
            } else {
                final Bundle errorBundle = new Bundle();
                errorBundle.putBoolean(Constants.KEY_CONNECTION_ENABLED, false);
                receiver.send(Constants.RESULT_CONNECTION_ERROR, errorBundle);
                serviceMonitorActions = ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY;
                saveDiscovery = false;
            }
        }
        // check if service start is necessary
        if(serviceMonitorActions == ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY) {
            if (isServiceStartNecessary(prevStateDTO,recStateDTO)) {
                serviceMonitorActions = ServiceMonitorActions.BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START;
                saveDiscovery = true;
            }
        }
        boolean isBackgroundServiceEnable = preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
        if (!isBackgroundServiceEnable) {
            saveDiscovery = false;
            if(serviceMonitorActions != ServiceMonitorActions.USER_TRIGGERED_SERVICE_START){
                serviceMonitorActions = ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY;
            }
        }
        // If necessary than modify Firebase state
        if(isOnlineStatus() && isBackgroundServiceEnable) {
            if(triggerEvent ==  DiscoveryTriggerEvents.BATTERY_POWER_DISCONNECTED) {
                firebaseStateModify(Constants.KEY_CLOSE_CONNECTION);
            } else if(isOnCharging()) {
                if(triggerEvent ==  DiscoveryTriggerEvents.BATTERY_POWER_CONNECTED || triggerEvent == DiscoveryTriggerEvents.CONNECTION_CHANGED || triggerEvent == DiscoveryTriggerEvents.BOOT_COMPLETED ) {
                    firebaseStateModify(Constants.KEY_LOGIN);
                } else if(serviceMonitorActions == ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY) {
                    serviceMonitorActions = ServiceMonitorActions.FIREBASE_UPDATE_STATE;
                }
            }
        }
        // start service or just store the state or do nothing
        String jsonStringDiscoveryDTOforPrevState = Constants.PREF_STRING_VALUE_EMPTY;
        if (serviceMonitorActions != ServiceMonitorActions.SERVICE_START_IS_NOT_NECESSARY) {
            recStateDTO.getNatResultsDTO().setDiscoveryResultCode(NatDiscoveryResult.UNKNOWN);
            Intent serviceStarter = new Intent(StateManagerService.this, ServiceMonitor.class);
            if(serviceMonitorActions == ServiceMonitorActions.FIREBASE_MESSAGE_IS_RECEIVED){
                serviceStarter.putExtra(Constants.KEY_REMOTE_PEER_ID, remotePeerID);
                serviceStarter.putExtra(Constants.KEY_TYPE, messageType);
                serviceStarter.putExtra(Constants.KEY_MESSAGE_DATA, messageData);
                if (messageType.equals(Constants.KEY_OFFER)){
                    recStateDTO.getNatResultsDTO().setLastDiscovery(System.currentTimeMillis());
                }
            } else if (serviceMonitorActions != ServiceMonitorActions.FIREBASE_UPDATE_STATE) {
                saveDiscovery = false; //because it will be saved in ServiceMonitor
                recStateDTO.getNatResultsDTO().setLastDiscovery(System.currentTimeMillis());
            }
            serviceStarter.setAction(serviceMonitorActions.getServiceStarterString());
            jsonStringDiscoveryDTOforPrevState = gson.toJson(recStateDTO);
            String jsonStringDisvoceryDTOforIntent;
            if(serviceMonitorActions == ServiceMonitorActions.USER_TRIGGERED_SERVICE_START) {
                serviceStarter.putExtra(Constants.KEY_RECEIVER, receiver);
                jsonStringDisvoceryDTOforIntent = jsonStringDiscoveryDTOforPrevState;
            } else {
                jsonStringDisvoceryDTOforIntent = gson.toJson(createHashedData(recStateDTO));
            }
            serviceStarter.putExtra(Constants.KEY_DISCOVERY_DTO, jsonStringDisvoceryDTOforIntent);
            Log.d(TAG, serviceMonitorActions.getServiceStarterString()+" "+triggerEvent.getName()+" start ServiceMonitor");
            ServiceMonitor.enqueueWork(this, serviceStarter);
        } else {
            jsonStringDiscoveryDTOforPrevState = gson.toJson(recStateDTO);
            Log.d(TAG, serviceMonitorActions.getServiceStarterString()+" "+triggerEvent.getName()+" nothing happened");
        }

        if (saveDiscovery) {
            Intent startService = new Intent(this, SaveRecordToServer.class);
            startService.putExtra(Constants.KEY_DISCOVERY_DTO, gson.toJson(createHashedData(recStateDTO)));
            Log.d(TAG, serviceMonitorActions.getServiceStarterString() + " " + triggerEvent.getName() + " start SaveRecordToServer");
            SaveRecordToServer.enqueueWork(this, startService);
            if(isOnlineStatus())
                saveRecordsFromLocalToOnlineParseServer();
        }
        // store recent state as previous stat
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(Constants.KEY_DISCOVERY_DTO,jsonStringDiscoveryDTOforPrevState);
        editor.commit();
        Log.d(TAG, recStateDTO.getRecordID() + " timeDif:" + (recStateDTO.getTimeStamp() - prevStateDTO.getTimeStamp()) + " prevNetworkState:" + prevStateDTO.getNetworkInfo() + " recentNetworkState:" + recStateDTO.getNetworkInfo() + " discoveryJSON:" + PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.KEY_DISCOVERY_DTO,"null"));
        //Log.d(TAG,"Runtime: "+(System.currentTimeMillis()-startTimeStamp));
    }

    private boolean isServiceStartNecessary(DiscoveryDTO prevStateDTO, DiscoveryDTO recStateDTO) {
        boolean isOnline = isOnlineStatus();
        boolean natHasNotBeenDiscoveredLongTimeAgo = System.currentTimeMillis() - recStateDTO.getNatResultsDTO().getLastDiscovery() > Constants.DISCOVERY_START_INTERVAL_ONLINE;
        boolean isTriggerConnectionChanged = recStateDTO.getTriggerCode() == DiscoveryTriggerEvents.CONNECTION_CHANGED.getCode();
        boolean isTriggerBatteryPowerConnected = recStateDTO.getTriggerCode() == DiscoveryTriggerEvents.BATTERY_POWER_CONNECTED.getCode();
        boolean connectionModeHasBeenChanged = recStateDTO.getConnectionMode() != prevStateDTO.getConnectionMode();
        boolean wifiSSIDHasBeenChangedOnWifi = !recStateDTO.getWifiDTO().getSsid().equals(prevStateDTO.getWifiDTO().getSsid()) &&
                recStateDTO.getConnectionMode() == ConnectionType.WIFI.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.WIFI.getCode();
        boolean mobileNetTypeHasBeenChangedOnMobileNetwork = !recStateDTO.getMobileDTO().getNetworkType().equals(prevStateDTO.getMobileDTO().getNetworkType()) &&
                recStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode();
        boolean carrierNameHasBeenChangedAndOnMobileNetwork = !recStateDTO.getMobileDTO().getCarrier().equals(prevStateDTO.getMobileDTO().getCarrier()) &&
                recStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode();
        Log.d(TAG,"isOnline:"+isOnline+" natHasNotBeenDiscoveredLongTimeAgo:"+natHasNotBeenDiscoveredLongTimeAgo+
        " isTriggerConnectionChanged:"+isTriggerConnectionChanged+" isTriggerBatteryPowerConnected:"+isTriggerBatteryPowerConnected+
        " connectionModeHasBeenChanged:"+connectionModeHasBeenChanged+" wifiSSIDHasBeenChangedOnWifi:"+wifiSSIDHasBeenChangedOnWifi+
        " mobileNetTypeHasBeenChangedOnMobileNetwork:"+mobileNetTypeHasBeenChangedOnMobileNetwork+" carrierNameHasBeenChangedAndOnMobileNetwork:"+carrierNameHasBeenChangedAndOnMobileNetwork);
        return isOnline && (natHasNotBeenDiscoveredLongTimeAgo || isTriggerBatteryPowerConnected || isTriggerConnectionChanged
                || connectionModeHasBeenChanged || wifiSSIDHasBeenChangedOnWifi
                || mobileNetTypeHasBeenChangedOnMobileNetwork || carrierNameHasBeenChangedAndOnMobileNetwork);
    }

    private DiscoveryDTO createHashedData(DiscoveryDTO discoveryDTO){
        String  hashedAndroidID, hashedSsid, hashedMacAddress;
        String androidID = discoveryDTO.getAndroidID();
        String macAddress = discoveryDTO.getWifiDTO().getMacAddress();
        String sSid = discoveryDTO.getWifiDTO().getSsid();
        hashedAndroidID = GeneralResource.createHashedId(androidID);
        hashedMacAddress = macAddress.equals(Constants.PREF_STRING_VALUE_EMPTY)?(Constants.PREF_STRING_VALUE_EMPTY):GeneralResource.createHashedId(macAddress);
        hashedSsid = sSid.equals(Constants.PREF_STRING_VALUE_EMPTY)?(Constants.PREF_STRING_VALUE_EMPTY):GeneralResource.createHashedId(sSid);
        discoveryDTO.setAndroidID(hashedAndroidID);
        discoveryDTO.getWifiDTO().setMacAddress(hashedMacAddress);
        discoveryDTO.getWifiDTO().setSsid(hashedSsid);
        return discoveryDTO;
    }

    private void saveRecordsFromLocalToOnlineParseServer(){
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isBackgroundServiceEnable = preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
        boolean isParseServiceEnable = preferences.getBoolean(Constants.PREF_KEY_PARSE_SERVICE, true);
        if (isBackgroundServiceEnable && isParseServiceEnable) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.REMOTE_PARSE_SERVER_TABLE_NAME);
            query.fromLocalDatastore();
            query.ignoreACLs();
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> measurementList, ParseException e) {
                    if (e == null) {
                        for (ParseObject measurement : measurementList) {
                            try {
                                measurement.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Log.d(TAG, "SAVE - I saved it online PARSE database");
                                    }
                                });
                                measurement.unpinInBackground();
                            } catch (Exception ex) {
                                Log.d(TAG, "Error during save in Parse");
                            }
                        }
                    } else {
                        Log.d(TAG, "Error reading from local PARSE datastore"+e.getMessage());
                    }
                }
            });
            query = ParseQuery.getQuery(Constants.FICT_SERVER_LOCAL_PARSE_TABLE_NAME);
            query.fromLocalDatastore();
            query.ignoreACLs();
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> measurementList, ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "measurementList.size:"+ measurementList.size());
                        if(measurementList.size() > 0) {
                            ArrayList<String> discoveryDtoList = new ArrayList<String>();
                            for (ParseObject measurement : measurementList) {
                                discoveryDtoList.add(measurement.getString(Constants.KEY_DISCOVERY_DTO));
                                measurement.unpinInBackground();
                            }
                            Intent startService = new Intent(StateManagerService.this, SaveRecordToServer.class);
                            Log.d(TAG, "OLD_RECORD_TO_FICT_SERVER start SaveRecordToServer "+discoveryDtoList.size());
                            startService.putStringArrayListExtra(Constants.KEY_DISCOVERY_DTO_LIST, discoveryDtoList);
                            SaveRecordToServer.enqueueWork(StateManagerService.this, startService);
                        }
                    } else {
                        Log.d(TAG, "Error reading from local PARSE datastore"+e.getMessage());
                    }
                }
            });
        } else {
            List<String> namesOfTable = new ArrayList<String>();
            namesOfTable.add(Constants.REMOTE_PARSE_SERVER_TABLE_NAME);
            namesOfTable.add(Constants.FICT_SERVER_LOCAL_PARSE_TABLE_NAME);
            for (String name :namesOfTable) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery(name);
                query.fromLocalDatastore();
                query.ignoreACLs();
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> measurementList, ParseException e) {
                        ParseObject.unpinAllInBackground(measurementList);
                    }
                });
            }
            Log.e(TAG, "ERROR, no authority for save records to online database so local is cleared!");
        }
    }

    private void firebaseStateModify(final String type){
        Intent startService = new Intent(this,P2PConnectionStarterService.class);
        startService.putExtra(Constants.KEY_CONNECTION_ID,-2);
        startService.putExtra(Constants.KEY_TYPE,type);
        P2PConnectionStarterService.enqueueWork(this,startService);
    }

    private DiscoveryDTO dicover(DiscoveryDTO prevStateDTO, DiscoveryTriggerEvents triggerEvent) {
        final NetworkInfo recentNetwork = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        DiscoveryDTO recentState = new DiscoveryDTO();
        recentState.setAndroidID(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        recentState.setTimeStamp(System.currentTimeMillis());
        recentState.setTimeZone(TimeZone.getDefault().getOffset(System.currentTimeMillis()) / Constants.MILLISEC_TO_HOURS_RATIO);
        recentState.setAppVersion(BuildConfig.VERSION_CODE);
        recentState.setAndroidVersion(Build.VERSION.SDK_INT);
        recentState.getNatResultsDTO().setDiscoveryResultCode(NatDiscoveryResult.DID_NOT_STARTED);
        recentState.getNatResultsDTO().setLastDiscovery(prevStateDTO.getNatResultsDTO().getLastDiscovery());
        recentState.getWebRTCResultsDTO().setExitStatus(P2PConnectionExitStatus.DID_NOT_STARTED);
        recentState.getWebRTCResultsDTO().setConnectionStart(0L);
        Long recordID = prevStateDTO.getRecordID() != 0L ? prevStateDTO.getRecordID() + 1L : 1L;
        recordID = recordID == Long.MAX_VALUE ? 1L : recordID;
        recentState.setRecordID(recordID);
        MobileNetInfoDTO mobileInfoDTO = new MobileNetInfoDTO();
        mobileInfoDTO.setRoaming(telephonyManager.isNetworkRoaming());
        mobileInfoDTO.setCarrier(telephonyManager.getNetworkOperatorName());
        mobileInfoDTO.setSimCountryIso(telephonyManager.getSimCountryIso());
        mobileInfoDTO.setAirplane(Settings.System.getInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0);
        mobileInfoDTO.setNetworkType(getString(R.string.na));
        mobileInfoDTO.setNetworkCountryIso(telephonyManager.getNetworkCountryIso());
        mobileInfoDTO.setPhoneType(telephonyManager.getPhoneType());
        WifiInfoDTO wifiInfoDTO = new WifiInfoDTO();
        wifiInfoDTO.setState(WifiState.getByCode(wifiManager.getWifiState()));
        if (recentNetwork != null) {
            final int networkType = recentNetwork.getType();
            recentState.setNetworkInfo(DetailedNetworkStatus.getByNetworkInfoDetailedState(recentNetwork.getDetailedState()).getCode());
            if (recentNetwork.isConnected()) {
                if (networkType == ConnectivityManager.TYPE_WIFI) {
                    wifiInfoDTO.setMacAddress(discoverWifiMACAddress(wifiInfo));
                    recentState.setConnectionMode(ConnectionType.WIFI);
                    wifiInfoDTO.setBandwidth(wifiInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
                    wifiInfoDTO.setRssi(wifiInfo.getRssi());
                    wifiInfoDTO.setSsid(wifiInfo.getSSID());
                    recentState.setLocalIP(Parser.parseIp(wifiInfo.getIpAddress()));
                } else if (networkType == ConnectivityManager.TYPE_MOBILE || networkType == ConnectivityManager.TYPE_MOBILE_DUN
                        || networkType == ConnectivityManager.TYPE_MOBILE_HIPRI || networkType == ConnectivityManager.TYPE_MOBILE_MMS
                        || networkType == ConnectivityManager.TYPE_MOBILE_SUPL) {
                    recentState.setConnectionMode(ConnectionType.MOBILE);
                    //if(Build.VERSION.SDK_INT >= 24){  mobileInfoDTO.setNetworkType(MobileNetType.getByCode(telephonyManager.getDataNetworkType()).getName()); } else {
                    mobileInfoDTO.setNetworkType(MobileNetType.getByCode(telephonyManager.getNetworkType()).getName());
                    //Log.d(TAG,recordID+" networtype:"+recentNetwork.getTypeName() +" subtype:"+recentNetwork.getSubtypeName()+ " telephonyManagerNetType:"+mobileInfoDTO.getNetworkType());
                    recentState.setLocalIP(MobileNetworkInfoBuilder.getIPFormatted(this));
                } else {
                    recentState.setConnectionMode(ConnectionType.OTHER);
                }
            } else {
                recentState.setNetworkInfo(DetailedNetworkStatus.DISCONNECTED.getCode());
                recentState.setConnectionMode(ConnectionType.NO_CONNECTION);
            }
        } else {
            recentState.setNetworkInfo(DetailedNetworkStatus.DISCONNECTED.getCode());
            recentState.setConnectionMode(ConnectionType.NO_CONNECTION);
        }
        recentState.setWifiDTO(wifiInfoDTO);
        recentState.setMobileDTO(mobileInfoDTO);
        recentState.setBatteryDTO(discoverBatteryState());
        recentState.setUptimeInfoDTO(discoverUptimeState(prevStateDTO.getUptimeInfoDTO(),triggerEvent));
        Location net_loc = null, gps_loc = null, finalLoc = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (gps_enabled)
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (gps_loc != null && net_loc != null) {
            if (gps_loc.getTime() < net_loc.getTime())
                finalLoc = net_loc;
            else
                finalLoc = gps_loc;
        } else {
            if (gps_loc != null) {
                finalLoc = gps_loc;
            } else if (net_loc != null) {
                finalLoc = net_loc;
            }
        }
        if(finalLoc != null){
            recentState.setLatitude(finalLoc.getLatitude());
            recentState.setLongitude(finalLoc.getLongitude());
            recentState.setLocationCaptureTimestamp(finalLoc.getTime());
        }
        boolean isOnline = isOnlineStatus();
        if (triggerEvent == DiscoveryTriggerEvents.CONNECTION_LOST || (triggerEvent == DiscoveryTriggerEvents.CONNECTION_CHANGED && !isOnline) ) {
            recentState.setLastDisconnect(recentState.getTimeStamp());
        } else if (prevStateDTO.getNetworkInfo() == 5 && !isOnline) {
            recentState.setLastDisconnect(prevStateDTO.getTimeStamp()+1L);
        } else {
            recentState.setLastDisconnect(prevStateDTO.getLastDisconnect());
        }
        if(prevStateDTO.getMobileDTO().isAirplane() != recentState.getMobileDTO().isAirplane()){
            triggerEvent = DiscoveryTriggerEvents.AIRPLANE_MODE_CHANGED;
        }
        if((prevStateDTO.getNetworkInfo() == 5 && !isOnline) || (prevStateDTO.getNetworkInfo() != 5 && isOnline)
                ||  prevStateDTO.getNetworkInfo() != recentState.getNetworkInfo() || recentState.getConnectionMode() != prevStateDTO.getConnectionMode()
                || (!recentState.getWifiDTO().getSsid().equals(prevStateDTO.getWifiDTO().getSsid()) &&
                recentState.getConnectionMode() == ConnectionType.WIFI.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.WIFI.getCode())
                || (!recentState.getMobileDTO().getNetworkType().equals(prevStateDTO.getMobileDTO().getNetworkType()) &&
                recentState.getConnectionMode() == ConnectionType.MOBILE.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode())
                || (!recentState.getMobileDTO().getCarrier().equals(prevStateDTO.getMobileDTO().getCarrier()) &&
                recentState.getConnectionMode() == ConnectionType.MOBILE.getCode() && prevStateDTO.getConnectionMode() == ConnectionType.MOBILE.getCode()) ) {
            triggerEvent = DiscoveryTriggerEvents.CONNECTION_CHANGED;
        }
        boolean prevPowerConnected = isOnCharging(BatteryStatusChargingState.getByCode(prevStateDTO.getBatteryDTO().getChargingState()),BatteryPluggedState.getByCode(prevStateDTO.getBatteryDTO().getPluggedState()));
        boolean recPowerConnected = isOnCharging(BatteryStatusChargingState.getByCode(recentState.getBatteryDTO().getChargingState()),BatteryPluggedState.getByCode(recentState.getBatteryDTO().getPluggedState()));
        if(!prevPowerConnected && recPowerConnected){
            triggerEvent = DiscoveryTriggerEvents.BATTERY_POWER_CONNECTED;
        } else if(prevPowerConnected && !recPowerConnected){
            triggerEvent = DiscoveryTriggerEvents.BATTERY_POWER_DISCONNECTED;
        }
        recentState.setTriggerCode(triggerEvent);
        return recentState;
    }

    private UptimeInfoDTO discoverUptimeState(UptimeInfoDTO prevStateUptimeInfo, DiscoveryTriggerEvents triggerEvent){
        UptimeInfoDTO uptimeInfoDTO = new UptimeInfoDTO();
        uptimeInfoDTO.setUptime(SystemClock.elapsedRealtime());
        if(triggerEvent == DiscoveryTriggerEvents.REBOOT || triggerEvent == DiscoveryTriggerEvents.ACTION_SHUTDOWN){
            uptimeInfoDTO.setShutDownTimestamp(System.currentTimeMillis());
        } else {
            uptimeInfoDTO.setShutDownTimestamp(prevStateUptimeInfo.getShutDownTimestamp());
        }
        if(triggerEvent == DiscoveryTriggerEvents.BOOT_COMPLETED) {
            uptimeInfoDTO.setTurnOnTimestamp(System.currentTimeMillis());
        } else {
            uptimeInfoDTO.setTurnOnTimestamp(prevStateUptimeInfo.getTurnOnTimestamp());
        }
        return uptimeInfoDTO;
    }

    private BatteryInfoDTO discoverBatteryState(){
        BatteryInfoDTO batteryDTO = new BatteryInfoDTO();
        final Bundle extras = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getExtras();
        BatteryStatusChargingState chargingState = BatteryStatusChargingState.getByCode(extras.getInt(BatteryManager.EXTRA_STATUS, -1));
        batteryDTO.setChargingState(chargingState);
        batteryDTO.setPluggedState(BatteryPluggedState.getByCode(extras.getInt(BatteryManager.EXTRA_PLUGGED, -1)));
        batteryDTO.setPercentage((int) (extras.getInt(BatteryManager.EXTRA_LEVEL, -1)
                / (extras.getInt(BatteryManager.EXTRA_SCALE, -1) + 0.0) * Constants.HUNDRED_PERCENT));
        batteryDTO.setHealth(BatteryHealth.getByCode(extras.getInt(BatteryManager.EXTRA_HEALTH, -1)));
        batteryDTO.setTemperature(extras.getInt(BatteryManager.EXTRA_TEMPERATURE, -1));
        batteryDTO.setVoltage(extras.getInt(BatteryManager.EXTRA_VOLTAGE, -1));
        if (extras.containsKey(BatteryManager.EXTRA_PRESENT)) {
            batteryDTO.setPresent(extras.getBoolean(BatteryManager.EXTRA_PRESENT));
        }
        batteryDTO.setTechnology(extras.getString(BatteryManager.EXTRA_TECHNOLOGY));
        return batteryDTO;
    }

    /**
     * @param wifiInfo for getMacAddress() method
     * for newer version: https://stackoverflow.com/questions/11705906/programmatically-getting-the-mac-of-an-android-device
     * @return MAC address string
     */
    private String discoverWifiMACAddress(WifiInfo wifiInfo) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                List<NetworkInterface> list = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterfaces : list) {
                    if (!networkInterfaces.getName().equalsIgnoreCase("wlan0")) continue;
                    byte[] macBytes = networkInterfaces.getHardwareAddress();
                    if (macBytes == null)
                        return "02:00:00:00:00:00";
                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        sb.append(Integer.toHexString(b & 0xFF) + ":");
                    }
                    if (sb.length() > 0)
                        sb.deleteCharAt(sb.length() - 1);
                    return sb.toString();
                }
            } catch (Exception ex) {
                Log.d(TAG, "WIFI MacAddress request error: " + ex.toString());
                return "02:00:00:00:00:00";
            }
            return "02:00:00:00:00:00";
        } else {
            return wifiInfo.getMacAddress();
        }
    }

    @Override
    public void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = preferences.edit();
        boolean isBackgroundServiceEnable = preferences.getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
        if(Build.VERSION.SDK_INT >= 28){
            if(isRestricted()) {
                isBackgroundServiceEnable = false;
                editor.putBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, false);
                editor.commit();
            }
        }
        if(isBackgroundServiceEnable) {
            setRepeating();
        }
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void setRepeating(){
        if (isOnCharging()) {
            if(Build.VERSION.SDK_INT >= Constants.API_LEVEL_JOB_SERVICE){
                ComponentName componentName = new ComponentName(this, StateManagerServiceTriggerJobService.class);
                JobInfo info;
                if(isOnlineStatus()){
                    info = new JobInfo.Builder(1313131342, componentName)
                            .setPersisted(true)
                            .setMinimumLatency(1*Constants.STATE_CHECK_INTERVAL)
                            .setOverrideDeadline(Math.round(1.1*Constants.STATE_CHECK_INTERVAL))
                            .build();

                } else {
                    info = new JobInfo.Builder(1313131342, componentName)
                            .setPersisted(true)
                            .setMinimumLatency(1*Constants.MILLISEC_TO_SECOND_RATIO)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setOverrideDeadline(1*Constants.STATE_CHECK_INTERVAL)
                            .build();
                }
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                int resultCode = scheduler.schedule(info);
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG, "Job scheduled " + info.getId());
                } else {
                    Log.d(TAG, "Job scheduling failed " + info.getId());
                }
            } else {
                final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                final Intent serviceStarter = new Intent(this, StateManagerService.class);
                serviceStarter.setAction(Constants.ACTION_STATE_CHECK);
                final PendingIntent pendingServiceStarter = PendingIntent.getService(this, 0, serviceStarter, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingServiceStarter);
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime()+Constants.STATE_CHECK_INTERVAL,
                        Constants.STATE_CHECK_INTERVAL, pendingServiceStarter);
                Log.d(TAG,"setRepeating is done "+ System.currentTimeMillis());
            }
        } else {
            if (Build.VERSION.SDK_INT >= 26) {
                PersistableBundle bundle = new PersistableBundle();
                bundle.putString(Constants.KEY_STATE,Intent.ACTION_POWER_CONNECTED);
                ComponentName componentName = new ComponentName(this, StateManagerServiceTriggerJobService.class);
                JobInfo info= new JobInfo.Builder(1313131342, componentName)
                        .setPersisted(true)
                        .setRequiresCharging(true)
                        .setMinimumLatency(1*Constants.MILLISEC_TO_SECOND_RATIO)
                        .setExtras(bundle)
                        .build();
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                int resultCode = scheduler.schedule(info);
                if (resultCode == JobScheduler.RESULT_SUCCESS) {
                    Log.d(TAG, "Job scheduled " + info.getId());
                } else {
                    Log.d(TAG, "Job scheduling failed " + info.getId());
                }
            } else if(Build.VERSION.SDK_INT >= Constants.API_LEVEL_JOB_SERVICE){
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancel(1313131342);
            } else {
                final Intent serviceStarter = new Intent(this, StateManagerService.class);
                serviceStarter.setAction(Constants.ACTION_STATE_CHECK);
                final PendingIntent pendingServiceStarter = PendingIntent.getService(this, 0, serviceStarter, PendingIntent.FLAG_UPDATE_CURRENT);
                final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingServiceStarter);
            }
            /***
             *
            Intent serviceStarter = new Intent(StateManagerService.this, ServiceMonitor.class);
            serviceStarter.setAction(ServiceMonitorActions.STOP_EVERYTHING.getServiceStarterString());
            Log.d(TAG, ServiceMonitorActions.STOP_EVERYTHING.getServiceStarterString()+" start ServiceMonitor");
            ServiceMonitor.enqueueWork(this, serviceStarter);
             */
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public boolean isRestricted(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //new, because of an issue
        if (Build.VERSION.SDK_INT >= 28) {
            if(activityManager.isBackgroundRestricted()){
                return true;
            }
        }
        return false;
    }

    public boolean isOnlineStatus(int detailedNetworkInfo) {
        return detailedNetworkInfo == DetailedNetworkStatus.CONNECTED.getCode();
    }


    public boolean isOnlineStatus() {
        NetworkInfo netInfo =  ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOnCharging() {
        final Bundle extras =  registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)).getExtras();
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
