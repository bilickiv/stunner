package hu.uszeged.inf.wlab.stunner.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryPluggedState;
import hu.uszeged.inf.wlab.stunner.utils.enums.BatteryStatusChargingState;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PServiceState;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;

public class ServiceMonitor extends JobIntentService {
    public static final String TAG = "ServiceMonitor";
    public static final String INTENT_FILTER = "SDP_FILTER";
    /**
     * Unique job ID for this service.
     */
    public static final int JOB_ID = 48699594;
    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ServiceMonitor.class, JOB_ID, work);
    }
    private static final String P2P_SERVICE_STATE = "P2PServiceState";
    private static final String P2P_SERVICE_STACK_FOR_PRE_DISCOVERY = "P2PServiceStackForPreDiscovery";
    private static final String P2P_SERVICE_STACK_FOR_NAT_AND_WEBRTC_RESULT_DISCOVERY_DTO = "P2PServiceStackForNatAndWebRtcResultDiscoveryDTO";
    private static final String P2P_SERVICE_STACK_FOR_P2P_RESULTS = "P2PServiceStackForP2PResults";
    private static final String P2P_REC_CONNECTION_ID = "P2PRecConnectionID";
    private static final String P2P_START_TIMESTAMP = "P2PStartTimestamp";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ServiceMonitorActions serviceMonitorActions = ServiceMonitorActions.getByServiceStarterString(intent.getAction());
        String discoveryDTOJsonStringFromIntent = Constants.PREF_STRING_VALUE_EMPTY;
        if(intent.hasExtra(Constants.KEY_DISCOVERY_DTO))
            discoveryDTOJsonStringFromIntent = intent.getStringExtra(Constants.KEY_DISCOVERY_DTO);
        String p2pResultsJsonStringFromIntent = Constants.PREF_STRING_VALUE_EMPTY;
        if(intent.hasExtra(Constants.KEY_P2P_RESULTS))
            p2pResultsJsonStringFromIntent =  intent.getStringExtra(Constants.KEY_P2P_RESULTS);
        Random r = new Random();
        int connectionIDFromIntent = -1;
        if(intent.hasExtra(Constants.KEY_CONNECTION_ID)){
            connectionIDFromIntent = intent.getIntExtra(Constants.KEY_CONNECTION_ID,-1);
        }
        String remoteUserID = Constants.PREF_STRING_VALUE_EMPTY;
        String messageData = Constants.PREF_STRING_VALUE_EMPTY;
        String messageType = Constants.PREF_STRING_VALUE_EMPTY;
        int connectionIdFromMessage = -1;
        if(intent.hasExtra(Constants.KEY_REMOTE_PEER_ID) && intent.hasExtra(Constants.KEY_MESSAGE_DATA) && intent.hasExtra(Constants.KEY_TYPE)){
            remoteUserID = intent.getExtras().getString(Constants.KEY_REMOTE_PEER_ID);
            messageType = intent.getStringExtra(Constants.KEY_TYPE);
            messageData = intent.getExtras().getString(Constants.KEY_MESSAGE_DATA);
            connectionIdFromMessage = getConnectionIdFromMessage(messageData);
        }
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceMonitor.this);
        SharedPreferences.Editor editor = preferences.edit();
        Log.d(TAG,"start serviceMonitorActions:"+ serviceMonitorActions.getServiceStarterString());
        String p2pServiceState = preferences.getString(P2P_SERVICE_STATE,P2PServiceState.IDLE.getServiceStateString());
        String p2pPreDiscovery = preferences.getString(P2P_SERVICE_STACK_FOR_PRE_DISCOVERY,Constants.PREF_STRING_VALUE_EMPTY);
        String p2pDiscoveryDTOFromSharedPref = preferences.getString(P2P_SERVICE_STACK_FOR_NAT_AND_WEBRTC_RESULT_DISCOVERY_DTO,Constants.PREF_STRING_VALUE_EMPTY);
        String p2pResultsFromSharedPref = preferences.getString(P2P_SERVICE_STACK_FOR_P2P_RESULTS,Constants.PREF_STRING_VALUE_EMPTY);
        int connectionIdFromSharedPref = preferences.getInt(P2P_REC_CONNECTION_ID,-1);
        Long p2pStartTimestamp = preferences.getLong(P2P_START_TIMESTAMP,0L);
        int recentlyGeneratedConnectionID = r.nextInt(Integer.MAX_VALUE);
        boolean consistenceState = true;
        if(!checkServiceStateConsistency(p2pServiceState,p2pPreDiscovery,p2pDiscoveryDTOFromSharedPref,p2pResultsFromSharedPref)){
            Log.e(TAG,"ERROR: inconsistent SharedPreferences state!");
            editorClear(editor);
            consistenceState = false;
        }
        boolean isFirstP2PTimeoutHappened = intent.getBooleanExtra(Constants.KEY_IS_FIRST_P2P_TIMEOUT_HAPPENED_BOOLEAN,false);
        if(isP2PTimeout(p2pStartTimestamp,connectionIdFromSharedPref,connectionIDFromIntent,connectionIdFromMessage)){
            if(!isFirstP2PTimeoutHappened) {
                Intent broadcastIntent = new Intent(INTENT_FILTER);
                broadcastIntent.putExtra(Constants.KEY_REMOTE_PEER_ID, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                broadcastIntent.putExtra(Constants.KEY_TYPE, Constants.KEY_CLOSE_CONNECTION);
                broadcastIntent.putExtra(Constants.KEY_MESSAGE_DATA, Constants.PREF_STRING_VALUE_EMPTY);
                sendBroadcast(broadcastIntent);
                isFirstP2PTimeoutHappened = true;
                serviceMonitorActions = ServiceMonitorActions.SERVICE_MONITOR_RESTART_IS_REQUIRED;
            } else {
                Log.d(TAG, "SERVICE_FINISHING_TIMEOUT p2pServiceState: " + p2pServiceState + " consistenceState:" + consistenceState + " p2pDiscoveryDTOFromSharedPref: " + p2pDiscoveryDTOFromSharedPref);
                if (p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString()) && consistenceState) {
                    P2PResultsDTO emptyP2PResults = new P2PResultsDTO();
                    emptyP2PResults.setAndroidID(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                    emptyP2PResults.setExitStatus(P2PConnectionExitStatus.CONNECTION_TIMED_OUT.getCode());
                    String jsonStringP2PResults = new GsonBuilder().create().toJson(emptyP2PResults);
                    if (p2pDiscoveryDTOFromSharedPref.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
                        saveMeasurementResults(p2pPreDiscovery, jsonStringP2PResults);
                    } else {
                        saveMeasurementResults(p2pDiscoveryDTOFromSharedPref, jsonStringP2PResults);
                    }
                } else if (p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS.getServiceStateString())
                        && consistenceState) {
                    saveMeasurementResults(p2pPreDiscovery, p2pResultsFromSharedPref);
                }
                editorClear(editor);
                p2pServiceState = P2PServiceState.IDLE.getServiceStateString();
                p2pPreDiscovery = Constants.PREF_STRING_VALUE_EMPTY;
                p2pDiscoveryDTOFromSharedPref = Constants.PREF_STRING_VALUE_EMPTY;
                p2pResultsFromSharedPref = Constants.PREF_STRING_VALUE_EMPTY;
                connectionIdFromSharedPref = -1;
                p2pStartTimestamp = 0L;
                consistenceState = checkServiceStateConsistency(p2pServiceState, p2pPreDiscovery, p2pDiscoveryDTOFromSharedPref, p2pResultsFromSharedPref);
            }
        }
        Log.d(TAG,"connectionIdFromSharedPref:"+connectionIdFromSharedPref+" connectionIDFromIntent:"+connectionIDFromIntent+" connectionIdFromMessage:"+connectionIdFromMessage+" remoteUserID:"+remoteUserID + " messageType:"+messageType);
        switch (serviceMonitorActions) {
            case USER_TRIGGERED_SERVICE_START :
                Log.d(TAG,"USER_TRIGGERED_SERVICE_START: " + p2pServiceState);
                ResultReceiver receiver = intent.getParcelableExtra(Constants.KEY_RECEIVER);
                startNatDiscoveryAndWebRtcTest(discoveryDTOJsonStringFromIntent,recentlyGeneratedConnectionID,receiver);
                break;
            case FIREBASE_MESSAGE_IS_RECEIVED:
                Log.d(TAG,"FIREBASE_MESSAGE_IS_RECEIVED p2pServiceState: " +p2pServiceState + " consistenceState:" + consistenceState);
                if( p2pServiceState.equals(P2PServiceState.IDLE.getServiceStateString()) &&
                        isOnCharging() && messageType.equals(Constants.KEY_OFFER) ) {
                    Log.d(TAG,"remote offer in service monitor");
                    startP2PConnectionStarterService(remoteUserID,connectionIdFromMessage, messageType, messageData);
                    editor.putString(P2P_SERVICE_STATE,P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString());
                    editor.putString(P2P_SERVICE_STACK_FOR_PRE_DISCOVERY,discoveryDTOJsonStringFromIntent);
                    editor.putInt(P2P_REC_CONNECTION_ID,connectionIdFromMessage);
                    editor.putLong(P2P_START_TIMESTAMP,System.currentTimeMillis());
                    startNatDiscoveryAndWebRtcTest(discoveryDTOJsonStringFromIntent,connectionIdFromMessage);
                } else if(p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString())
                        && connectionIdFromSharedPref == connectionIdFromMessage && consistenceState ) {
                    Log.d(TAG,"remote answer in service monitor");
                    Intent broadcastIntent = new Intent(INTENT_FILTER);
                    broadcastIntent.putExtra(Constants.KEY_REMOTE_PEER_ID, remoteUserID);
                    broadcastIntent.putExtra(Constants.KEY_TYPE, messageType);
                    broadcastIntent.putExtra(Constants.KEY_MESSAGE_DATA, messageData);
                    sendBroadcast(broadcastIntent);
                } else if (!messageType.equals(Constants.KEY_REJECT)) {
                    Log.d(TAG,"reject in service monitor");
                    sendNotificationReject(remoteUserID,connectionIdFromMessage);
                } else {
                    Log.d(TAG,"DROP from:"+remoteUserID+" type:"+messageType);
                }
                break;
            case BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START:
                Log.d(TAG,"BACKGROUND_NAT_DISCOVERY_WEBRTC_TEST_AND_P2P_SERVICE_START: " + p2pServiceState);
                if( p2pServiceState.equals(P2PServiceState.IDLE.getServiceStateString()) &&
                        isOnCharging() && consistenceState) {
                    Log.d(TAG,"Background P2P will start");
                    startP2PConnectionStarterService(recentlyGeneratedConnectionID,Constants.KEY_START);
                    editor.putString(P2P_SERVICE_STACK_FOR_PRE_DISCOVERY,discoveryDTOJsonStringFromIntent);
                    editor.putString(P2P_SERVICE_STATE,P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString());
                    editor.putInt(P2P_REC_CONNECTION_ID,recentlyGeneratedConnectionID);
                    editor.putLong(P2P_START_TIMESTAMP,System.currentTimeMillis());
                }
                startNatDiscoveryAndWebRtcTest(discoveryDTOJsonStringFromIntent,recentlyGeneratedConnectionID);
                break;
            case NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED:
                Log.d(TAG,"NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED p2pServiceState: " +p2pServiceState + " consistenceState:" + consistenceState + " p2pDiscoveryDTOFromSharedPref: "+p2pDiscoveryDTOFromSharedPref);
                if( p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString())
                        && p2pDiscoveryDTOFromSharedPref.equals(Constants.PREF_STRING_VALUE_EMPTY)
                        && connectionIdFromSharedPref == connectionIDFromIntent && consistenceState) {
                    editor.putString(P2P_SERVICE_STACK_FOR_NAT_AND_WEBRTC_RESULT_DISCOVERY_DTO,discoveryDTOJsonStringFromIntent);
                } else if( p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS.getServiceStateString())
                        && connectionIdFromSharedPref == connectionIDFromIntent && consistenceState ) {
                    editorClear(editor);
                    saveMeasurementResults(discoveryDTOJsonStringFromIntent,p2pResultsFromSharedPref);
                } else {
                    saveDiscovery(discoveryDTOJsonStringFromIntent);
                }
                break;
            case P2P_SERVICE_FINISHED:
                Log.d(TAG,"P2P_SERVICE_FINISHED p2pServiceState: " +p2pServiceState + " consistenceState:" + consistenceState + " p2pDiscoveryDTOFromSharedPref: "+p2pDiscoveryDTOFromSharedPref);
                if(p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString())
                        && connectionIdFromSharedPref == connectionIDFromIntent && consistenceState){
                    if(p2pDiscoveryDTOFromSharedPref.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
                       editor.putString(P2P_SERVICE_STATE, P2PServiceState.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS.getServiceStateString());
                       editor.putString(P2P_SERVICE_STACK_FOR_P2P_RESULTS, p2pResultsJsonStringFromIntent);
                    } else {
                       editorClear(editor);
                       saveMeasurementResults(p2pDiscoveryDTOFromSharedPref, p2pResultsJsonStringFromIntent);
                    }
                } else {
                    Log.e(TAG,"ERROR: inconsistent SharedPreferences state!");
                    editorClear(editor);
                }
                break;
            case STOP_EVERYTHING:
                break;
            case FIREBASE_UPDATE_STATE:
                if(p2pServiceState.equals(P2PServiceState.IDLE.getServiceStateString())) {
                    startP2PConnectionStarterService(Constants.KEY_UPDATE_STATE);
                }
                break;
            case SERVICE_MONITOR_RESTART_IS_REQUIRED:
                try {
                    Thread.sleep(10*Constants.MILLISEC_TO_SECOND_RATIO);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isFirstP2PTimeoutHappened)
                    intent.putExtra(Constants.KEY_IS_FIRST_P2P_TIMEOUT_HAPPENED_BOOLEAN,isFirstP2PTimeoutHappened);
                ServiceMonitor.enqueueWork(this, intent);
                break;
            default :
                Log.e(TAG,"ERROR "+ serviceMonitorActions.getServiceStarterString()+" action should never happening!");
        }
        editor.commit();
    }


    private boolean isP2PTimeout(Long p2pStartTimestamp, int connectionIdFromSharedPref, int connectionIDFromIntent, int connectionIDFromMessage) {
        if(p2pStartTimestamp == 0L) {
            return false;
        }
        if(connectionIDFromMessage == connectionIdFromSharedPref && connectionIDFromMessage != -1){
            return false;
        }
        if(connectionIDFromIntent == connectionIdFromSharedPref && connectionIDFromIntent != -1){
            return false;
        }
        if(System.currentTimeMillis()-p2pStartTimestamp > Constants.P2P_WAIT_INTERVAL_ONLINE){
            return true;
        }
        return false;
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

    private void startNatDiscoveryAndWebRtcTest(String jsonStringDiscoveryDTO, int connectionID){
        startNatDiscoveryAndWebRtcTest(jsonStringDiscoveryDTO,connectionID,null);
    }

    private void startNatDiscoveryAndWebRtcTest(String jsonStringDiscoveryDTO, int connectionID, ResultReceiver receiver) {
        Intent serviceStarter = new Intent(ServiceMonitor.this, NatDiscoveryAndWebRtcCapabilityService.class);
        serviceStarter.putExtra(Constants.KEY_DISCOVERY_DTO, jsonStringDiscoveryDTO);
        serviceStarter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        if(receiver != null)
            serviceStarter.putExtra(Constants.KEY_RECEIVER, receiver);
        NatDiscoveryAndWebRtcCapabilityService.enqueueWork(this, serviceStarter);
    }

    private void editorClear(SharedPreferences.Editor editor) {
        editor.putString(P2P_SERVICE_STATE,P2PServiceState.IDLE.getServiceStateString());
        editor.remove(P2P_SERVICE_STACK_FOR_PRE_DISCOVERY);
        editor.remove(P2P_SERVICE_STACK_FOR_NAT_AND_WEBRTC_RESULT_DISCOVERY_DTO);
        editor.remove(P2P_SERVICE_STACK_FOR_P2P_RESULTS);
        editor.remove(P2P_REC_CONNECTION_ID);
        editor.remove(P2P_START_TIMESTAMP);
    }

    private boolean checkServiceStateConsistency(String p2pServiceState, String p2pPreDiscovery, String discoveryDTOForP2P, String p2pResults) {
        if(p2pServiceState.equals(P2PServiceState.IDLE.getServiceStateString()) && p2pResults.equals(Constants.PREF_STRING_VALUE_EMPTY) &&
                discoveryDTOForP2P.equals(Constants.PREF_STRING_VALUE_EMPTY) && p2pPreDiscovery.equals(Constants.PREF_STRING_VALUE_EMPTY) )
            return true;
        if(p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_STARTED.getServiceStateString())
                && !p2pPreDiscovery.equals(Constants.PREF_STRING_VALUE_EMPTY) && p2pResults.equals(Constants.PREF_STRING_VALUE_EMPTY) )
            return true;
        if(p2pServiceState.equals(P2PServiceState.HAVE_ALREADY_FINISHED_BUT_WAITING_FOR_OTHER_SERVICES_RESULTS.getServiceStateString())
                && !p2pPreDiscovery.equals(Constants.PREF_STRING_VALUE_EMPTY) && !p2pResults.equals(Constants.PREF_STRING_VALUE_EMPTY)
                && discoveryDTOForP2P.equals(Constants.PREF_STRING_VALUE_EMPTY))
            return true;
        return false;
    }

    private void saveMeasurementResults(String jsonStringDiscoveryDTO, String jsonStringP2PResults) {
        boolean isBackgroundServiceEnable = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
        if (isBackgroundServiceEnable) {
            startP2PConnectionStarterService(Constants.KEY_ENABLE_P2P);
            Intent startService = new Intent(this, SaveRecordToServer.class);
            startService.putExtra(Constants.KEY_DISCOVERY_DTO, jsonStringDiscoveryDTO);
            startService.putExtra(Constants.KEY_P2P_RESULTS, jsonStringP2PResults);
            SaveRecordToServer.enqueueWork(this, startService);
        }
    }


    private void saveDiscovery(String jsonStringDiscoveryDTO) {
        boolean isBackgroundServiceEnable = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.PREF_KEY_BACKGROUND_SERVICE, true);
        if (isBackgroundServiceEnable) {
            Intent startService = new Intent(this,SaveRecordToServer.class);
            startService.putExtra(Constants.KEY_DISCOVERY_DTO,jsonStringDiscoveryDTO);
            SaveRecordToServer.enqueueWork(this, startService);
        }
    }

    private void startP2PConnectionStarterService(final String type){
        startP2PConnectionStarterService(Constants.PREF_STRING_VALUE_EMPTY,-1,type,Constants.PREF_STRING_VALUE_EMPTY);
    }

    private void startP2PConnectionStarterService(final int connectionID, final String type){
        startP2PConnectionStarterService(Constants.PREF_STRING_VALUE_EMPTY,connectionID,type,Constants.PREF_STRING_VALUE_EMPTY);
    }

    private void startP2PConnectionStarterService(final String remotePeerID, final int connectionID, final String type, final String messageData){
        final Intent starter = new Intent(ServiceMonitor.this, P2PConnectionStarterService.class);
        starter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        starter.putExtra(Constants.KEY_TYPE, type);
        if(!messageData.equals(Constants.PREF_STRING_VALUE_EMPTY)){
            starter.putExtra(Constants.KEY_REMOTE_PEER_ID, remotePeerID);
            starter.putExtra(Constants.KEY_MESSAGE_DATA, messageData);
        }
        P2PConnectionStarterService.enqueueWork(this, starter);
        Log.d("Start", "P2PService was started");
    }

    private int getConnectionIdFromMessage(final String message){
        int incomingConnectionID = -1;
        try {
            JSONObject json = new JSONObject(message);
            incomingConnectionID = json.getInt("connectionID");
        } catch (Exception e) {
            Log.d(TAG, "WARNING - The intent not contains info about connection ID");
        }
        return incomingConnectionID;
    }

    private void sendNotificationReject(String peerID,int connectionID){
        try {
            Log.d(TAG, "SEND_NOTIFICATION_REJECT");
            JSONObject json = new JSONObject();
            json.put("connectionID", connectionID);
            String message = json.toString();
            sendNotification(Constants.KEY_REJECT, peerID, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(final String type, final String peerID, final String message) {
        String dataTable = Constants.VALUE_NOTIFICATIONS_TABLE;
        final DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, " sendNotification type:" + type + " message:" + message + " mRootRef:" + String.valueOf(mRootRef));
        DatabaseReference newNotificationref = mRootRef.child(dataTable).child(peerID).push();
        String newNotificationId = newNotificationref.getKey();
        HashMap<String, String> notificationData = new HashMap<>();
        notificationData.put("fromWho", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        notificationData.put("type", type);
        notificationData.put("data", message);
        Map requestMap = new HashMap();
        requestMap.put(dataTable+"/" + peerID + "/" + newNotificationId, notificationData);
        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d(TAG, "Notification send is completed! notificationId:" + databaseReference.getKey() + " databaseReference:" + databaseReference.toString());
                //Log.d(TAG, "BLOCK THE FURTHER CONNECTIONS UNTIL DESTROY");
                if (databaseError != null) {
                    Log.d("DATABASE_ERROR", "There was some error in sending request" + databaseError.toString());
                }
            }
        });
    }
}
