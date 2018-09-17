package com.hyunju.jin.movie.activity.videocall;

import android.content.Context;
import android.opengl.EGLContext;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyunju.jin.movie.datamodel.JanusFeed;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.JanusWebRTCConst;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import computician.janusclientapi.IJanusGatewayCallbacks;
import computician.janusclientapi.IJanusPluginCallbacks;
import computician.janusclientapi.IPluginHandleWebRTCCallbacks;
import computician.janusclientapi.JanusMediaConstraints;
import computician.janusclientapi.JanusPluginHandle;
import computician.janusclientapi.JanusServer;
import computician.janusclientapi.JanusSupportedPluginPackages;
import computician.janusclientapi.PluginHandleSendMessageCallbacks;
import computician.janusclientapi.PluginHandleWebRTCCallbacks;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 다중영상통화를 하기 위해 서버와 통신하는 모든 작업을 처리하는 클래스
 * 영상통화 참여자의 화면을 그리고 통화 수신, 종료 등의 작업을 처리한다.
 */
public class GroupVideoCall {

    private final String TAG = "GroupVideoCall";    // 로그를 남길때 사용함.
    public static JanusServer janusServer;
    private JanusPluginHandle pluginHandle;

    Context context;    // Toast 메시지를 띄우기 위해 클래스 객체생성시 전달받은 Context 를 저장함
    EGLContext eglContext;  // 영상통화 참여자들의 화면을 렌더링하며 모든 렌더러를 관리하는 클래스
    private VideoRenderer.Callbacks localRender;    // 자신의 영상통화 화면을 그리는 렌더러
    private Stack<VideoRenderer.Callbacks> availableRemoteRenderers = new Stack<>(); // 아직 영상통화에 들어오지 않은 사용자의 화면 렌더러를 보관하는 스택.
    private HashMap<BigInteger, VideoRenderer.Callbacks> remoteRenderers = new HashMap<>(); // 현재 영상통화 중인 다른 사용자의 화면 렌더러를 보관하는 목록.
    private HashMap<BigInteger, JanusFeed> remoteCallbacks = new HashMap<>();   // 현재 영상통화중인 다른 사용자의 정보와 콜백 리스너를 보관하는 목록.

    // 하나의 영상통화 그룹을 채팅방이라고 생각하면 아래와 같은 정보가 필요하다.
    private int groupVideoCallRoomID;   // 영상통화 그룹 구분 번호
    private BigInteger myFeedID;    // 자신의 미디어 피드 구분 번호. 자신의 비디오 정보를 중계하는 회선이라고 이해하자.
    private User senderUser; // 영상통화를 건 사용자
    private HashMap<Integer, User> groupVideoCallUsers; // 전화를 건 사용자를 제외하고 영상통화에 참여한 사람들

    private boolean loginUserEqualSender;    // 영상통화 발신자와 현재 사용자가 일치하는지 나타낸다. true 일 경우에만 새로운 Room 을 생성해야한다.

    UserService userService;

    /**
     * 영상통화 수신자들이 통화가 온 것을 확인할 수 있게 FCM 메세지를 전송해야한다.
     * FCM 메시지 전송을 서버에 요청한다.
     */
    private void fcmSend(){

        // 서버에 보낼 데이터를 준비한다.
        HashMap<String, String> data_sendFCMForVideoCall = new HashMap<>();
        Gson gson = new Gson(); // 서버에 보낼 데이터 객체를 json 형태로 변환하기 위해 생성

        data_sendFCMForVideoCall.put("senderUser", gson.toJson(senderUser));    // 그룹영상통화 발신자 정보
        data_sendFCMForVideoCall.put("groupVideoCallRoomID", groupVideoCallRoomID+"");  // 그룹영상통화 방 ID

        // 그룹영상통화 수신자 정보를 배열에 담는다.
        ArrayList<User> receivers = new ArrayList<User>();
        for(Integer userCode : groupVideoCallUsers.keySet()){
            receivers.add(groupVideoCallUsers.get(userCode));
        }
        // 수신자 정보를 json 형태로 변환하여 서버에 보낸다.
        data_sendFCMForVideoCall.put("receivers", gson.toJson(receivers));   // 수신자들에게 순서대로 FCM 메시지를 보내기 위해 배열형태로 보내고
        data_sendFCMForVideoCall.put("receiversHashMap", gson.toJson(groupVideoCallUsers)); // FCM 메시지에 담을 HashMap 형태로도 보낸다.
        /* (참고) 두가지 형태로 데이터를 보낸 이유
            ArrayList 으로 보낸 데이터는 서버에서 사용하기 편한 형태.
            HashMap 으로 보낸 데이터는 그대로 FCM 메시지에 데이터로 넣어 보내면 MyFireBaseMessagingService 에서 바로 사용하기 편한 형태.
        */

        Call<ResponseData> call_sendFCMForVideoCall = userService.post("sendFCMForVideoCall", data_sendFCMForVideoCall);
        call_sendFCMForVideoCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    registerUsername();
                    return;
                }

