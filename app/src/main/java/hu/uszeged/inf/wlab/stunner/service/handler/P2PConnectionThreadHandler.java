package hu.uszeged.inf.wlab.stunner.service.handler;

import android.app.job.JobParameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Build;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import hu.uszeged.inf.wlab.stunner.service.ServiceMonitor;
import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.P2PResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;

public class P2PConnectionThreadHandler extends Handler {

    public static final String TAG = "P2PThreadHandler";

    private PeerConnection localPeerConnection;
    private DataChannel sendChannel;

    private P2PResultsDTO p2pConnectionInfo = new P2PResultsDTO();
    private boolean srflxGot = false;
    private ArrayList<String> iceReceiverArray  = new ArrayList<>();

    private JSONArray iceSenderArray  = new JSONArray();
    private HashMap<String,String> sdp  = new HashMap<>();
    private boolean hasOfferAlreadySent;
    private ValueEventListener onDisconnectEvnentListener;

    private final Context context;

    private boolean stopService = false;
    private Long startTimeStamp = System.currentTimeMillis();

    private BroadcastReceiver myReceiverSdp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fromWho = intent.getExtras().getString(Constants.KEY_REMOTE_PEER_ID);
            String typeOf = intent.getExtras().getString(Constants.KEY_TYPE);
            String remoteSdp = intent.getExtras().getString(Constants.KEY_MESSAGE_DATA);
            int connectionID = -1;
            try {
                JSONObject json = new JSONObject(remoteSdp);
                connectionID = json.getInt("connectionID");
            } catch (Exception e) {
                Log.d(TAG, "WARNING - The intent not contains info about connection ID");
            }
            Log.d(TAG, " P2PConnectionThreadHandler  GOT " + intent.getAction() + " fromWho:" + fromWho + " type:" + typeOf + " connectionID:" + connectionID + " remoteSDP:" + remoteSdp);
            messageArrived(fromWho, typeOf, remoteSdp);
        }
    };

    public P2PConnectionThreadHandler(Looper looper, final Context context) {
        super(looper);
        this.context = context;
    }


    @Override
    public void handleMessage(Message msg) {
        final Bundle arguments = (Bundle) msg.obj;
        hasOfferAlreadySent = false;
        Long totalWaitingStartTimeStamp = System.currentTimeMillis();
        p2pConnectionInfo = new P2PResultsDTO();
        p2pConnectionInfo.setAndroidID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        try {
            onDisconnectEvnentListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild(Constants.FIELD_ONLINE)) {
                            Boolean online = Boolean.parseBoolean(String.valueOf(dataSnapshot.child(Constants.FIELD_ONLINE).getValue()));
                            Log.d(TAG, "Peer "+dataSnapshot.getKey()+" onDisconnect:" + online);
                            if (!online) {
                                Log.d(TAG, "Peer "+dataSnapshot.getKey()+" goes offline  - STOP SERVICE");
                                p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.PEER_CONNECTION_LOST.getCode());
                                stopP2PConnectionAsync("PEER_CONNECTION_LOST");
                            }
                        } else if (dataSnapshot.hasChild(Constants.FIELD_LAST_DISCONNECT) ) {
                            Long lastDisconnect = Long.parseLong(String.valueOf(dataSnapshot.child(Constants.FIELD_LAST_DISCONNECT).getValue()));
                            Log.d(TAG, "Peer "+dataSnapshot.getKey()+" onDisconnect:" + lastDisconnect + " startConnection: " + p2pConnectionInfo.getConnectionStart() + " dif:" + (p2pConnectionInfo.getConnectionStart() - lastDisconnect));
                            if (lastDisconnect > p2pConnectionInfo.getConnectionStart()) {
                                Log.d(TAG, "Peer "+dataSnapshot.getKey()+" goes offline  - STOP SERVICE");
                                p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.PEER_CONNECTION_LOST.getCode());
                                stopP2PConnectionAsync("PEER_CONNECTION_LOST");
                            }
                        } else {
                            Log.d(TAG, "Peer "+dataSnapshot.getKey()+" record is broken  - STOP SERVICE");
                            p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.PEER_CONNECTION_LOST.getCode());
                            stopP2PConnectionAsync("PEER_CONNECTION_LOST");
                        }
                    } else {
                        Log.d(TAG, "WARNING - peer has not got record");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Firebase cancelled to view peer connection info");
                }
            };
            context.registerReceiver(myReceiverSdp, new IntentFilter(ServiceMonitor.INTENT_FILTER));
            stopService = false;
            String messageData = arguments.getString(Constants.KEY_MESSAGE_DATA, Constants.PREF_STRING_VALUE_EMPTY);
            String type = arguments.getString(Constants.KEY_TYPE, Constants.PREF_STRING_VALUE_EMPTY);
            p2pConnectionInfo.setConnectionID(arguments.getInt(Constants.KEY_CONNECTION_ID, -1));
            String peerID = arguments.getString(Constants.KEY_REMOTE_PEER_ID,Constants.PREF_STRING_VALUE_EMPTY);
            if (!peerID.equals(Constants.PREF_STRING_VALUE_EMPTY)) {
                setPeerIDAndStartListenerForItsOnDisconnect(peerID);
            }
            if (type.equals(Constants.KEY_START)) {
                p2pConnectionInfo.setSender(true);
            } else {
                p2pConnectionInfo.setSender(false);
            }
            Log.d(TAG, "My ID : " + p2pConnectionInfo.getAndroidID());
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Log.d(TAG, "Firebase connection ERROR, missed login for Firebase");
                p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.FIREBASE_CONNECTION_ERROR.getCode());
                stopP2PConnectionAsync("FIREBASE NOT LOGGED IN");
            } else {
                Log.d(TAG, PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true)
                        ? "Success initAndroidGlobals" : "Failed initAndroidGlobals");
                PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory();
                srflxGot = false;
            /*if (p2pConnectionInfo.getAndroidID().equals("a13a104079b54063")) {
                p2pConnectionInfo.setPeerID("453f94e7e4cd2714"); //Árpi - rgai emulator
            } else if (p2pConnectionInfo.getAndroidID().equals("453f94e7e4cd2714")) {
                p2pConnectionInfo.setPeerID("a13a104079b54063"); //Árpi - HTC
            }*/
                Log.d(TAG, " appointed peer:" + p2pConnectionInfo.getPeerID() + " connection ID: " + p2pConnectionInfo.getConnectionID() + " amITheInitiator:" + p2pConnectionInfo.isSender());
                List<PeerConnection.IceServer> iceServers = new LinkedList<>();
                iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
                iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
                final MediaConstraints constraints = new MediaConstraints();
                localPeerConnection = peerConnectionFactory.createPeerConnection(iceServers, constraints, localPeerConnectionObserver);
                DataChannel.Init initDataChannel = new DataChannel.Init();
                initDataChannel.ordered = true;
                initDataChannel.maxRetransmits = -1; //2;
                initDataChannel.maxRetransmitTimeMs = -1; //500;
                initDataChannel.protocol = "TCP/DTLS/SCTP";//"TCP/DTLS/SCTP";
                initDataChannel.negotiated = true;
                initDataChannel.id = 1;//recentConnectionID;
                try {
                    sendChannel = localPeerConnection.createDataChannel("RTCDataChannel" + p2pConnectionInfo.isSender(), initDataChannel);
                    sendChannel.registerObserver(localDataChannelObserver);
                    if (p2pConnectionInfo.isSender()) {
                        localPeerConnection.createOffer(localSessionObserver, constraints);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "ERROR " + e.getMessage());
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.STUN_SERVER_ERROR.getCode());
                    stopP2PConnectionAsync("CHANNEL OPEN ERROR");
                }
                if (!p2pConnectionInfo.isSender()) {
                    messageArrived(p2pConnectionInfo.getPeerID(), Constants.KEY_OFFER, messageData);
                }
            }
            startTimeStamp = System.currentTimeMillis();
            while (!stopService) {
                try {
                    Thread.sleep(Constants.MILLISEC_TO_SECOND_RATIO);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - startTimeStamp > Constants.P2P_WAIT_INTERVAL_ONLINE) {
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.CONNECTION_TIMED_OUT.getCode());
                    stopP2PConnectionAsync("CONNECTION_TIMED_OUT");
                }
                Log.d(TAG, "wait in handleMessage " + (System.currentTimeMillis() - startTimeStamp));
            }
        } catch (Exception e){
            p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.UNKNOWN.getCode());
            stopP2PConnectionAsync("UNKNOWN ERROR: "+e.getMessage());
        }
        Log.d(TAG, "stopP2PConnection is STARTED");
        context.unregisterReceiver(myReceiverSdp);
        stopListenerForPeerOnDisconnect();
        closeChannel();
        closeConnection();
        Log.d(TAG, "stopP2PConnection is FINISHED");
        Log.d(TAG,"finish in "+(System.currentTimeMillis()-totalWaitingStartTimeStamp)+"ms");
        if (context instanceof TestFinishedListener) {
            final Bundle resultBundle = new Bundle();
            resultBundle.putParcelable(Constants.KEY_P2P_RESULTS, p2pConnectionInfo);
            if(Build.VERSION.SDK_INT >= Constants.API_LEVEL_JOB_SERVICE){
                JobParameters jobParameters = arguments.getParcelable(Constants.KEY_JOB_PARAMS);
                resultBundle.putParcelable(Constants.KEY_JOB_PARAMS,jobParameters);
            }
            Log.d(TAG,"onTestFinished");
            ((TestFinishedListener) context).onTestFinished(resultBundle);
        }
        Log.d(TAG,"end");
    }

    private void setPeerIDAndStartListenerForItsOnDisconnect(String peerID){
        p2pConnectionInfo.setPeerID(peerID);
        Log.d(TAG, "START listener for " + peerID + " " + Constants.FIELD_ONLINE + " Firebase realtime database field." );
        try {
            Thread.sleep(Constants.MILLISEC_TO_SECOND_RATIO);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onDisconnectEvnentListener.isNull:"+(onDisconnectEvnentListener==null));
        FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(peerID).addValueEventListener(onDisconnectEvnentListener);
    }



    private void stopListenerForPeerOnDisconnect(){
        if(!p2pConnectionInfo.getPeerID().equals(Constants.PREF_STRING_VALUE_EMPTY)) {
            Log.d(TAG, "STOP listener for " + p2pConnectionInfo.getPeerID() + " " + Constants.FIELD_ONLINE + " Firebase realtime database field." );
            FirebaseDatabase.getInstance().getReference().child(Constants.TABLE_USERS).child(p2pConnectionInfo.getPeerID()).removeEventListener(onDisconnectEvnentListener);
        }
    }

    public void stopP2PConnectionAsync(String reason) {
        Log.d(TAG,  "stopP2PConnectionAsync: "+reason);
        stopService = true;
    }


    private void closeChannel() {
        if (sendChannel != null) {
            if ( p2pConnectionInfo.getChannelOpen() != 0L && p2pConnectionInfo.getChannelClosed() == 0L  ) {
                Long timeStamp = (new Date()).getTime();
                Log.d("TIME", timeStamp + " " + sendChannel.state().name());
                p2pConnectionInfo.setChannelClosed(timeStamp);
            }
            sendChannel.close();
            sendChannel.unregisterObserver();
        }
    }

    private void closeConnection() {
        if (localPeerConnection != null)
            localPeerConnection.close();
        if(p2pConnectionInfo.getConnectionEnd() == 0L) {
            Long timeStamp = (new Date()).getTime();
            p2pConnectionInfo.setConnectionEnd(timeStamp);
        }
        iceSenderArray = null;
        iceReceiverArray = null;
    }

    PeerConnection.Observer localPeerConnectionObserver = new PeerConnection.Observer() {

        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "localPeerConnectionObserver onSignalingChange() " + signalingState.name());
            Long timeStamp = (new Date()).getTime();
            Log.d("TIME", timeStamp + " " + signalingState.name());
            if (signalingState.name().equals("HAVE_LOCAL_OFFER")) {
                p2pConnectionInfo.setConnectionStart(timeStamp);
                p2pConnectionInfo.setSender(true);
                startTimeStamp = System.currentTimeMillis();
            } else if (signalingState.name().equals("HAVE_REMOTE_OFFER")) {
                p2pConnectionInfo.setConnectionStart(timeStamp);
                p2pConnectionInfo.setSender(false);
                startTimeStamp = System.currentTimeMillis();
            }
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "localPeerConnectionObserver onIceConnectionChange() " + iceConnectionState.name());
            Long timeStamp = (new Date()).getTime();
            if (iceConnectionState.name().equals("CONNECTED")) {
                Log.d("TIME", timeStamp + " " + iceConnectionState.name());
                Log.d("CONNECTED", " localState:" + localPeerConnection.iceConnectionState().name() + " paramState:" + iceConnectionState.name());
                try {
                    Log.d("CONNECTED", " localDiscription type:" + localPeerConnection.getLocalDescription().type + " disc:" + localPeerConnection.getLocalDescription().description);
                } catch (Exception e) {
                    Log.d("CONNECTED", " localDiscription ERROR NODISCRIPTION");
                }
                try {
                    Log.d("CONNECTED", " remoteDiscription type:" + localPeerConnection.getRemoteDescription().type + " disc:" + localPeerConnection.getRemoteDescription().description);
                } catch (Exception e) {
                    Log.d("CONNECTED", " remoteDiscription ERROR NODISCRIPTION");
                }
                startTimeStamp = System.currentTimeMillis();
            } else if (iceConnectionState.name().equals("FAILED")) {
                Log.d("FAILED", " localState:" + localPeerConnection.iceConnectionState().name() + " paramState:" + iceConnectionState.name());
                try {
                    Log.d("FAILED", " localDiscription type:" + localPeerConnection.getLocalDescription().type + " disc:" + localPeerConnection.getLocalDescription().description);
                } catch (Exception e) {
                    Log.d("FAILED", " localDiscription ERROR NODISCRIPTION");
                }
                try {
                    Log.d("FAILED", " remoteDiscription type:" + localPeerConnection.getRemoteDescription().type + " disc:" + localPeerConnection.getRemoteDescription().description);
                } catch (Exception e) {
                    Log.d("FAILED", " remoteDiscription ERROR NODISCRIPTION");
                }
                Log.d("FAILED", " senders: " + localPeerConnection.getSenders().toString());
                Log.d("FAILED", " receivers: " + localPeerConnection.getReceivers().toString());
                Log.d(TAG, "localPeerConnectionObserver onIceConnectionChange() FAILED STOPSELF");
                p2pConnectionInfo.setConnectionEnd(timeStamp);
                if(srflxGot){
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_FAILED_TO_OPEN_WITH_SRFLX.getCode());
                } else {
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_FAILED_TO_OPEN_WITHOUT_SRFLX.getCode());
                }
                stopP2PConnectionAsync("onIceConnectionChange() FAILED");
            } else if (iceConnectionState.name().equals("CLOSED")) {
                Log.d(TAG, "localPeerConnectionObserver onIceConnectionChange() CLOSED STOPSELF");
                p2pConnectionInfo.setConnectionEnd(timeStamp);
                stopP2PConnectionAsync("onIceConnectionChange() CLOSED");
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d(TAG, "localPeerConnectionObserver onIceConnectionReceivingChange(): " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(TAG, "localPeerConnectionObserver onIceGatheringChange() " + iceGatheringState.name());
            startTimeStamp = System.currentTimeMillis();
            if (iceGatheringState.name().equals("COMPLETE")) {
                if (!hasOfferAlreadySent) {
                    JSONObject json = new JSONObject();
                    String mes;
                    String key;
                    try {
                        json.put("connectionID", p2pConnectionInfo.getConnectionID());
                        json.put(Constants.KEY_ICE, iceSenderArray);
                        if (sdp.containsKey(Constants.KEY_ANSWER)) {
                            json.put("SDP", sdp.get(Constants.KEY_ANSWER));
                            key = Constants.KEY_ANSWER;
                            Log.d(TAG, sdp.get(Constants.KEY_ANSWER));
                        } else {
                            json.put("SDP", sdp.get(Constants.KEY_OFFER));
                            key = Constants.KEY_OFFER;
                            Log.d(TAG, sdp.get(Constants.KEY_OFFER));
                        }
                        mes = json.toString();
                        Log.d(TAG, mes);
                        hasOfferAlreadySent = true;
                        sendNotification(key, mes);
                    } catch (org.json.JSONException ex) {
                        Log.d(TAG, ex.toString());
                    }
                }
            }
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "localPeerConnectionObserver onIceCandidate: " + iceCandidate.toString());
            JSONObject json = new JSONObject();
            String mes;
            try {
                //json.put("connectionID", p2pConnectionInfo.getConnectionID());
                json.put("type", "candidate");
                json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                json.put("sdpMid", iceCandidate.sdpMid);
                json.put("candidate", iceCandidate.sdp);
                mes = json.toString();
                Log.d(TAG, "local iceCandidateJson" + mes);
                iceSenderArray.put(mes);
            } catch (org.json.JSONException ex) {
                Log.d(TAG, ex.toString());
            }
            startTimeStamp = System.currentTimeMillis();
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.e(TAG, "localPeerConnectionObserver onDataChannel() ERROR, this method is not should be called!");
            //sendChannel = dataChannel;
            //sendChannel.registerObserver(localDataChannelObserver);
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(TAG, "localPeerConnectionObserver onRenegotiationNeeded()");
        }
    };

    SdpObserver localSessionObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d(TAG, "localSessionObserver onCreateSuccess");

            localPeerConnection.setLocalDescription(localSessionObserver, sessionDescription);

            JSONObject json = new JSONObject();
            String message;
            try {
                //json.put("connectionID", p2pConnectionInfo.getConnectionID());
                json.put("type", sessionDescription.type.toString().toLowerCase());
                json.put("sdp", sessionDescription.description);

                message = json.toString();
                Log.d(TAG, message);

                Log.d("TYPE_OF_SDP", sessionDescription.type.toString().toLowerCase());

                if (sessionDescription.type.toString().toLowerCase().equals(Constants.KEY_ANSWER)) {
                    sdp.put(Constants.KEY_ANSWER, message);
                    //sendNotification(Constants.KEY_ANSWER, message);
                } else {
                    Log.d(TAG, "Message: " + message + " To:" + p2pConnectionInfo.getPeerID());
                    Log.d(TAG, "OFFER_MESSAGE: " + message);
                    sdp.put(Constants.KEY_OFFER, message);
                    //sendNotification(Constants.KEY_OFFER, message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
            Log.d(TAG, "localSessionObserver onSetSuccess");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.d(TAG, "localSessionObserver onCreateFailure " + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.d(TAG, "localSessionObserver onSetFailure " + s);
        }
    };

    public void messageArrived(String fromWho, String typeOf, String data) {
        startTimeStamp = System.currentTimeMillis();
        Log.d(TAG,"Type: " + typeOf + " /data: " + data + " /fromWho: " + fromWho);
        try {
            if (typeOf.equals(Constants.KEY_CLOSE_CONNECTION)) {
                p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.CONNECTION_TIMED_OUT.getCode());
                stopP2PConnectionAsync("CONNECTION_TIMED_OUT_BY_BROADCAST");
            } else if (typeOf.equals(Constants.KEY_REJECT)) {
                if (fromWho.equals(p2pConnectionInfo.getAndroidID())){
                    Log.d(TAG,"NOBODY_IS_AVAILABLE");
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.NOBODY_IS_AVAILABLE.getCode());
                } else {
                    Log.d(TAG,"OFFER_IS_REJECTED");
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.OFFER_IS_REJECTED.getCode());
                }
                stopP2PConnectionAsync("REJECT arrived");
            } else if (typeOf.equals("offer")) {
                Log.d(TAG,"OFFER");
                if (fromWho.equals(p2pConnectionInfo.getAndroidID())){
                    Log.d(TAG,"OFFER_IS_REJECTED");
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.OFFER_IS_REJECTED.getCode());
                } else if (!p2pConnectionInfo.isSender()) {
                    JSONObject jsonFromData = new JSONObject(data);
                    String typeOfSDP = jsonFromData.getString("SDP");
                    Log.d(TAG, typeOfSDP);

                    JSONObject jsonFromSDP = new JSONObject(typeOfSDP);
                    String sdp = jsonFromSDP.getString("sdp");
                    String type = jsonFromSDP.getString("type");
                    SessionDescription sdp2 = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);
                    localPeerConnection.setRemoteDescription(localSessionObserver, sdp2);
                    MediaConstraints constraints = new MediaConstraints();
                    localPeerConnection.createAnswer(localSessionObserver, constraints);

                    JSONArray iceMes = jsonFromData.getJSONArray(Constants.KEY_ICE);
                    Log.d(TAG, "ICES" + iceMes);
                    if (iceMes != null) {
                        int len = iceMes.length();
                        for (int i = 0; i < len; i++) {
                            iceReceiverArray.add(iceMes.get(i).toString());
                        }
                    }
                    //iceReceiverArray = (ArrayList<String>) Arrays.asList(iceMes.split(","));
                    Log.d(TAG, "iceArray: " + String.valueOf(iceReceiverArray));
                    for (String mes : iceReceiverArray) {
                        JSONObject json2 = new JSONObject(mes);
                        IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                        Log.d(TAG, "CANDIDATE: " + candidate);
                        localPeerConnection.addIceCandidate(candidate);
                        String candidateStr = "candidate:";
                        int pos = candidate.toString().indexOf(candidateStr) + candidateStr.length();
                        String[] fields = candidate.toString().substring(pos).split(" ");
                        if (fields[7].equals("srflx")) {
                            srflxGot = true;
                        }
                    }
                } else {
                    int connectionID = -1;
                    try {
                        JSONObject json = new JSONObject(data);
                        connectionID = json.getInt("connectionID");
                    } catch (Exception e) {
                        Log.d(TAG, "WARNING - The intent not contains info about connection ID");
                    }
                    sendNotificationReject(fromWho,connectionID);
                }
                Log.d("OK", "I got the offer...");
            } else if (typeOf.equals("answer")) {
                Log.d(TAG,"ANSWER");
                setPeerIDAndStartListenerForItsOnDisconnect(fromWho);
                JSONObject jsonFromData = new JSONObject(data);
                String typeOfSDP = jsonFromData.getString("SDP");

                JSONObject jsonFromSDP = new JSONObject(typeOfSDP);
                String sdp = jsonFromSDP.getString("sdp");
                String type = jsonFromSDP.getString("type");
                SessionDescription sdp2 = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);
                localPeerConnection.setRemoteDescription(localSessionObserver, sdp2);
                MediaConstraints constraints = new MediaConstraints();
                localPeerConnection.createAnswer(localSessionObserver, constraints);

                JSONArray iceMes = jsonFromData.getJSONArray(Constants.KEY_ICE);
                Log.d(TAG, "ICES" + iceMes);
                if (iceMes != null) {
                    int len = iceMes.length();
                    for (int i=0;i<len;i++){
                        iceReceiverArray.add(iceMes.get(i).toString());
                    }
                }
                for(String mes : iceReceiverArray){
                    JSONObject json2 = new JSONObject(mes);
                    IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                    Log.d(TAG, "CANDIDATE: " + candidate);
                    localPeerConnection.addIceCandidate(candidate);
                    String candidateStr = "candidate:";
                    int pos = candidate.toString().indexOf(candidateStr) + candidateStr.length();
                    String[] fields = candidate.toString().substring(pos).split(" ");
                    if (fields[7].equals("srflx")) {
                        srflxGot = true;
                    }
                }
                Log.d("OK", "I got the answer...");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    DataChannel.Observer localDataChannelObserver = new DataChannel.Observer() {

        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {
            Log.d(TAG, "localDataChannelObserver onStateChange() " + sendChannel.state().name() + " label: " + sendChannel.label() + " sendChannel.state(): " + sendChannel.state());
            Long timeStamp = (new Date()).getTime();
            Log.d("TIME", timeStamp + " " + sendChannel.state().name());
            if (sendChannel.state().name().equals("OPEN")) {
                startTimeStamp = System.currentTimeMillis();
                p2pConnectionInfo.setChannelOpen(timeStamp);
                if (p2pConnectionInfo.isSender()) {
                    sendDefaultMessage();
                }
            } else if (sendChannel.state().name().equals("CLOSED")) {
                if(p2pConnectionInfo.getChannelOpen() != 0L && p2pConnectionInfo.getChannelClosed() == 0L) {
                    p2pConnectionInfo.setChannelClosed(timeStamp);
                }
                Log.d(TAG, "localDataChannelObserver onStateChange() STOPSELF");
                stopP2PConnectionAsync("localDataChannelObserver onStateChange() CLOSED");
            }
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            startTimeStamp = System.currentTimeMillis();
            Log.d(TAG, "localDataChannelObserver onMessage()");
            if (!buffer.binary) {
                int limit = buffer.data.limit();
                byte[] datas = new byte[limit];
                buffer.data.get(datas);
                String localMessageReceived = new String(datas);
                Log.d(TAG, "I got a message: " + localMessageReceived + " amITheInitiator:" + p2pConnectionInfo.isSender() + " sendChannel.state():" + sendChannel.state() + " (0,5):" + localMessageReceived.substring(0, 5) + " (0,3):" + localMessageReceived.substring(0, 3));
                if (localMessageReceived.substring(0, 5).equals("Hello")) {
                    String message = "Hi!";
                    if (localMessageReceived.length() > 17) {
                        message += " I am " + p2pConnectionInfo.getAndroidID() + ". What's up " + localMessageReceived.substring(18) + " ?";
                    } else {
                        message += " What's up ?";
                    }
                    ByteBuffer buffer2 = ByteBuffer.wrap(message.getBytes());
                    sendChannel.send(new DataChannel.Buffer(buffer2, false));
                    Log.d(TAG, "I sent it back...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL.getCode());
                    stopP2PConnectionAsync("P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL");
                } else if (localMessageReceived.substring(0, 3).equals("Hi!")) {
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL.getCode());
                    stopP2PConnectionAsync("P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL");
                } else {
                    p2pConnectionInfo.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR.getCode());
                    stopP2PConnectionAsync("P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR");
                }
            }
        }
    };

    private void sendDefaultMessage() {
        String message = "Hello there! I am " + p2pConnectionInfo.getAndroidID();
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        sendChannel.send(new DataChannel.Buffer(buffer, false));
        Log.d(TAG, "I sent it...");
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
    public void sendNotification(final String type, final String message) {
        sendNotification(type,Constants.PREF_STRING_VALUE_EMPTY,message);
    }

    public void sendNotification(final String type, final String peerID, final String message) {
        if(!message.equals("null")) {
            String addressee;
            String dataTable;
            if (type.equals(Constants.KEY_OFFER)) {
                addressee = p2pConnectionInfo.getAndroidID();
                dataTable = Constants.VALUE_NOTIFICATIONS_TO_RANDOM_DEVICE_TABLE;
            } else if (type.equals(Constants.KEY_REJECT)) {
                addressee = peerID;
                dataTable = Constants.VALUE_NOTIFICATIONS_TABLE;
            } else {
                addressee = p2pConnectionInfo.getPeerID();
                dataTable = Constants.VALUE_NOTIFICATIONS_TABLE;
            }
            byte[] b = new byte[0];
            try {
                b = message.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.d(TAG, " sendNotification type:" + type + " message:" + message + " size:"+b.length+"byte");
            DatabaseReference newNotificationref = FirebaseDatabase.getInstance().getReference().child(dataTable).child(addressee).push();
            String newNotificationId = newNotificationref.getKey();
            HashMap<String, String> notificationData = new HashMap<>();
            notificationData.put("fromWho", p2pConnectionInfo.getAndroidID());
            notificationData.put("type", type);
            notificationData.put("data", message);
            Map requestMap = new HashMap();
            requestMap.put(dataTable+"/" + addressee + "/" + newNotificationId, notificationData);
            FirebaseDatabase.getInstance().getReference().updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.d(TAG, "Notification send is completed! type:" + type + " message:" + message);
                    //Log.d(TAG, "BLOCK THE FURTHER CONNECTIONS UNTIL DESTROY");
                    if (databaseError != null) {
                        Log.d("DATABASE_ERROR", "There was some error in sending request" + databaseError.toString());
                    }
                }
            });
            startTimeStamp = System.currentTimeMillis();
        } else {
            Log.d(TAG, "ERROR: DATA_CAN_NOT_BE_NULL");
        }
    }

    /**
     * Interface to signal the service that the test has finished.
     *
     */
    public interface TestFinishedListener {
        public void onTestFinished(final Bundle results);
    }
}
