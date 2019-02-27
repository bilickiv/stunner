package hu.uszeged.inf.wlab.stunner.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Random;

import hu.uszeged.inf.wlab.stunner.R;
import hu.uszeged.inf.wlab.stunner.service.handler.DiscoveryThreadHandler;
import hu.uszeged.inf.wlab.stunner.service.handler.WebRTCThreadHandler;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
//import hu.uszeged.inf.wlab.stunner.utils.GeneralResource;
import hu.uszeged.inf.wlab.stunner.utils.GeneralResource;
import hu.uszeged.inf.wlab.stunner.utils.dtos.DiscoveryDTO;
import hu.uszeged.inf.wlab.stunner.utils.dtos.WebRTCResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;


public class NatDiscoveryAndWebRtcCapabilityService extends JobIntentService implements DiscoveryThreadHandler.TestFinishedListener, WebRTCThreadHandler.TestFinishedListener {
	public static final String TAG = "NatAndWebRtcService";
    /**
     * Unique job ID for this service.
     */
	public static final int JOB_ID = 95522089;

	/**
	 * Convenience method for enqueuing work in to this service.
	 */
	public static void enqueueWork(Context context, Intent work) {
		enqueueWork(context, NatDiscoveryAndWebRtcCapabilityService.class, JOB_ID, work);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleWork(@NonNull Intent intent) {
        int recID = intent.getIntExtra(Constants.KEY_CONNECTION_ID,-1);
        final HandlerThread webRtcThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        webRtcThread.start();
        final WebRTCThreadHandler webRTCThreadHandler = new WebRTCThreadHandler(webRtcThread.getLooper(), this);
        final Message messageForWebRTCHandler = webRTCThreadHandler.obtainMessage();
        final Bundle paramsForWebRTCHandler = new Bundle();
        paramsForWebRTCHandler.putInt(Constants.KEY_ID,recID);
        messageForWebRTCHandler.obj = paramsForWebRTCHandler;
        webRTCThreadHandler.sendMessage(messageForWebRTCHandler);
        Log.d(TAG,"WebRTCThreadHandler started "+webRtcThread.isAlive());
        String jsonStringDiscoveryDTOFromIntent = intent.getStringExtra(Constants.KEY_DISCOVERY_DTO);
        ResultReceiver receiver = null;
        if(intent.hasExtra(Constants.KEY_RECEIVER))
            receiver = intent.getParcelableExtra(Constants.KEY_RECEIVER);
        GsonBuilder builder = new GsonBuilder();//.setPrettyPrinting();
        Gson gson = builder.create();
        DiscoveryDTO discovery = gson.fromJson(jsonStringDiscoveryDTOFromIntent, DiscoveryDTO.class);
        Log.d(TAG,"NAT service start "+discovery.getRecordID());
        final Bundle params = new Bundle();
        params.putLong(Constants.KEY_START_ID, discovery.getRecordID());
        /* randomize STUN test parameters */
        final int randomNum = new Random().nextInt(getResources().getStringArray(R.array.servers).length - 0) + 0;
        final String stunServer = getResources().getStringArray(R.array.servers)[randomNum];
        final int port = getResources().getIntArray(R.array.ports)[randomNum];
        params.putString(Constants.KEY_SERVER_ADDRESS, stunServer);
        params.putInt(Constants.KEY_SERVER_PORT, port);
        params.putString(Constants.KEY_IP_ADDRESS, discovery.getLocalIP());
        params.putInt(Constants.KEY_ID,recID);
        if(receiver != null)
            receiver.send(Constants.RESULT_CONNECTION_START, params);
        final HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final DiscoveryThreadHandler handler = new DiscoveryThreadHandler(thread.getLooper(), receiver, this);
        params.putParcelable(Constants.KEY_DATA, discovery);
        /* send message to stunner test thread via its handler */
        final Message message = handler.obtainMessage();
        message.obj = params;
        handler.sendMessage(message);
        Log.d(TAG,"NAT discovery is started. ID:"+discovery.getRecordID());
	}

	@Override
	public void onTestFinished(Bundle results) {
        GsonBuilder builder = new GsonBuilder();//.setPrettyPrinting();
        Gson gson = builder.create();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NatDiscoveryAndWebRtcCapabilityService.this);
        SharedPreferences.Editor editor  = preferences.edit();
        int connectionID = results.getInt(Constants.KEY_ID);
        if (results.containsKey(Constants.KEY_WEBRTC_RESULTS)){
            WebRTCResultsDTO webRtcResultDTO = results.getParcelable(Constants.KEY_WEBRTC_RESULTS);
            String finishedNatDiscoveryResultsJson = preferences.getString(Constants.KEY_DISCOVERY_DTO+connectionID,Constants.PREF_STRING_VALUE_EMPTY);
            if(!finishedNatDiscoveryResultsJson.equals(Constants.PREF_STRING_VALUE_EMPTY)){
                DiscoveryDTO discoveryDTO = gson.fromJson(finishedNatDiscoveryResultsJson,DiscoveryDTO.class);
                String jsonStringResults = mergeWebRtcAndNatResults(discoveryDTO,webRtcResultDTO,gson);
                sendResultsToServiceMonitor(jsonStringResults,connectionID);
                editor.remove(Constants.KEY_DISCOVERY_DTO+connectionID);
            } else {
                editor.putString(Constants.KEY_WEBRTC_RESULTS+connectionID,gson.toJson(webRtcResultDTO));
            }
        } else {
            DiscoveryDTO discoveryDTO = results.getParcelable(Constants.KEY_DATA);
            final Bundle natResultBundle = new Bundle();
            natResultBundle.putParcelable(Constants.KEY_DATA, discoveryDTO);
            if (results.containsKey(Constants.KEY_RECEIVER)) {
                final ResultReceiver receiver = results.getParcelable(Constants.KEY_RECEIVER);
                receiver.send(Constants.RESULT_STUN_OK, natResultBundle);
                discoveryDTO = createHashedData(discoveryDTO);
            }
            String finishedWebRtcTestResultsJson = preferences.getString(Constants.KEY_WEBRTC_RESULTS+connectionID,Constants.PREF_STRING_VALUE_EMPTY);
            if(!finishedWebRtcTestResultsJson.equals(Constants.PREF_STRING_VALUE_EMPTY)){
                WebRTCResultsDTO webRtcResultDTO = gson.fromJson(finishedWebRtcTestResultsJson,WebRTCResultsDTO.class);
                String jsonStringResults = mergeWebRtcAndNatResults(discoveryDTO,webRtcResultDTO,gson);
                sendResultsToServiceMonitor(jsonStringResults,connectionID);
                editor.remove(Constants.KEY_WEBRTC_RESULTS+connectionID);
            } else {
                editor.putString(Constants.KEY_DISCOVERY_DTO+connectionID,gson.toJson(discoveryDTO));
            }
        }
        editor.commit();
	}

    private void sendResultsToServiceMonitor(String jsonStringResults, int connectionID) {
        Intent serviceStarter = new Intent(NatDiscoveryAndWebRtcCapabilityService.this, ServiceMonitor.class);
        serviceStarter.setAction(ServiceMonitorActions.NAT_DISCOVERY_AND_WEBRTC_TEST_SERVICE_FINISHED.getServiceStarterString());
        serviceStarter.putExtra(Constants.KEY_DISCOVERY_DTO, jsonStringResults);
        serviceStarter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        ServiceMonitor.enqueueWork(this, serviceStarter);
    }

    private String mergeWebRtcAndNatResults(DiscoveryDTO discoveryDTO, WebRTCResultsDTO webRtcResultDTO, Gson gson) {
	    discoveryDTO.setWebRTCResultsDTO(webRtcResultDTO);
	    return gson.toJson(discoveryDTO);
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
}