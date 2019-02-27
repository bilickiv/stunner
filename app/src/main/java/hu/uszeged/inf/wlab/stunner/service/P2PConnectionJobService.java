package hu.uszeged.inf.wlab.stunner.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.google.gson.GsonBuilder;

import hu.uszeged.inf.wlab.stunner.service.handler.P2PConnectionThreadHandler;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;

@TargetApi(21)
public class P2PConnectionJobService extends JobService implements P2PConnectionThreadHandler.TestFinishedListener {
    public static final String TAG = "P2PConnectionJS";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        String appointedPeer = jobParameters.getExtras().getString(Constants.KEY_REMOTE_PEER_ID,Constants.PREF_STRING_VALUE_EMPTY);
        int connectionID = jobParameters.getExtras().getInt(Constants.KEY_CONNECTION_ID);
        String receivedMessage = jobParameters.getExtras().getString(Constants.KEY_MESSAGE_DATA,Constants.PREF_STRING_VALUE_EMPTY);
        String type = jobParameters.getExtras().getString(Constants.KEY_TYPE,Constants.PREF_STRING_VALUE_EMPTY);
        final HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final P2PConnectionThreadHandler handler = new P2PConnectionThreadHandler(thread.getLooper(),this);
        final Message message = handler.obtainMessage();
        final Bundle params = new Bundle();
        if(!appointedPeer.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
            params.putString(Constants.KEY_REMOTE_PEER_ID, appointedPeer);
        }
        params.putInt(Constants.KEY_CONNECTION_ID,connectionID);
        params.putString(Constants.KEY_TYPE,type);
        if(!receivedMessage.equals(Constants.PREF_STRING_VALUE_EMPTY)){
            params.putString(Constants.KEY_MESSAGE_DATA,receivedMessage);
        }
        params.putParcelable(Constants.KEY_JOB_PARAMS,jobParameters);
        message.obj = params;
        handler.sendMessage(message);
        Log.d(TAG,"P2PConnection Thread is started");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG,"P2PConnection is terminated by Android OS");
        return false;
    }

    @Override
    public void onTestFinished(Bundle results) {
        Log.d(TAG,"P2PConnection is finished");
        P2PResultsDTO p2pConnectionInfo = results.getParcelable(Constants.KEY_P2P_RESULTS);
        String jsonStringP2PResultsDTO =  new GsonBuilder().create().toJson(p2pConnectionInfo);
        int connectionID = p2pConnectionInfo.getConnectionID();
        Intent serviceStarter = new Intent(P2PConnectionJobService.this, ServiceMonitor.class);
        serviceStarter.setAction(ServiceMonitorActions.P2P_SERVICE_FINISHED.getServiceStarterString());
        serviceStarter.putExtra(Constants.KEY_P2P_RESULTS, jsonStringP2PResultsDTO);
        serviceStarter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        ServiceMonitor.enqueueWork(this, serviceStarter);
        JobParameters params = results.getParcelable(Constants.KEY_JOB_PARAMS);
        jobFinished(params,false);
        Log.d(TAG,"JobService is finished");
    }
}
