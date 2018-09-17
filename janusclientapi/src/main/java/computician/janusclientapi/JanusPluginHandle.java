package computician.janusclientapi;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.*;

import java.math.BigInteger;

/**
 * Janus 미디어 서버 플러그인을 관리하는 클래스
 */
public class JanusPluginHandle {

    private boolean started = false;
    private MediaStream myStream = null;
    private MediaStream remoteStream = null;
    private SessionDescription mySdp = null;
    private PeerConnection peerConnection = null;
    private DataChannel dataChannel = null;
    private boolean trickle = true;
    private boolean iceDone = false;
    private boolean sdpSent = false;

    private final String VIDEO_TRACK_ID = "1929283";
    private final String AUDIO_TRACK_ID = "1928882";
    private final String LOCAL_MEDIA_ID = "1198181";

    private PeerConnectionFactory sessionFactory = null;
    private final JanusServer server;
    public final JanusSupportedPluginPackages plugin;
    public final BigInteger id;
    private final IJanusPluginCallbacks callbacks;

    public JanusPluginHandle(JanusServer server, JanusSupportedPluginPackages plugin, BigInteger handle_id, IJanusPluginCallbacks callbacks) {
        this.server = server;
        this.plugin = plugin;
        id = handle_id;
        this.callbacks = callbacks;
        sessionFactory = new PeerConnectionFactory();
    }

    public void onMessage(String msg) {
        try {
            JSONObject obj = new JSONObject(msg);
            callbacks.onMessage(obj, null);
        } catch (JSONException ex) {
            //TODO do we want to notify the GatewayHandler?
        }
    }

    public void onMessage(JSONObject msg, JSONObject jsep) {
        callbacks.onMessage(msg, jsep);
    }

    private void onLocalStream(MediaStream stream) {
        callbacks.onLocalStream(stream);
    }

    private void onRemoteStream(MediaStream stream) {
        callbacks.onRemoteStream(stream);
    }

    public void onDataOpen(Object data) {
        callbacks.onDataOpen(data);
    }

    public void onData(Object data) {
        callbacks.onData(data);
    }

    public void onCleanup() {
        callbacks.onCleanup();
    }

    public void onDetached() {
        callbacks.onDetached();
    }

    public void sendMessage(IPluginHandleSendMessageCallbacks obj) {
        server.sendMessage(TransactionType.plugin_handle_message, id, obj, plugin);
    }

    private void streamsDone(IPluginHandleWebRTCCallbacks webRTCCallbacks) {
        MediaConstraints pc_cons = new MediaConstraints();
        pc_cons.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        if (webRTCCallbacks.getMedia().getRecvAudio())
            pc_cons.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        if (webRTCCallbacks.getMedia().getRecvVideo())
            pc_cons.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        peerConnection = sessionFactory.createPeerConnection(server.iceServers, pc_cons, new WebRtcObserver(webRTCCallbacks));
        if (myStream != null)
            peerConnection.addStream(myStream);
        if (webRTCCallbacks.getJsep() == null) {
            createSdpInternal(webRTCCallbacks, true);
        } else {
            try {
                JSONObject obj = webRTCCallbacks.getJsep();
                String sdp = obj.getString("sdp");
                SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(obj.getString("type"));
                SessionDescription sessionDescription = new SessionDescription(type, sdp);
                peerConnection.setRemoteDescription(new WebRtcObserver(webRTCCallbacks), sessionDescription);
            } catch (Exception ex) {
                webRTCCallbacks.onCallbackError(ex.getMessage());
            }
        }
    }

    public void createOffer(IPluginHandleWebRTCCallbacks webrtcCallbacks) {
        new AsyncPrepareWebRtc().execute(webrtcCallbacks);
    }

    public void createAnswer(IPluginHandleWebRTCCallbacks webrtcCallbacks) {
        new AsyncPrepareWebRtc().execute(webrtcCallbacks);
    }

