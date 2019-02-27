package hu.uszeged.inf.wlab.stunner.service.handler;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import hu.uszeged.inf.wlab.stunner.utils.Constants;
import hu.uszeged.inf.wlab.stunner.utils.dtos.WebRTCResultsDTO;
import hu.uszeged.inf.wlab.stunner.utils.enums.P2PConnectionExitStatus;

public class WebRTCThreadHandler extends Handler {
    public final String TAG = "WebRTCThreadHandler";
    private PeerConnectionFactory peerConnectionFactory;

    private PeerConnection pc1;
    private PeerConnection pc2;

    private DataChannel sendChannel;
    private DataChannel receiveChannel;

    private String localMessageReceived = "";
    private String remoteMessageReceived = "";
    private boolean srflxGotLocal = false;
    private boolean srflxGotRemote = false;
    private boolean localFailed = false;
    private boolean remoteFailed = false;
    private boolean localConnected = false;
    private boolean remoteConnected = false;

    private boolean localMessageArrived = false;
    private boolean remoteMessageArrived = false;

    private Long startTimeStamp = System.currentTimeMillis();

    private WebRTCResultsDTO resultsDTO;

    /** The {@link Context} instance to gain access to system resources. */
    private final Context context;


    public WebRTCThreadHandler(Looper looper, final Context context) {
        super(looper);
        this.context = context;
        resultsDTO = new WebRTCResultsDTO();
    }

