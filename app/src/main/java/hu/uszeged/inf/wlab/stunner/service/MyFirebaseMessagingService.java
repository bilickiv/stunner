package hu.uszeged.inf.wlab.stunner.service;

/**
 * Created by teglaskrisztian on 2018. 04. 11..
 */

import android.content.Intent;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.Arrays;
import java.util.Collections;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = "FCMService";
    public MyFirebaseMessagingService() { }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, String.valueOf(remoteMessage));

        if(remoteMessage.getData().get(Constants.KEY_TYPE) != null) {
            String peerID = remoteMessage.getData().get(Constants.KEY_FIREBASE_MESSAGE_FROM_WHO);
            String type = remoteMessage.getData().get(Constants.KEY_TYPE);
            String data = remoteMessage.getData().get(Constants.KEY_DATA);
            Log.d(TAG, "User_ID: " + peerID + " Type: " + type + " Data: " + data);
            final Intent serviceStarter = new Intent(MyFirebaseMessagingService.this, StateManagerService.class);
            serviceStarter.setAction(Constants.ACTION_FIREBASE_MESSAGE_IS_RECEIVED);
            serviceStarter.putExtra(Constants.KEY_REMOTE_PEER_ID, peerID);
            serviceStarter.putExtra(Constants.KEY_TYPE, type);
            serviceStarter.putExtra(Constants.KEY_MESSAGE_DATA, data);
            StateManagerService.enqueueWork(this,serviceStarter);
        } else {
            Log.e(TAG,"NullPointerException: Firebase message type is null "+Arrays.toString(remoteMessage.getData().keySet().toArray())+" | "+Arrays.toString(remoteMessage.getData().values().toArray()) );
        }
    }

    @Override
    public void onDeletedMessages(){
        Log.d(TAG, "onDeletedMessages()");
    }

    @Override
    public void onMessageSent(String s) {
        Log.d(TAG, "onMessageSent() "+s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        Log.d(TAG, "onSendError() " +e.toString() + " message:"+s);
    }
}
