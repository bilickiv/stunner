package hu.uszeged.inf.wlab.stunner.service;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.GsonBuilder;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;
import hu.uszeged.inf.wlab.stunner.utils.enums.ServiceMonitorActions;

/**
 * Created by sten97 on 2018. 10. 07..
 */

public class P2PConnectionStarterService extends JobIntentService {
    public static final String TAG = "P2PConStarterService";

    public static final int JOB_ID = 93263625;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, P2PConnectionStarterService.class, JOB_ID, work);
    }
    private boolean isConnectedToFireBaseDatabase = false;
    private boolean isWaitingForSomething = true;
    private Long startTimeStamp = System.currentTimeMillis();

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        isConnectedToFireBaseDatabase = false;
        isWaitingForSomething = true;
        int connectionID = intent.getIntExtra(Constants.KEY_CONNECTION_ID,-1);
        String type = intent.getStringExtra(Constants.KEY_TYPE);
        switch (type) {
            case Constants.KEY_OFFER:
            case Constants.KEY_START:
            case Constants.KEY_LOGIN:
                loginGetTokenCheckFirebaseDataBaseConnectionAndSetInitialFields(connectionID,type);
                break;
            case Constants.KEY_UPDATE_STATE:
            case Constants.KEY_ENABLE_P2P:
                setOrUpdateRecordInPeerCandidates();
                isConnectedToFireBaseDatabase = true;
                isWaitingForSomething = false;
                break;
            case Constants.KEY_CLOSE_CONNECTION:
                closeFirebaseConnection();
                isConnectedToFireBaseDatabase = true;
                isWaitingForSomething = false;
                break;
            default:
                Log.e(TAG, "Unknown type ERROR: "+type);
                break;
        }
        startTimeStamp = System.currentTimeMillis();
        P2PConnectionExitStatus exitStatus = P2PConnectionExitStatus.UNKNOWN;
        while(isWaitingForSomething){
            try {
                Thread.sleep(1 * Constants.MILLISEC_TO_SECOND_RATIO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(System.currentTimeMillis()-startTimeStamp > 20*Constants.MILLISEC_TO_SECOND_RATIO) {
                exitStatus = P2PConnectionExitStatus.CONNECTION_TIMED_OUT;
                break;
            }
        }
        if(exitStatus.getCode() == P2PConnectionExitStatus.UNKNOWN.getCode() && !isConnectedToFireBaseDatabase){
            exitStatus = P2PConnectionExitStatus.FIREBASE_CONNECTION_ERROR;
        }
        Log.d(TAG,"login is completed");
        if(isConnectedToFireBaseDatabase) {
            switch (type) {
                case Constants.KEY_OFFER:
                    startP2PConnection(intent.getStringExtra(Constants.KEY_REMOTE_PEER_ID), type, connectionID, intent.getStringExtra(Constants.KEY_MESSAGE_DATA));
                    break;
                case Constants.KEY_START:
                    startP2PConnection(Constants.PREF_STRING_VALUE_EMPTY, type, connectionID, Constants.PREF_STRING_VALUE_EMPTY);
                    break;
                default:
                    break;
            }
        }
        if(exitStatus.getCode() != P2PConnectionExitStatus.UNKNOWN.getCode()){
            Log.d(TAG,"Firebase connection is not succeed "+exitStatus + " " +isConnectedToFireBaseDatabase);
            p2pServiceFinishedWithError(new P2PResultsDTO(),exitStatus.getCode(),connectionID,type);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startP2PConnection(String remotePeerID, String type, int connectionID, String receivedMessage){
        removeRecordFromPeerCandidates();
        if(Build.VERSION.SDK_INT>=Constants.API_LEVEL_JOB_SERVICE){
            startP2PConnectionJobService(remotePeerID, type, connectionID, receivedMessage);
        } else {
            startP2PConnectionService(remotePeerID, type, connectionID, receivedMessage);
        }
    }

    public void startP2PConnectionService(String remotePeerID, String type, int connectionID, String receivedMessage){
        final Intent starter = new Intent(P2PConnectionStarterService.this, P2PConnectionService.class);
        if(!remotePeerID.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
            starter.putExtra(Constants.KEY_REMOTE_PEER_ID, remotePeerID);
        }
        starter.putExtra(Constants.KEY_CONNECTION_ID,connectionID);
        starter.putExtra(Constants.KEY_TYPE,type);
        if(!receivedMessage.equals(Constants.PREF_STRING_VALUE_EMPTY) && receivedMessage != null){
            starter.putExtra(Constants.KEY_MESSAGE_DATA,receivedMessage);
        }
        startService(starter);
    }

    @TargetApi(Constants.API_LEVEL_JOB_SERVICE)
    public void startP2PConnectionJobService(String remotePeerID, String type, int connectionID, String receivedMessage){
        PersistableBundle bundle = new PersistableBundle();
        bundle.putString(Constants.KEY_REMOTE_PEER_ID,remotePeerID);
        bundle.putInt(Constants.KEY_CONNECTION_ID,connectionID);
        bundle.putString(Constants.KEY_TYPE,type);
        if(!receivedMessage.equals(Constants.PREF_STRING_VALUE_EMPTY) && receivedMessage != null){
            bundle.putString(Constants.KEY_MESSAGE_DATA,receivedMessage);
        }
        ComponentName componentName = new ComponentName(this, P2PConnectionJobService.class);
        JobInfo info = new JobInfo.Builder(269345811, componentName)
                .setPersisted(true)
                .setMinimumLatency(1)
                .setOverrideDeadline(1)
                .setExtras(bundle)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job scheduled " + info.getId());
        } else {
            Log.d(TAG, "Job scheduling failed " + info.getId());
        }
    }

    private void loginGetTokenCheckFirebaseDataBaseConnectionAndSetInitialFields(final int connectionID, final String type){
        startTimeStamp = System.currentTimeMillis();
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Log.d(TAG, "P2PConnection signInAnonymously complete, start:"+type);
                        getTokenAndContinueLogin(connectionID,type);
                    } else {
                        p2pServiceFinishedWithError(new P2PResultsDTO(),P2PConnectionExitStatus.FIREBASE_CONNECTION_ERROR.getCode(),connectionID,type);
                        String task_result = task.getException().getMessage().toString();
                        Log.d("Error", "Login failed! " + task_result);
                    }
                }
            });
        } else {
            FirebaseDatabase.getInstance().goOnline();
            getTokenAndContinueLogin(connectionID,type);
        }
    }

    private void getTokenAndContinueLogin(final int connectionID, final String type){
        startTimeStamp = System.currentTimeMillis();
        String token = FirebaseInstanceId.getInstance().getToken();
        if(token == null){
            startTimeStamp = System.currentTimeMillis();
            while (FirebaseInstanceId.getInstance().getToken() == null) {
                try {
                    Thread.sleep(1 * Constants.MILLISEC_TO_SECOND_RATIO);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - startTimeStamp > 15 * Constants.MILLISEC_TO_SECOND_RATIO) {
                    break;
                }
            }
            startTimeStamp = System.currentTimeMillis();
            token = FirebaseInstanceId.getInstance().getToken();
            if (token == null) {
                Log.d(TAG, "Device token is null after 20 seconds");
                p2pServiceFinishedWithError(new P2PResultsDTO(),P2PConnectionExitStatus.FIREBASE_CONNECTION_ERROR.getCode(),connectionID,type);
                return;
            }
        }
        checkFirebaseDataBaseConnectionAndSetInitialFields(connectionID,type,token);
    }

    private void checkFirebaseDataBaseConnectionAndSetInitialFields(final int connectionID, final String type, final String token){
        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        startTimeStamp = System.currentTimeMillis();
        Log.d(TAG,"get user info");
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                isConnectedToFireBaseDatabase = true;
                startTimeStamp = System.currentTimeMillis();
                Log.d(TAG,"user info is arrived");
                if (snapshot.exists()) {
                    if(snapshot.hasChild(Constants.FIELD_ONLINE)) {
                        boolean remoteOnline = Boolean.parseBoolean(String.valueOf(snapshot.child(Constants.FIELD_ONLINE).getValue()));
                        if (!remoteOnline) {
                            FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).setValue("true");
                            FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).onDisconnect().setValue("false");
                        }
                    } else {
                        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).setValue("true");
                        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).onDisconnect().setValue("false");
                    }
                    if(!token.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
                        if(snapshot.hasChild(Constants.FIELD_TOKEN_ID)){
                            String remoteToken = String.valueOf(snapshot.child(Constants.FIELD_TOKEN_ID).getValue());
                            if (remoteToken.equals(token)){
                                isWaitingForSomething=false;
                            } else {
                                FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_TOKEN_ID).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        isWaitingForSomething=false;
                                    }
                                });
                            }
                        } else {
                            FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_TOKEN_ID).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    isWaitingForSomething=false;
                                }
                            });
                        }
                    } else {
                        isWaitingForSomething = false;
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).setValue("true");
                    FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).onDisconnect().setValue("false");
                    if(!token.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
                        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_TOKEN_ID).setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                isWaitingForSomething = false;
                            }
                        });
                    } else {
                        Log.e(TAG,"ERROR token is not set");
                        isWaitingForSomething = false;
                    }
                    /**
                     * Legacy codes: remove next version
                     */
                    FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child("busy").setValue("false");
                    FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child("lastDisconnect").setValue(0);
                    /**/
                }
                setOrUpdateRecordInPeerCandidates();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "P2PConnection connection to Firebase Database cancelled");
                p2pServiceFinishedWithError(new P2PResultsDTO(),P2PConnectionExitStatus.FIREBASE_CONNECTION_ERROR.getCode(),connectionID,type);
            }
        });
    }

    private void p2pServiceFinishedWithError(P2PResultsDTO p2pResults, Integer exitStatus, Integer connectionID, String type) {
        isWaitingForSomething=false;
        if(type.equals(Constants.KEY_START) || type.equals(Constants.KEY_OFFER)) {
            p2pResults.setExitStatus(exitStatus);
            p2pResults.setConnectionID(connectionID);
            p2pResults.setSender(type.equals(Constants.KEY_START));
            p2pResults.setConnectionEnd(System.currentTimeMillis());
            String jsonP2PResults = new GsonBuilder().create().toJson(p2pResults);
            Intent serviceRestarterIntent = new Intent(P2PConnectionStarterService.this, ServiceMonitor.class);
            serviceRestarterIntent.putExtra(Constants.KEY_P2P_RESULTS, jsonP2PResults);
            serviceRestarterIntent.putExtra(Constants.KEY_CONNECTION_ID, connectionID);
            serviceRestarterIntent.setAction(ServiceMonitorActions.P2P_SERVICE_FINISHED.getServiceStarterString());
            ServiceMonitor.enqueueWork(this, serviceRestarterIntent);
        }
    }

    private void removeRecordFromPeerCandidates() {
        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_PEER_CANDIDATES).child(androidID).onDisconnect().cancel();
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_PEER_CANDIDATES).child(androidID).removeValue();
    }

    private void setOrUpdateRecordInPeerCandidates() {
        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        /**
         * Legacy codes: remove next version
         */
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child("lastSeenOnline").setValue(ServerValue.TIMESTAMP);
        /**/
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_PEER_CANDIDATES).child(androidID).setValue(ServerValue.TIMESTAMP);
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_PEER_CANDIDATES).child(androidID).onDisconnect().removeValue();
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).onDisconnect().setValue("false");
    }

    private void closeFirebaseConnection() {
        final String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).onDisconnect().cancel();
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(androidID).child(Constants.FIELD_ONLINE).setValue("false");
        removeRecordFromPeerCandidates();
        FirebaseDatabase.getInstance().goOffline();
        //FirebaseAuth.getInstance().getCurrentUser().delete();
    }
}