                Log.e(TAG, "FCM 전송 요청 실패");
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "FCM 전송 요청 실패 (onFailure)");
                t.printStackTrace();
            }
        });


    }

    /**
     * 생성자. 영상통화 발신자인 경우 이 생성자를 사용해야한다.
     */
    public GroupVideoCall(Context context, VideoRenderer.Callbacks localRender, ArrayList<VideoRenderer.Callbacks> remoteRenders, User sender, HashMap<Integer, User> groupVideoCallUsers){

        this.context = context;
        this.loginUserEqualSender = true; // 영상통화 발신자이기 때문에 true
        this.localRender = localRender; // 자신의 영상통화 화면 렌더러 지정

        for(int i = 0; i < remoteRenders.size(); i++) {   // 다른 사용자의 영상통화 화면 렌더러 지정
            this.availableRemoteRenderers.push(remoteRenders.get(i));
        }

        // 영상통화 참가자 정보를 저장한다.
        senderUser = sender;
        this.groupVideoCallUsers = groupVideoCallUsers;

        // 영상통화 방 번호를 생성한다.
        /*
            (참고1) 방번호의 중복을 피하기 위해선 서버에서 exist 요청을 보내 검사해야한다.
            현재 이 앱은 동시에 여러개의 영상통화가 생길일이 없어서 생략함. 실서비스할 경우 방번호 확인하는 부분을 추가해야한다.

            (참고2) 서버에 Janus 게이트웨이 설정파일(janus.plugin.videoroom.cfg)을 보면 기본적으로 생성되는 비디오룸이 있다. 그 비디오룸 ID 와 겹치지 않도록 방번호를 생성해야한다.
            현재 자동으로 생성되는 비디오룸 ID 는 1234, 5678 이다.
        */
        Random random = new Random();
        groupVideoCallRoomID = random.nextInt(1000) + 2000; // 2000~3000 번에 해당하는 방번호를 생성한다.
        //groupVideoCallRoomID= 1234;

        // WebRTC 미디어 서버인 Janus 와 연결한다.
        janusServer = new JanusServer(new JanusGlobalCallbacks());
        userService = RetrofitClient.getUserService();
    }

    /**
     * 영상통화 수신자인 경우 이 생성자를 사용해야한다.
     * 마지막 매개변수로 자신이 들어갈 영상통화 방 ID 를 줘야한다.
     */
    public GroupVideoCall(Context context, VideoRenderer.Callbacks localRender, ArrayList<VideoRenderer.Callbacks> remoteRenders, User sender, HashMap<Integer, User> groupVideoCallUsers, int groupVideoCallRoomID){

        this.context = context;
        this.loginUserEqualSender = false;
        this.localRender = localRender; // 자신의 영상통화 화면 렌더러 지정

        for(int i = 0; i < remoteRenders.size(); i++) {   // 다른 사용자의 영상통화 화면 렌더러 지정
            this.availableRemoteRenderers.push(remoteRenders.get(i));
        }

        // 영상통화 참가자 정보를 저장한다.
        senderUser = sender;
        this.groupVideoCallUsers = groupVideoCallUsers;

        // 영상통화 방 번호를 생성한다.
        /*
            (참고1) 방번호의 중복을 피하기 위해선 서버에서 exist 요청을 보내 검사해야한다.
            현재 이 앱은 동시에 여러개의 영상통화가 생길일이 없어서 생략함. 실서비스할 경우 방번호 확인하는 부분을 추가해야한다.

            (참고2) 서버에 Janus 게이트웨이 설정파일(janus.plugin.videoroom.cfg)을 보면 기본적으로 생성되는 비디오룸이 있다. 그 비디오룸 ID 와 겹치지 않도록 방번호를 생성해야한다.
            현재 자동으로 생성되는 비디오룸 ID 는 1234, 5678 이다.
        */
        this.groupVideoCallRoomID = groupVideoCallRoomID;

        // WebRTC 미디어 서버인 Janus 와 연결한다.
        janusServer = new JanusServer(new JanusGlobalCallbacks());
        userService = RetrofitClient.getUserService();
    }

    /**
     * WebRTC 에서 사용할 미디어 데이터 설정을 초기화한다.
     */
    public boolean initializeMediaContext(Context context, boolean audio, boolean video, boolean videoHwAcceleration, EGLContext eglContext){
        // videoHwAcceleration 는 무슨 역할?
        // eglContext 은 VideoRendererGui.getEGLContext() 인데.. 여기에 화면을 그리겠다는건지 뭔지 암튼 openGL 봐도봐도 모르겠어~
        this.eglContext = eglContext;
        return janusServer.initializeMediaContext(context, audio, video, videoHwAcceleration, eglContext);
    }

    public void Start() {
        janusServer.Connect();
    }

    /**
     * Janus 게이트웨이와 연결하고 그 결과를 리턴하는 콜백 리스너
     */
    public class JanusGlobalCallbacks implements IJanusGatewayCallbacks {

        private final String LOCAL_TAG = "JanusGlobalCB";

        public void onSuccess() {
            Log.e(LOCAL_TAG, "Janus Gateway connect SUCCESS");
            janusServer.Attach(new JanusVideoRoomCallbacks());
        }

        @Override
        public void onDestroy() {
            Log.e(LOCAL_TAG, "Janus Gateway connect DESTROY");
        }

        @Override
        public String getServerUri() {
            return JanusWebRTCConst.JANUS_URI;
        }

        @Override
        public List<PeerConnection.IceServer> getIceServers() {
            /*
                (참고) WebRTC 에선 P2P 통신을 위한 연결 시 ICE 프로토콜?을 지원하기로 했다. 따라서 ICE 서버 설정을 해줘야한다.
                ICE 서버는 사용자간 연결 가능한 모든 통로?에 대한 정보를 가지고 있다가 매번 가장 빠른 통로를 사용하여 통신할 수 있도록 해준다.

                (주의) 사용할 STUN, TURN 서버는 Janus 게이트웨이에 설정된 값과 동일해야한다.
                게이트웨이에를 기동하면 아래와 같이 STUN, TURN 서버를 확인할 수 있다.

                    STUN server to use: stun.voip.eutelia.it:3478
                      >> 83.211.9.232:3478 (IPv4)
                    Testing STUN server: message is of 20 bytes
                      >> Our public address is 183.111.227.218
                    TURN server to use: 183.111.227.218:3478 (udp)

                게이트웨이와 클라이언트의 서버 정보를 동일하게 맞춰주지 않으면 P2P 연결이 불가능함.
             */
            ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
            // STUN 서버 설정. 무료로 사용할 수 있는 서버를 사용한다.
            iceServers.add(new PeerConnection.IceServer("stun:stun.voip.eutelia.it:3478"));
            //iceServers.add(new PeerConnection.IceServer("stun:83.211.9.232:3478"));

            // TURN 서버는 무료인 경우가 거의 없다. 별도로 구축해야한다.
            /*
                직접 구축해야한다는 사실을 잘 모르고 그냥 서버 IP 로 설정했었음.. 계속 Feed ID 를 찾지 못해서 연결을 할 수 없었다.
                현재 STUN 서버로도 충분히 빠른 통신이 가능하고 시연 상황에서 문제가 없으므로 TURN 서버는 구축하지 않은 상태다.
                단, 실제 시연환경(발표, 면접)에서 어떤 예외가 발생할지 모르니 핫스팟을 이용해 시연이 가능한지 확인해 볼 필요가 있음.
             */
            //iceServers.add(new PeerConnection.IceServer("turn:183.111.227.218:3478", "movie", "pwd1234"));

            return iceServers;
        }

        @Override
        public Boolean getIpv6Support() {
            return Boolean.FALSE;
        }

        @Override
        public Integer getMaxPollEvents() {
            // 이건 뭔지 모르겠네요~
            return 0;
        }

        @Override
        public void onCallbackError(String error) {
            Log.e(LOCAL_TAG, "[ERROR] "+error);

            /*
                (참고) 에러 발생 상황 정리
                -   Janus 서버가 기동되지 않은 경우
                     Connection to janus server is closed
                -   unpublish 요청 시 간혹 발생하는데.. 원인을 모르겠다.
                    Error connected to Janus gateway. Exception: slowlink is not a constant in computician.janusclientapi.JanusMessageType
             */
        }
    }


    /**
     * Janus 미디어 서버와 연결 후 연결 종료까지 메시지를 처리하는 콜백 리스너.
     *
     * (궁금) 여기에서 Plugin 이 정확이 무엇을 위해 지정되어야 하는건지 모르겠다. 사용 가능한 메시징 종류를 알기 위해?
     */
    public class JanusVideoRoomCallbacks implements IJanusPluginCallbacks {

        private final String LOCAL_TAG = "JanusVideoRoomCreateCB";

        @Override
        public void success(JanusPluginHandle handle) {
            Log.e(LOCAL_TAG, "VideoRoom plugin attach SUCCESS");
            pluginHandle = handle;
            if(loginUserEqualSender){
                createVideoRoom();  // 새로운 영상통화 그룹을 만든다.
            }else{
                registerUsername();
            }
        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsep) {
            try {
                String videoroom = msg.getString("videoroom");

                if(videoroom.equals("joined")) {    // 여기서 joined 는 내가 게시자로서 성공적으로 참여한 경우임
                    Log.i("VideoRoomTest", "[joined]");

                    myFeedID = new BigInteger(msg.getString("id")); // 참가자의 고유 ID. 서버에서 자동으로 생성한다.
                    publishOwnFeed();

                    // 게시자로서 참가한 시점에 이미 미디어를 게시중인 (=통화에 참여한) 사람들이 있을테니까 이 사람들을 화면에 표시해야함.
                    // 이 코드는 전화를 받는사람들이라면 반드시 실행해야겠다.
                    if(msg.has("publishers")){  // 게시자가 뭐야? 그리고 어떻게 여러명일 수 있지.. 방에 참여하는 모든 인원(=서버에 영상을 보내는 게시자)을 말하는건가?

                        // publishers 목록을 못찾는 이유.. 서버와 클라이언트의 stun, turn 서버가 안맞기 때문. 왜???
                        JSONArray pubs = msg.getJSONArray("publishers");
                        for(int i = 0; i < pubs.length(); i++) {
                            JSONObject pub = pubs.getJSONObject(i);
                            BigInteger tehId = new BigInteger(pub.getString("id"));
                            String display = pub.getString("display");
                            newRemoteFeed(tehId, display);
                        }
                    }

                } else if(videoroom.equals("destroyed")) {
                    Log.i("VideoRoomTest", "[destroyed]");
                    // 현재 호출되는 일은 없음.

                } else if(videoroom.equals("event")) {

                    if(msg.has("configured")) { // request 값이 publish, configured 일때 응답
                        Log.i("VideoRoomTest", "[configured] "+ msg.getString("configured"));

                    }else if(msg.has("started")){ // request 값이 start 일때 응답
                        Log.i("VideoRoomTest", "[started]");

                    }else if(msg.has("joining")){   // 새로운 사용자가 비디오룸에 join 했을때 응답
                        Log.i("VideoRoomTest", "[joining]");
                        // 별다른 작업은 안한다. 영상통화에서 join 만 하는건 의미 없음.

                    }else if(msg.has("publishers")){    // 해당 비디오룸에 새로운 publisher 가 게시를 시작한 경우
                        JSONArray pubs = msg.getJSONArray("publishers");
                        for(int i = 0; i < pubs.length(); i++) {
                            JSONObject pub = pubs.getJSONObject(i);
                            Log.i("VideoRoomTest", "[publishers] "+pub.getString("id"));
                            newRemoteFeed(new BigInteger(pub.getString("id")), pub.getString("display"));
                        }

                    }else if(msg.has("unpublished")) {  // 영상통화에 참가자들(publisher 들) 이 게시만 중단하는 경우(unpublish), 방을 떠나는 경우(leave) 메시지
                        Log.i("VideoRoomTest", "[unpublished]");

                        // unpublished 값이 게시가 취소된 게시자의 고유 ID 임
                        String unpublished = msg.getString("unpublished");
                        if("ok".equals(unpublished)){   // 자기 자신인 경우
                            //  unpublish 를 요청한 경우 이 블록이 실행되는데, 현재 로직에서는 실행될 일이 없다.

                        }else{ // 다른 사람인 경우

                            BigInteger remoteFeedId = new BigInteger(msg.getString("unpublished"));

                            if (remoteRenderers.containsKey(remoteFeedId)) {  // 현재 영상통화에 참가중인 사용자인지 확인한다.
                                // 토스트 메시지가 왜 안보일까?
                                //Toast.makeText(context, remoteCallbacks.get(remoteFeedId).getDisplayName()+" 님이 나가셨습니다.", Toast.LENGTH_SHORT).show();

                                remoteCallbacks.get(remoteFeedId).getVideoRenderer().dispose(); // 사용자의 화면을 그리던 렌더러를 메모리 해제한다.
                                VideoRendererGui.remove(remoteRenderers.get(remoteFeedId)); // 화면 렌더링을 담당하는 VideoRendererGui 에서 해당 렌더러를 제거한다.
                                /*
                                    현재는 한번 나간 사용자는 다시 들어올 수 없고, 추가로 사용자를 초대할 수 없기때문에 remove 만 하고 다시 렌더러를 추가하진 않는다.
                                    만약 추가로 사용자를 초대할 것이라면 create 해서 렌더러 추가해줘야하는데, 그것도 현재 비어있는 화면 위치에 맞게 생성해야한다.
                                 */

                                /*
                                   영상통화 참여자 목록에서 제외시키고 그 사용자 화면을 표시하던 렌더러를 다시 추가하는 작업.
                                   현재는 참여자 목록에서만 제외시켜야하므로 주석처리.
                                 */
                                // availableRemoteRenderers.push(remoteRenderers.remove(remoteFeedId)); // 사용자를 추가하지 못하도록 렌더러를 반납하진 않는다.
                                remoteRenderers.remove(remoteFeedId);   // 영상통화 참여자 목록에서 제외한다.

                            }

                            if(remoteRenderers.size() == 0 && availableRemoteRenderers.empty()){
                                // 현재 영상통화에 참여중인 사용자가 없고, 더이상 들어올 사용자도 없는 경우
                                //Toast.makeText(context, "통화를 종료합니다.", Toast.LENGTH_SHORT).show();
                                endCall();

                            }else{
                                Log.e(LOCAL_TAG, "남은 사용자 "+remoteRenderers.size());
                            }
                        }

                    }else if(msg.has("leaving")) {
                        Log.e("VideoRoomTest", "[leaving]");
                        // 이때 통화종료 처리
                        BigInteger id = new BigInteger(msg.getString("leaving"));
                        Log.e(LOCAL_TAG, "["+id+"] 사용자 방을 leaving");

                    }else{
                        Log.e(LOCAL_TAG , "에러 발생");
                    }
                }

                if(jsep != null) {
                    pluginHandle.handleRemoteJsep(new PluginHandleWebRTCCallbacks(null, jsep, false));
                }

            } catch (Exception ex) {
                Log.e(LOCAL_TAG, "[메시지 처리중 에러]");
                ex.printStackTrace();
            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {
            Log.e(LOCAL_TAG, "onLocalStream()");
            stream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        }

        @Override
        public void onRemoteStream(MediaStream stream) {
            Log.e(LOCAL_TAG, "onRemoteStream()");

        }

        @Override
        public void onDataOpen(Object data) {
            Log.e(LOCAL_TAG, "success()");

        }

        @Override
        public void onData(Object data) {
            Log.e(LOCAL_TAG, "onData()");

        }

        @Override
        public void onCleanup() {
            Log.e(LOCAL_TAG, "onCleanup()");
        }

        @Override
        public void onDetached() {
            Log.e(LOCAL_TAG, "onDetached()");
        }

        @Override
        public JanusSupportedPluginPackages getPlugin() {
            return JanusSupportedPluginPackages.JANUS_VIDEO_ROOM;
        }

        @Override
        public void onCallbackError(String error) {
            Log.e(LOCAL_TAG, error);

            /*
                (참고) error 메시지 정리
                 1) Peerconnection factory is not initialized, please initialize via initializeMediaContext so that peerconnections can be made by the plugins
             */
        }
    }

    /**
     * 새로운 영상통화 사용자의 미디어 정보를 받을 수 있게 한다.
     */
    private void newRemoteFeed(BigInteger id, String display) { // 플러그인을 리스너로 첨부한다.

        final String LOCAL_TAG = "newRemoteFeed";

        VideoRenderer.Callbacks myrenderer;
        if(!remoteRenderers.containsKey(id)){    // 이미 영상통화에 참여한 사람이 아니라면
            if(availableRemoteRenderers.empty()){    // 영상을 렌더링할 공간이 없다면
                //TODO no more space
                Log.e(LOCAL_TAG, "남은 공간이 없어");
                return; // 그냥 나감
            }
            // 영상을 렌더링할 공간이 남아있다면
            Log.e(LOCAL_TAG, "[pop] "+id);
            remoteRenderers.put(id, availableRemoteRenderers.pop());    // 영상통화 참여중인 목록에 사용자 코드를 Key 값으로 렌더러를 추가한다.
        }
        myrenderer = remoteRenderers.get(id);

        ListenerAttachCallbacks newPublisher = new ListenerAttachCallbacks(id, display, myrenderer);
        // 사용자 Feed 에 연결한다.
        janusServer.Attach(newPublisher);
    }

    // join 은 방에 입장할때만 쓰는게 아니다. 특정 목적으로 P2P 연결을 시작할때 사용하는듯.

    // videoRoom 에 joined 되었을때, 그 시점에 존재하는 모든 게시자들에게서 미디어 정보를 수신할 수 있도록 한다.
    // 중요한 점은 미디어정보를 받고있는 게시자가 방을 나가게되면 핸들러가 사라지게 된다는 것이다.
    // 핸들러가 사라졌을 때 렌더러를 다시 복구하거나 혹은 에러가 안나도록 처리하는 작업이 필요하겠다. 구독자는 게시자에게 종속된다.

    // 이게 호출되는 시점은 개발자가 정하면 됨. 지금은 비디오룸에 최초로 입장했을 때 기존에 존재하던 게시자에 한해서 호출되도록 되어있다.

    /**
     * 다른 사용자와 영상통화 연결을 관리하는 콜백 리스너
     */
    class ListenerAttachCallbacks implements IJanusPluginCallbacks{

        public VideoRenderer videoRenderer; // 비디오 렌더링을 관리하는 클래스
        private VideoRenderer.Callbacks rendererCallbacks; // 렌더러
        final private BigInteger feedID;    // 렌더링할 사용자 코드
        String display; // 디스플레이명. 사용자 ID 값을 가진다.
        public JanusPluginHandle listener_handle = null;

        final String LOCAL_TAG = "ListenerAttachCB";

        public ListenerAttachCallbacks(BigInteger id, String display, VideoRenderer.Callbacks renderer){
            this.rendererCallbacks = renderer;
            this.display = display;
            this.feedID = id;

        }

        public void success(JanusPluginHandle handle) {
            listener_handle = handle;
            try {
                /*
                    자세한 데이터 값은 https://janus.conf.meetecho.com/docs/videoroom.html 에서 [VideoRoom Subscribers] 으로 검색
                 */
                JSONObject body = new JSONObject();
                JSONObject msg = new JSONObject();
                body.put("request", "join");    // 방에 참가한다는 의미가 아니라 특정 피드에 참가한다는 의미임
                body.put("room", groupVideoCallRoomID);   // 그 피드는 이 방번호에 있고
                body.put("ptype", "subscriber");  // 이번엔 리스너로 참가하네.. 피드를 받아보는 리스너로 참가한다.
                // 공홈에 보면 listener 가 아니라 subscriber 으로 바뀐것 같다.
                // 방 설정값을 보면 publishers = 6 이니까 listener 로는 제한이 없는 것 같은데
                body.put("feed", feedID);   // 조인할 피드. 반드시 적어줘야함~! 피드는 결국 게시자(publisher? 의 고유 ID다.)
                Log.e("피드코드", feedID + "");
                msg.put("message", body);
                handle.sendMessage(new PluginHandleSendMessageCallbacks(msg)); // 요청을 서버에 보낸다.
                // 사실상 미디어스트림을 받기 위한 SDP 제안을 '준비'하는 플러그인 역할을 하는 것.
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onMessage(JSONObject msg, JSONObject jsep) {

            try {
                String videoroom = msg.getString("videoroom");
                Log.e(LOCAL_TAG, "[리모트리스너] videoroom = " + videoroom);

                // join을 했는데 attached 가 리턴된다?
                if(videoroom.equals("event")){
                    if(msg.has("started")) {
                        Log.e("제대로 시작됨", "");
                    }

                }else if (videoroom.equals("attached") && jsep != null) { // jsep 값은 왜 확인하는걸까? msg 와 jsep 의 차이는?
                    // 아까 success 에서 보낸 SDP 제안을 실제로 생성하는 과정
                    // WebRTC PeerConnection 설정 과정을 다시 봐야할듯하다. 제안을 요청하고 응답이오면 제안을 보낸다. 이걸 P2P 양쪽에서 둘다 해줘야한다.

                    // 여기서 리스너를 저장하도록 하는게 낫겠는데요. 그리고 핸들러를 저장하는 것임.. 정확히는 listener_handle 이걸 저장해야한다는 말

                    // 지금은 제안을 실제로 보내는 과정임.
                    final JSONObject remoteJsep = jsep; // 뭐니?
                    listener_handle.createAnswer(new IPluginHandleWebRTCCallbacks() {
                        @Override
                        public void onSuccess(JSONObject obj) {
                            try {
                                JSONObject mymsg = new JSONObject();
                                JSONObject body = new JSONObject();
                                body.put("request", "start");   // start 는 뭘 시작한다는걸까 방을 시작하는게 아닌가보네..
                                // 아까 SDP 제안을 함(success) 그 제안에 대한 응답이 옴(onMessage) 그래서 응답을 받았다는 답장을 또 보낸다.(start)
                                // 그러면 P2P 연결이 시작됨.
                                body.put("room", groupVideoCallRoomID);
                                mymsg.put("message", body);
                                mymsg.put("jsep", obj);
                                listener_handle.sendMessage(new PluginHandleSendMessageCallbacks(mymsg));
                                // 이 요청에 성공하면 event 응답이 오고 "started" : "ok" 이 온다는데 이건 음.. 어디로 오는거니? 여기엔 onMessage 가 없단다.
                                // 통신과정에서 핸들러를 하나 만들어서 걔가 모든 작업을 다 하는게 아니네.. P2P 연결 수마다 생성되는거같아.
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        @Override
                        public JSONObject getJsep() {
                            return remoteJsep;
                        }

                        @Override
                        public JanusMediaConstraints getMedia() {
                            JanusMediaConstraints cons = new JanusMediaConstraints();
                            cons.setVideo(null);    // 얘를 null 로 하자면 비디오는 안보내겠다?
                            cons.setRecvAudio(true);
                            cons.setRecvVideo(true);
                            // 영상통화 상대방의 영상과 오디오만 받도록 설정한다.
                            return cons;
                        }

                        @Override
                        public Boolean getTrickle() {
                            return true;
                        }

                        @Override
                        public void onCallbackError(String error) {
                            Log.e("attached", "새로운 게시자와 연결 후");
                            Log.e("attached", error);
                        }
                    });
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onLocalStream(MediaStream stream) {

        }

        @Override
        public void onRemoteStream(MediaStream stream) {
            videoRenderer = new VideoRenderer(rendererCallbacks);
            // 사용자의 정보와 콜백리스너를 저장한다.
            remoteCallbacks.put(feedID, new JanusFeed(feedID, display, groupVideoCallUsers.get(display), videoRenderer, this));
            stream.videoTracks.get(0).addRenderer(videoRenderer); // 인덱스 0 으로 지정..?
        }

        @Override
        public void onDataOpen(Object data) {

        }

        @Override
        public void onData(Object data) {

        }

        @Override
        public void onCleanup() {

        }

        @Override
        public void onDetached() {
            Log.e(LOCAL_TAG, "리모트 onDetached() 호출됨");
            //listener_handle.detach();
        }

        @Override
        public JanusSupportedPluginPackages getPlugin() {
            return JanusSupportedPluginPackages.JANUS_VIDEO_ROOM;
        }

        @Override
        public void onCallbackError(String error) {
            Log.e(LOCAL_TAG, "[ERROR]" + error);
        }
    }


    /**
     * join 한 비디오룸에서 자신의 미디어 정보를 상대방에게 보낼 수 있도록 미디어 게시 권한을 획득한다.
     */
    private void publishOwnFeed() {

        final String LOCAL_TAG = "publishOwnFeed";

        if(pluginHandle != null) {
            pluginHandle.createOffer(new IPluginHandleWebRTCCallbacks() {
                @Override
                public void onSuccess(JSONObject obj) {
                    try{
                        Log.e(LOCAL_TAG, "피드 생성 요청");
                        // 우선 이걸 왜 해줘야하는지 모르겠다. 영상 재생/일지정지/정지로 쓸수있대. 영상을 바로 재생하도록 하는거구나 이
                        JSONObject msg = new JSONObject();
                        JSONObject body = new JSONObject();
                        //body.put("request", "configure");   // videoroom 설정 값 변경
                        body.put("request", "publish"); // publish, configure
                        body.put("audio", true);
                        body.put("video", true);
                        msg.put("message", body);
                        msg.put("jsep", obj);   // obj 에 들은 데이터는?
                        pluginHandle.sendMessage(new PluginHandleSendMessageCallbacks(msg));
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public JSONObject getJsep() {
                    return null;
                }

                @Override
                public JanusMediaConstraints getMedia() {
                    JanusMediaConstraints cons = new JanusMediaConstraints();
                    cons.setRecvAudio(false);   // 자신의 오디오, 비디오 정보는 받을 필요 없으므로 false
                    cons.setRecvVideo(false);
                    /*
                        자신의 오디오, 비디오 정보를 상대방에게 보내야하므로 true 로 설정한다.
                        VideoRoom 플러그인을 사용할땐 비디오, 오디오 스트림이 없으면 에러난다.
                        권한 획득을 못한 경우에도 에러!
                     */
                    cons.setSendAudio(true);
                    cons.setSendVideo(true);
                    return cons;
                }

                @Override
                public Boolean getTrickle() {
                    return true;
                }

                @Override
                public void onCallbackError(String error) {
                    Log.e(LOCAL_TAG, "onCallbackError()");
                    Log.e(LOCAL_TAG, error);
                }
            });
        }
    }


    /**
     * 그룹 영상통화를 위한 새로운 방 생성을 요청한다.
     */
    private void createVideoRoom(){

        String LOCAL_TAG = "createVideoRoom";

        if(pluginHandle != null) {

            JSONObject obj = new JSONObject();
            JSONObject msg = new JSONObject();

            try {
                obj.put("request", "create");   // 새로운 비디오룸을 생성한다.
                obj.put("room", groupVideoCallRoomID);  // 비디오룸의 아이디 값을 설정한다.
                obj.put("is_private", false);    // 방 비공개 여부. 비공개할 경우 방 목록 조회시 나타나지 않는다.
                obj.put("notify_joining", true);    // 방에 joined 된 경우도 알림을 받도록 설정한다.
                obj.put("publishers", 4);   // 영상통화에 참여할 수 있는 최대 인원을 4명으로 지정한다.
                msg.put("message", obj);

                // 서버에 create 작업을 요청한다.
                pluginHandle.sendMessage(new PluginHandleSendMessageCallbacks(msg));  // 이 코드가 왜 try/catch 문 밖에 있어야 할까?
                /*
                     create 작업은 동기 요청으로 처리되므로 콜백 리스너의 onMessage 가 호출되진 않는다.
                     sendMessage() 라인이 오류없이 실행되면 요청이 완료됬다고 보면 된다.

                 */
                Log.e(LOCAL_TAG, "방번호(" + groupVideoCallRoomID+") 생성됨");

                //registerUsername();

                // 이제 FCM 으로 전화를 걸어버리자.
                fcmSend();


            } catch(Exception ex) {
                Log.e(LOCAL_TAG, "방번호(" + groupVideoCallRoomID+") 생성 요청 실패");
                ex.printStackTrace();
            }

        }else{
            // pluginHandle 이 null 인 경우

        }
    }


    /**
     * 사용자를 영상통화 그룹에 추가한다.
     */
    private void registerUsername() {

        if(pluginHandle != null) {

            JSONObject obj = new JSONObject();
            JSONObject msg = new JSONObject();

            try {
                /*
                    사용자를 새로만든 영상통화 그룹에 join 시킨다. P2P 연결을 하기전에 방에 join 부터 해야하기 때문에 이 작업이 필요하다.
                    join 은 방에 입장한다는 개념으로 이해하면 편하다. 방에 입장하면 여러 알림(joined, publish, unpublish 등)을 받을 순 있지만 P2P 연결은 되지 않은 상태다.
                    아.. 그러면 저런 알림은 다 서버에서 보내주는 거라고 보면 되겠구나.
                 */
                obj.put("request", "join");
                obj.put("room", groupVideoCallRoomID);
                obj.put("ptype", "publisher");
                obj.put("display", senderUser.getId());
                msg.put("message", obj);

                pluginHandle.sendMessage(new PluginHandleSendMessageCallbacks(msg));

            } catch(Exception ex) {
                ex.printStackTrace();
            }

        }else{
            // pluginHandle 이 null 인 경우
        }
    }

    /**
     * 영상통화를 종료한다.
     */
    public void endCall(){
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