    private void prepareWebRtc(IPluginHandleWebRTCCallbacks callbacks) {
        if (peerConnection != null) {
            if (callbacks.getJsep() == null) {
                createSdpInternal(callbacks, true);
            } else {
                try {
                    JSONObject jsep = callbacks.getJsep();
                    String sdpString = jsep.getString("sdp");
                    SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(jsep.getString("type"));
                    SessionDescription sdp = new SessionDescription(type, sdpString);
                    peerConnection.setRemoteDescription(new WebRtcObserver(callbacks), sdp);
                } catch (JSONException ex) {

                }
            }
        } else {
            trickle = callbacks.getTrickle() != null ? callbacks.getTrickle() : false;
            AudioTrack audioTrack = null;
            VideoTrack videoTrack = null;
            MediaStream stream = null;
            if (callbacks.getMedia().getSendAudio()) {
                AudioSource source = sessionFactory.createAudioSource(new MediaConstraints());
                audioTrack = sessionFactory.createAudioTrack(AUDIO_TRACK_ID, source);
            }
            if (callbacks.getMedia().getSendVideo()) {
                VideoCapturerAndroid capturer = null;
                switch (callbacks.getMedia().getCamera()) {
                    case back:
                        capturer = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfBackFacingDevice());
                        break;
                    case front:
                        capturer = VideoCapturerAndroid.create(VideoCapturerAndroid.getNameOfFrontFacingDevice());
                        break;
                }
                MediaConstraints constraints = new MediaConstraints();
                JanusMediaConstraints.JanusVideo videoConstraints = callbacks.getMedia().getVideo();
               /* constraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(videoConstraints.getMaxHeight())));
                constraints.optional.add(new MediaConstraints.KeyValuePair("minHeight", Integer.toString(videoConstraints.getMinHeight())));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(videoConstraints.getMaxWidth())));
                constraints.optional.add(new MediaConstraints.KeyValuePair("minWidth", Integer.toString(videoConstraints.getMinWidth())));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(videoConstraints.getMaxFramerate())));
                constraints.optional.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(videoConstraints.getMinFramerate()))); */
                VideoSource source = sessionFactory.createVideoSource(capturer, constraints);
                videoTrack = sessionFactory.createVideoTrack(VIDEO_TRACK_ID, source);
            }
            if (audioTrack != null || videoTrack != null) {
                stream = sessionFactory.createLocalMediaStream(LOCAL_MEDIA_ID);
                if (audioTrack != null)
                    stream.addTrack(audioTrack);
                if (videoTrack != null)
                    stream.addTrack(videoTrack);
            }
            myStream = stream;
            if (stream != null)
                onLocalStream(stream);
            streamsDone(callbacks);
        }
    }

    private void createSdpInternal(IPluginHandleWebRTCCallbacks callbacks, Boolean isOffer) {
        MediaConstraints pc_cons = new MediaConstraints();
        pc_cons.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        if (callbacks.getMedia().getRecvAudio()) {
            pc_cons.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        }
        if (callbacks.getMedia().getRecvVideo()) {
            pc_cons.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
            Log.d("VIDEO_ROOM", "Receiving video");
        }
        if (isOffer) {
            peerConnection.createOffer(new WebRtcObserver(callbacks), pc_cons);
        } else {
            peerConnection.createAnswer(new WebRtcObserver(callbacks), pc_cons);
        }
    }

    public void handleRemoteJsep(IPluginHandleWebRTCCallbacks webrtcCallbacks) {
        new AsyncHandleRemoteJsep().execute(webrtcCallbacks);
    }

    /**
     * 영상통화를 종료한다. P2P 연결을 해제한다.
     */
    public void hangUp() {
        String TAG = "hangUp";

        Log.e(TAG, "hangUp 진입");

        if (remoteStream != null) {
            Log.e(TAG, "remoteStream 진입");
            remoteStream.dispose();
            remoteStream = null;
        }
        if (myStream != null) {
            Log.e(TAG, "myStream 진입");
            myStream.dispose();
            myStream = null;
        }

        Log.e(TAG, peerConnection.signalingState()+"");

        if(peerConnection != null){
            Log.e(TAG, "peerConnection != null");
        }

        if (peerConnection != null && peerConnection.signalingState() != PeerConnection.SignalingState.CLOSED) {
            Log.e(TAG, "peerConnection 진입");
            peerConnection.close();
        }

        peerConnection = null;
        started = false;
        mySdp = null;
        if (dataChannel != null) {
            Log.e(TAG, "dataChannel 진입");
            dataChannel.close();
        }
        dataChannel = null;
        trickle = true;
        iceDone = false;
        sdpSent = false;

        Log.e(TAG, "hangUp 종료");
    }

    public void detach() {
        String TAG = "detach";

        Log.e(TAG, "detach 진입");

        hangUp();
        JSONObject obj = new JSONObject();
        server.sendMessage(obj, JanusMessageType.detach, id);

        Log.e(TAG, "detach 종료");
    }

    private void onLocalSdp(SessionDescription sdp, IPluginHandleWebRTCCallbacks callbacks) {
        if (peerConnection != null) {
            if (mySdp == null) {
                mySdp = sdp;
                peerConnection.setLocalDescription(new WebRtcObserver(callbacks), sdp);
            }
            if (!iceDone && !trickle)
                return;
            if (sdpSent)
                return;
            try {
                sdpSent = true;
                JSONObject obj = new JSONObject();
                obj.put("sdp", mySdp.description);
                obj.put("type", mySdp.type.canonicalForm());
                callbacks.onSuccess(obj);
            } catch (JSONException ex) {
                callbacks.onCallbackError(ex.getMessage());
            }
        }
    }

    private void sendTrickleCandidate(IceCandidate candidate) {
        try {
            JSONObject message = new JSONObject();
            JSONObject cand = new JSONObject();
            if (candidate == null)
                cand.put("completed", true);
            else {
                cand.put("candidate", candidate.sdp);
                cand.put("sdpMid", candidate.sdpMid);
                cand.put("sdpMLineIndex", candidate.sdpMLineIndex);
            }
            message.put("candidate", cand);

            server.sendMessage(message, JanusMessageType.trickle, id);
        } catch (JSONException ex) {

        }
    }

    private void sendSdp(IPluginHandleWebRTCCallbacks callbacks) {
        if (mySdp != null) {
            mySdp = peerConnection.getLocalDescription();
            if (!sdpSent) {
                sdpSent = true;
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("sdp", mySdp.description);
                    obj.put("type", mySdp.type.canonicalForm());
                    callbacks.onSuccess(obj);
                } catch (JSONException ex) {
                    callbacks.onCallbackError(ex.getMessage());
                }
            }
        }
    }

    private class WebRtcObserver implements SdpObserver, PeerConnection.Observer {
        private final IPluginHandleWebRTCCallbacks webRtcCallbacks;
        public WebRtcObserver(IPluginHandleWebRTCCallbacks callbacks) {
            this.webRtcCallbacks = callbacks;
        }

        @Override
        public void onSetSuccess() {
            Log.d("JANUSCLIENT", "On Set Success");
            if (mySdp == null) {
                createSdpInternal(webRtcCallbacks, false);
            }
        }

        @Override
        public void onSetFailure(String error) {
            Log.d("JANUSCLIENT", "On set Failure");
            //todo JS api does not account for this
            webRtcCallbacks.onCallbackError(error);
        }

        @Override
        public void onCreateSuccess(SessionDescription sdp) {
            Log.d("JANUSCLIENT", "Create success");
            onLocalSdp(sdp, webRtcCallbacks);
        }

        @Override
        public void onCreateFailure(String error) {
            Log.d("JANUSCLIENT", "Create failure");
            webRtcCallbacks.onCallbackError(error);
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState state) {
            Log.d("JANUSCLIENT", "Signal change " + state.toString());
            switch (state) {
                case STABLE:
                    break;
                case HAVE_LOCAL_OFFER:
                    break;
                case HAVE_LOCAL_PRANSWER:
                    break;
                case HAVE_REMOTE_OFFER:
                    break;
                case HAVE_REMOTE_PRANSWER:
                    break;
                case CLOSED:
                    break;
            }
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
            Log.d("JANUSCLIENT", "Ice Connection change " + state.toString());
            switch (state) {
                case DISCONNECTED:
                    break;
                case CONNECTED:
                    break;
                case NEW:
                    break;
                case CHECKING:
                    break;
                case CLOSED:
                    break;
                case FAILED:
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState state) {
            switch (state) {
                case NEW:
                    break;
                case GATHERING:
                    break;
                case COMPLETE:
                    if(!trickle) {
                        mySdp = peerConnection.getLocalDescription();
                        sendSdp(webRtcCallbacks);
                    } else {
                        sendTrickleCandidate(null);
                    }
                    break;
                default:
                    break;
            }
            Log.d("JANUSCLIENT", "Ice Gathering " + state.toString());
        }

        @Override
        public void onIceCandidate(IceCandidate candidate) {
            if(trickle){
                sendTrickleCandidate(candidate);
            }
        }

        @Override
        public void onAddStream(MediaStream stream) {
            Log.d("JANUSCLIENT", "onAddStream " + stream.label());
            remoteStream = stream;
            onRemoteStream(stream);
        }

        @Override
        public void onRemoveStream(MediaStream stream) {
            Log.d("JANUSCLIENT", "onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel channel) {
            Log.d("JANUSCLIENT", "onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Log.d("JANUSCLIENT", "Renegotiation needed");
        }

    }

    private class AsyncPrepareWebRtc extends AsyncTask<IPluginHandleWebRTCCallbacks, Void, Void> {

        @Override
        protected Void doInBackground(IPluginHandleWebRTCCallbacks... params) {
            IPluginHandleWebRTCCallbacks cb = params[0];
            prepareWebRtc(cb);
            return null;
        }
    }

    private class AsyncHandleRemoteJsep extends AsyncTask<IPluginHandleWebRTCCallbacks, Void, Void> {
        @Override
        protected Void doInBackground(IPluginHandleWebRTCCallbacks... params) {
            IPluginHandleWebRTCCallbacks webrtcCallbacks = params[0];
            if (sessionFactory == null) {
                webrtcCallbacks.onCallbackError("WebRtc PeerFactory is not initialized. Please call initializeMediaContext");
                return null;
            }
            JSONObject jsep = webrtcCallbacks.getJsep();
            if (jsep != null) {
                if (peerConnection == null) {
                    Log.d("JANUSCLIENT", "could not set remote offer");
                    callbacks.onCallbackError("No peerconnection created, if this is an answer please use createAnswer");
                    return null;
                }
                try {

                    String sdpString = jsep.getString("sdp");
                    Log.d("JANUSCLIENT", sdpString);
                    SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(jsep.getString("type"));
                    SessionDescription sdp = new SessionDescription(type, sdpString);
                    peerConnection.setRemoteDescription(new WebRtcObserver(webrtcCallbacks), sdp);
                } catch (JSONException ex) {
                    Log.d("JANUSCLIENT", ex.getMessage());
                    webrtcCallbacks.onCallbackError(ex.getMessage());
                }
            }
            return null;
        }
    }

}
