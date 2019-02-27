package hu.uszeged.inf.wlab.stunner.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.google.gson.GsonBuilder;

import hu.uszeged.inf.wlab.stunner.service.handler.P2PConnectionThreadHandler;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;

public class P2PConnectionService extends Service implements P2PConnectionThreadHandler.TestFinishedListener{
    public static String TAG = "P2PConnectionService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int connectionID = intent.getIntExtra(Constants.KEY_CONNECTION_ID,-1);
        String type = intent.getStringExtra(Constants.KEY_TYPE);
        final HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final P2PConnectionThreadHandler handler = new P2PConnectionThreadHandler(thread.getLooper(),this);
        final Message message = handler.obtainMessage();
        final Bundle params = new Bundle();
        if(intent.hasExtra(Constants.KEY_REMOTE_PEER_ID)){
            params.putString(Constants.KEY_REMOTE_PEER_ID, intent.getStringExtra(Constants.KEY_REMOTE_PEER_ID));
        }
        params.putInt(Constants.KEY_CONNECTION_ID,connectionID);
        params.putString(Constants.KEY_TYPE,type);
        if(intent.hasExtra(Constants.KEY_MESSAGE_DATA)){
            params.putString(Constants.KEY_MESSAGE_DATA,intent.getStringExtra(Constants.KEY_MESSAGE_DATA));
        }
        message.obj = params;
        handler.sendMessage(message);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTestFinished(Bundle results) {
        P2PResultsDTO p2pConnectionInfo = results.getParcelable(Constants.KEY_P2P_RESULTS);
        String jsonStringP2PResultsDTO =  new GsonBuilder().create().toJson(p2pConnectionInfo);
        int connectionID = p2pConnectionInfo.getConnectionID();
        Intent serviceStarter = new Intent(this, ServiceMonitor.class);
        serviceStarter.setAction(ServiceMonitorActions.P2P_SERVICE_FINISHED.getServiceStarterString());
        Log.d(TAG,"P2PThreadHandler finished "+connectionID);
        serviceStarter.putExtra(Constants.KEY_P2P_RESULTS, jsonStringP2PResultsDTO);
        serviceStarter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        ServiceMonitor.enqueueWork(this, serviceStarter);
        stopSelf();
    }
}