    @Override
    public void handleMessage(final Message msg) {
        final Bundle arguments = (Bundle) msg.obj;
        Log.d(TAG, "handleMessage start ");
        peerConnectionFactory = new PeerConnectionFactory();
        Log.d(TAG, "has yet to create local and remote peerConnection");
        List<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        //iceServers.add(new PeerConnection.IceServer("stun:stun.sipgate.net:3478"));
        MediaConstraints constraints = new MediaConstraints();
        resultsDTO.setConnectionStart(System.currentTimeMillis());
        pc1 = peerConnectionFactory.createPeerConnection(iceServers, constraints, pc1Observer);
        pc2 = peerConnectionFactory.createPeerConnection(iceServers, constraints, pc2Observer);
        DataChannel.Init initDataChannel = new DataChannel.Init();
        initDataChannel.ordered = true;
        initDataChannel.maxRetransmits = -1; //2;
        initDataChannel.maxRetransmitTimeMs = -1; //500;
        initDataChannel.protocol = "TCP/DTLS/SCTP";//"TCP/DTLS/SCTP";
        initDataChannel.negotiated = false;
        initDataChannel.id = 1;//recentConnectionID;
        sendChannel = pc1.createDataChannel("RTCDataChannel", new DataChannel.Init());
        sendChannel.registerObserver(localDataChannelObserver);
        pc1.createOffer(localSessionObserver, constraints);
        startTimeStamp = System.currentTimeMillis();
        while(!isStopService()){
            try {
                Thread.sleep(Constants.MILLISEC_TO_SECOND_RATIO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "wait in handleMessage");
        }
        closeChannel();
        closeConnection();
        if (context instanceof TestFinishedListener) {
            final Bundle resultBundle = new Bundle();
            resultBundle.putParcelable(Constants.KEY_WEBRTC_RESULTS, resultsDTO);
            resultBundle.putInt(Constants.KEY_ID,arguments.getInt(Constants.KEY_ID));
            ((TestFinishedListener) context).onTestFinished(resultBundle);
        }
        Log.d(TAG, "handleMessage end "+resultsDTO.toString());
    }



    public boolean isStopService(){
        if(remoteFailed && localFailed) {
            if(srflxGotLocal && srflxGotRemote)
                resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_FAILED_TO_OPEN_WITH_SRFLX);
            else
                resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_FAILED_TO_OPEN_WITHOUT_SRFLX);
            return true;
        }
        if(remoteMessageArrived && localMessageArrived){
            resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL);
            return true;
        }
        if(System.currentTimeMillis() - startTimeStamp > 10*Constants.MILLISEC_TO_SECOND_RATIO) {
            if(localConnected && remoteConnected){
                resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR);
            } else {
                resultsDTO.setExitStatus(P2PConnectionExitStatus.CONNECTION_TIMED_OUT);
            }
            return true;
        }
        return false;
    }

    private void closeChannel() {
        Log.d(TAG, "start of closeChannel");
        if (sendChannel != null) {
            sendChannel.close();
        }
        if (receiveChannel != null) {
            receiveChannel.close();
        }
        if (resultsDTO.getChannelOpen() != 0L && resultsDTO.getChannelClosed() == 0L){
            resultsDTO.setChannelClosed(System.currentTimeMillis());
        }
        Log.d(TAG, "end of closeChannel");
    }

    private void closeConnection() {
        Log.d(TAG, "start of closeConnection");
        if (pc1 != null){
            pc1.close();
        }
        if (pc2 != null){
            pc2.close();
        }
        if(resultsDTO.getConnectionEnd() == 0L) {
            resultsDTO.setConnectionEnd(System.currentTimeMillis());
        }
        Log.d(TAG, "end of closeConnection");
    }

    PeerConnection.Observer pc1Observer = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "localPeerConnectionObserver onSignalingChange() " + signalingState.name());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "localPeerConnectionObserver onIceConnectionChange() " + iceConnectionState.name());
            if (iceConnectionState.name() == "FAILED"){
                localFailed = true;
            } else if (iceConnectionState.name().equals("CONNECTED")) {
                startTimeStamp = System.currentTimeMillis();
                localConnected = true;
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d(TAG, "localPeerConnectionObserver onIceConnectionReceivingChange(): " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(TAG, "localPeerConnectionObserver onIceGatheringChange() " + iceGatheringState.name());
            if(iceGatheringState.name().equals("COMPLETE")){
                startTimeStamp = System.currentTimeMillis();
                if(!srflxGotLocal){
                    localFailed = true;
                }
            }
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "localPeerConnectionObserver onIceCandidate: " + iceCandidate.toString());

            startTimeStamp = System.currentTimeMillis();
            JSONObject json = new JSONObject();
            JSONObject candidateJson = new JSONObject();

            String candidateStr = "candidate:";
            int pos = iceCandidate.toString().indexOf(candidateStr) + candidateStr.length();
            String[] fields = iceCandidate.toString().substring(pos).split(" ");

            String mes;

            try {

                candidateJson.put("type", fields[7]);
                candidateJson.put("protocol", fields[2]);
                candidateJson.put("address", fields[4]);

                Log.d(TAG, "TYPEPC1: " + candidateJson.get("type"));

                json.put("type", "candidate");
                json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                json.put("sdpMid", iceCandidate.sdpMid);
                json.put("candidate", iceCandidate.sdp);

                mes = json.toString();

                Log.d ("ICE",iceCandidate.sdp);
                if(candidateJson.get("type").equals("srflx")){
                    Log.d (TAG, "SRFLX candidate of pc1 was added to pc2 candidates");
                    JSONObject json2 = new  JSONObject (mes);
                    IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                    Log.d(TAG, "localPeerConnectionObserver onIceCandidate: " + candidate.toString());
                    pc2.addIceCandidate (candidate);
                    srflxGotLocal = true;
                }

                /*if(candidateJson.get("type").equals("host")){
                    //Log.d (TAG, "SRFLX candidate of pc1 was added to pc2 candidates");
                    JSONObject json2 = new  JSONObject (mes);
                    IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                    pc2.addIceCandidate (candidate);
                    srflxGotLocal = true;
                }*/
                //Log.d (TAG, "local iceCandidateJson" + mes);

            } catch (org.json.JSONException ex) {
                Log.d(TAG, ex.toString());
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d(TAG, "localPeerConnectionObserver onDataChannel()");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(TAG, "localPeerConnectionObserver onRenegotiationNeeded()");
        }
    };

    PeerConnection.Observer pc2Observer = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Log.d(TAG, "remotePeerConnectionObserver onSignalingChange() " + signalingState.name());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Log.d(TAG, "remotePeerConnectionObserver onIceConnectionChange() " + iceConnectionState.name() );
            if (iceConnectionState.name() == "FAILED"){
                remoteFailed = true;
            } else  if (iceConnectionState.name().equals("CONNECTED")) {
                startTimeStamp = System.currentTimeMillis();
                remoteConnected = true;
            }
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Log.d(TAG, "remotePeerConnectionObserver onIceConnectionReceivingChange(): " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Log.d(TAG, "remotePeerConnectionObserver onIceGatheringChange() " + iceGatheringState.name());
            if(iceGatheringState.name().equals("COMPLETE")){
                startTimeStamp = System.currentTimeMillis();
                if(!srflxGotRemote){
                    remoteFailed = true;
                }
            }
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Log.d(TAG, "remotePeerConnectionObserver onIceCandidate: " + iceCandidate.toString());

            startTimeStamp = System.currentTimeMillis();
            JSONObject json = new JSONObject();
            JSONObject candidateJson = new JSONObject();

            String candidateStr = "candidate:";
            int pos = iceCandidate.toString().indexOf(candidateStr) + candidateStr.length();
            String[] fields = iceCandidate.toString().substring(pos).split(" ");

            String message;

            try {

                candidateJson.put("type", fields[7]);
                candidateJson.put("protocol", fields[2]);
                candidateJson.put("address", fields[4]);

                Log.d(TAG, "TYPEPC2: " + candidateJson.get("type"));

                json.put("type", "candidate");
                json.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                json.put("sdpMid", iceCandidate.sdpMid);
                json.put("candidate", iceCandidate.sdp);

                message = json.toString();

                Log.d (TAG, "remote iceCandidateJson" + message);

                if(candidateJson.get("type").equals("srflx")){
                    Log.d (TAG, "SRFLX candidate of pc2 was added to pc1 candidates");
                    JSONObject json2 = new  JSONObject (message);
                    IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                    pc1.addIceCandidate (candidate);
                    srflxGotRemote = true;
                }

                /*if(candidateJson.get("type").equals("host")){
                    //Log.d (TAG, "SRFLX candidate of pc1 was added to pc2 candidates");
                    JSONObject json2 = new  JSONObject (message);
                    IceCandidate candidate = new IceCandidate(json2.getString("sdpMid"), json2.getInt("sdpMLineIndex"), json2.getString("candidate"));
                    pc1.addIceCandidate (candidate);
                    srflxGotRemote = true;
                }*/

            } catch (org.json.JSONException ex) {
                Log.d(TAG, ex.toString());
            }
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {

        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {

        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Log.d(TAG, "remotePeerConnectionObserver onDataChannel()");
            startTimeStamp = System.currentTimeMillis();
            receiveChannel = dataChannel;
            receiveChannel.registerObserver(remoteDataChannelObserver);
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d(TAG, "remotePeerConnectionObserver onRenegotiationNeeded()");
        }
    };

    SdpObserver localSessionObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d(TAG, "local onCreateSuccess");

            pc1.setLocalDescription(localSessionObserver, sessionDescription);

            JSONObject json = new JSONObject();
            String message;

            try {
                json.put("type", sessionDescription.type.toString().toLowerCase());
                json.put("sdp", sessionDescription.description);

                message = json.toString();
                Log.d(TAG, message);

                JSONObject json2 = new JSONObject(message);
                String type = json2.getString("type");
                String sdp = json2.getString("sdp");

                SessionDescription sdp2 = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);

                pc2.setRemoteDescription(remoteSessionObserver, sdp2);
                MediaConstraints constraints = new MediaConstraints();
                pc2.createAnswer(remoteSessionObserver, constraints);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
            Log.d(TAG, "local onSetSuccess");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.d(TAG, "local onCreateFailure " + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.d(TAG, "local onSetFailure " + s);
        }
    };

    SdpObserver remoteSessionObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Log.d(TAG, "remote onCreateSuccess()");

            pc2.setLocalDescription(remoteSessionObserver, sessionDescription);

            JSONObject json = new JSONObject();
            String message;

            try {
                json.put("type", sessionDescription.type.toString().toLowerCase());
                json.put("sdp", sessionDescription.description);

                message = json.toString();
                Log.d(TAG, message);

                JSONObject json2 = new JSONObject(message);
                String type = json2.getString("type");
                String sdp = json2.getString("sdp");

                SessionDescription sdp2 = new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp);
                pc1.setRemoteDescription(localSessionObserver, sdp2);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSetSuccess() {
            Log.d(TAG, "remote onSetSuccess()");
        }

        @Override
        public void onCreateFailure(String s) {
            Log.d(TAG, "remote onCreateFailure() " + s);
        }

        @Override
        public void onSetFailure(String s) {
            Log.d(TAG, "remote onSetFailure()");
        }
    };

    DataChannel.Observer localDataChannelObserver = new DataChannel.Observer() {

        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {
            Log.d(TAG, "localDataChannelObserver onStateChange() " + sendChannel.state().name());
            if (sendChannel.state().name().equals("OPEN")) {
                startTimeStamp = System.currentTimeMillis();
                resultsDTO.setChannelOpen(System.currentTimeMillis());
                String message = "Hello";
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                sendChannel.send(new DataChannel.Buffer(buffer, false));
                Log.d("PC1", "I sent it...");
            } else if (sendChannel.state().name().equals("CLOSED")) {
                if(resultsDTO.getChannelOpen() != 0L) {
                    resultsDTO.setChannelClosed(System.currentTimeMillis());
                }
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
                localMessageReceived = new String(datas);
                Log.d(TAG, "Local onMessage:" + localMessageReceived);
                if (localMessageReceived.equals("World")) {
                    localMessageArrived = true;
                    resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_AND_EXCHANGE_MESSAGES_SUCCESSFUL);
                    if(resultsDTO.getChannelClosed() == 0L) {
                        resultsDTO.setChannelClosed(System.currentTimeMillis());
                    }
                    if(resultsDTO.getConnectionEnd() == 0L) {
                        resultsDTO.setConnectionEnd(System.currentTimeMillis());
                    }
                } else {
                    resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR);
                }
            }
        }
    };

    DataChannel.Observer remoteDataChannelObserver = new DataChannel.Observer() {
        @Override
        public void onBufferedAmountChange(long l) {


        }

        @Override
        public void onStateChange() {
            Log.d(TAG, "remoteDataChannel onStateChange() " + receiveChannel.state().name());
            if (sendChannel.state().name().equals("OPEN")) {
                startTimeStamp = System.currentTimeMillis();
                resultsDTO.setChannelOpen(System.currentTimeMillis());
            } else if (sendChannel.state().name().equals("CLOSED")) {
                if(resultsDTO.getChannelOpen() != 0L) {
                    resultsDTO.setChannelClosed(System.currentTimeMillis());
                }
            }
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            startTimeStamp = System.currentTimeMillis();
            Log.d(TAG, "remoteDataChannel onMessage()");

            if (!buffer.binary) {
                int limit = buffer.data.limit();
                byte[] datas = new byte[limit];
                buffer.data.get(datas);
                remoteMessageReceived = new String(datas);
                Log.d(TAG, "Remote onMessage:" + remoteMessageReceived);
                if(remoteMessageReceived.equals("Hello")){
                    String message = "World";
                    ByteBuffer bufferBack = ByteBuffer.wrap(message.getBytes());
                    receiveChannel.send(new DataChannel.Buffer(bufferBack, false));
                    remoteMessageArrived = true;
                    Log.d("PC2", "I sent it back...");
                } else {
                    resultsDTO.setExitStatus(P2PConnectionExitStatus.P2P_CHANNEL_OPEN_BUT_MESSAGE_ERROR);
                    Log.d(TAG,"Invalid message");
                }
            }
        }
    };

    /**
     * Interface to signal the service that the test has finished.
     *
     */
    public interface TestFinishedListener {
        public void onTestFinished(final Bundle results);
    }
}
