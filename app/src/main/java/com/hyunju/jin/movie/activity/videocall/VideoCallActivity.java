package com.hyunju.jin.movie.activity.videocall;

import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.adapter.videocall.GroupVideoCallUsersAdapter;
import com.hyunju.jin.movie.datamodel.User;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import computician.janusclientapi.JanusServer;

/**
 * 다중영상통화를 하기 위해 전화를 걸고 통화를 하는 화면.
 *
 * 현재 카메라, 마이크 권한요청 처리가 되있지 않음.
 */
public class VideoCallActivity extends SuperActivity {

    /*
       영상통화할 사용자 목록을 꺼내는 Key 값. 자기 자신은 목록에서 제외됨.
       목록은 Key-Value 형태와 리스트 형태 두가지로 제공된다.
     */
    public static final String DATA_KEY_REC_MAP = "receiversHashMap";   // Key-Value 형태로 저장된 데이터 key 값. 통화 발신자 수신자 모두 전달하는 데이터.
    public static final String DATA_KEY_REC = "receivers";  // 리스트 형태로 저장된 데이터 key 값. 수신자만 전달하는 데이터.

    public static final String DATA_KEY_SENDER = "senderUser"; // 영상통화를 건 사용자 정보를 꺼내는 key 값.
    public static final String DATA_KEY_ROOM_ID = "videoRoomID";    // 영상통화 방 ID 값을 꺼내는 Key

    User sender;    // 통화 발신자 정보
    HashMap<Integer, User> receiversHashMap; //  영상 통화할 사용자 목록. Key 값은 사용자 코드, Value 값은 사용자 정보를 담은 User 객체다. 자기자신은 목록에 포함되지 않는다.
    ArrayList<User> receivers;
    int groupVideoCallRoomID;   // 영상통화 방 ID

    public static JanusServer janusServer;  // WebRTC 미디어 서버와 연결된 객체. 영상통화에 관련된 모든 작업을 담당한다.
    private VideoRenderer.Callbacks localRender;
    ArrayList<VideoRenderer.Callbacks> remoteRenders;   // 다른 사람들 화면 그리기

    GroupVideoCall groupVideoCall;

    /*
        버터나이프 라이브러리를 이용해 View 를 참조하는 객체
        @BindView(레이아웃 xml 파일에서의 View ID 값)을 해당 객체에서 참조하게된다.
     */

    @BindView(R.id.layout_video_call) FrameLayout layout_video_call;   // 영상통화 중일때 보여질 화면의 최상위 레이아웃
    @BindView(R.id.gl_surface_view_remotes) GLSurfaceView gl_surface_view_remotes;  // 영상통화 화면

    @BindView(R.id.layout_receiver) LinearLayout layout_receiver;   // 전화 수신자가 영상통화를 수락/거절하기 전까지 보여질 화면의 최상위 레이아웃
    @BindView(R.id.img_sender_user_profile) ImageView img_sender_user_profile;  // 영상통화 발신자 프로필 사진
    @BindView(R.id.tv_sender_user_profile) TextView tv_sender_user_profile; // 영상통화 발신자 ID
    @BindView(R.id.recycler_view_video_call_users) RecyclerView recycler_view_video_call_users; // 영상통화 참가자들을 표시하는 뷰

    GroupVideoCallUsersAdapter groupVideoCallUsersAdapter; // recycler_view_video_call_users 에 연결되는 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        // 이걸 해준다고해서 에러가 안나는건 아니다. E/System: Ignoring attempt to set property "java.net.preferIPv6Addresses" to value "false".
        // 정확히 무슨 일을 하는 건지는 모르겠음.
        java.lang.System.setProperty("java.net.preferIPv6Addresses", "true");
        java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
        */
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이블바 없앰. (주의) setContentView 보다 먼저 호출되어야 함.
        setContentView(R.layout.activity_video_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 풀스크린 사용
        ButterKnife.bind(this); // 버터나이프 라이브러리를 이용해 View 참조를 생성한다.

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            sender = (User) bundle.getSerializable(DATA_KEY_SENDER);
            // 영상통화할 사용자 목록을 꺼낸다.
            receiversHashMap = (HashMap<Integer, User>) bundle.getSerializable(DATA_KEY_REC_MAP);
            receivers = (ArrayList<User>) bundle.getSerializable(DATA_KEY_REC);
            groupVideoCallRoomID = bundle.getInt(DATA_KEY_ROOM_ID, -1); // 전화 발신자인경우 -1, groupVideoCallRoomID 는 사용하지 않는다.
        }

        // 선택한 사용자가 있는지 확인한다.
        if(receiversHashMap == null || receiversHashMap.size() == 0){
            Toast.makeText(getContext(), "사용자를 선택해주세요.", Toast.LENGTH_SHORT).show();
            finish();   // 없다면 액티비티를 종료한다.
        }

        initialize();
    }

    /**
     * 통화를 하기 위해 필요한 변수, 객체 생성 및 화면 작업을 실행한다.
     */
    private void initialize(){

        // 전화 발신자일 경우
        if(sender.getUserCode() == loginUser.getUserCode()){
            gl_surface_view_remotes.setPreserveEGLContextOnPause(true);  // GLSurfaceView 가 일시정지 되었다가 다시 시작될때 유지할 건지? 뭔차인지
            gl_surface_view_remotes.setKeepScreenOn(true);  // 화면 포커스 유지
            VideoRendererGui.setView(gl_surface_view_remotes, new VideoViewInit());    // 왜 이걸 해줘야하지?
            /*
                영상통화 화면을 그릴 렌더러를 생성한다.
                (주의) VideoRendererGui.setView 를 한 후 생성해야한다.
                       VideoRendererGui.create 함수는 VideoViewInit 내부에서 호출되면 안된다.
            */
            createRenderer();

        }else{  // 전화 수신자일 경우

            // 영상통화 발신자 정보 표시
            Glide.with(getContext()).load(sender.getProfileImg()).apply(new RequestOptions().error(R.drawable.user_avatar)).into(img_sender_user_profile);
            tv_sender_user_profile.setText(sender.getId()+"");

            // 영상통화 참여자 정보를 표시하자.
            // 어댑터와 데이터를 연결
            groupVideoCallUsersAdapter = new GroupVideoCallUsersAdapter(getContext(), receivers);
            // 어댑터와 리사이클러 뷰 연결
            // 리사이클러 뷰 레이아웃 지정. 단말기 해상도에 따라 참여자 목록이 잘리는 경우가 있다.
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            //GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            recycler_view_video_call_users.setLayoutManager(layoutManager);
            recycler_view_video_call_users.setAdapter(groupVideoCallUsersAdapter);

            layout_video_call.setVisibility(View.GONE); // 영상통화 화면을 감추고
            layout_receiver.setVisibility(View.VISIBLE);  // 전화 받기, 거절 레이아웃을 보여준다.

        }

    }

    /**
     * 영상통화 화면을 그릴 렌더러를 생성한다.
     * 전체 영상통화 참여자 수에 따라 통화화면의 크기를 다르게 생성한다.
     */
    private void createRenderer(){

        // 최대 3명과 함께 영상통화를 할 수 있다.
        remoteRenders = new ArrayList<VideoRenderer.Callbacks>();
        // 렌더러를 3개 생성한다.
        remoteRenders.add(VideoRendererGui.create(50, 0, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true));
        remoteRenders.add(VideoRendererGui.create(0, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true));
        remoteRenders.add(VideoRendererGui.create(50, 50, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true));

        // 자신의 화면을 표시할 렌더러 생성
        localRender = VideoRendererGui.create(0, 0, 50, 50, VideoRendererGui.ScalingType.SCALE_ASPECT_FILL, true);

        /*
            렌더러는 VideoRendererGui 에서 생성되고 관리되는데, create 할 경우 리턴값으로 생성된 렌더러에 대한 참조를 얻을 수 있다.
            렌더링 화면이 겹칠 경우 나중에 create 된 렌더링이 더 위에서 화면을 그린다.

         */
    }

    /**
     * 영상통화 화면을 렌더링하는 View 를 초기화하는 클래스.
     * 이 작업은 별도의 스레드에서 수행되어야한다. (왜?)
     */
    private class VideoViewInit implements Runnable {
        public void run() {
            try {
                EGLContext con = VideoRendererGui.getEGLContext();

                // 전화를 건 경우
                if(sender.getUserCode() == loginUser.getUserCode() || groupVideoCallRoomID == -1){
                    groupVideoCall = new GroupVideoCall(getContext(), localRender, remoteRenders, loginUser, receiversHashMap);
                }else{
                    // 전화를 받은 경우, 지정된 영상통화 그룹으로 들어가도록 비디오룸 ID 를 설정한다.
                    groupVideoCall = new GroupVideoCall(getContext(), localRender, remoteRenders, loginUser, receiversHashMap, groupVideoCallRoomID);
                }

                if(groupVideoCall.initializeMediaContext(getContext(), true, true, true, con)){
                    groupVideoCall.Start();
                }else{
                    Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                }

            } catch (Exception ex) {
                Log.e("computician.janusclient", ex.getMessage());
            }
        }
    }


    /**
     * 영상통화 수락 처리
     */
    @OnClick(R.id.btn_call_receiver)
    public void callReceiver(){

        layout_receiver.setVisibility(View.GONE);
        layout_video_call.setVisibility(View.VISIBLE);

        // 수신자일 경우, 영상통화를 시작한다.
        if(sender.getUserCode() != loginUser.getUserCode()) {

            gl_surface_view_remotes.setPreserveEGLContextOnPause(true);  // GLSurfaceView 가 일시정지 되었다가 다시 시작될때 유지할 건지? 뭔차인지
            gl_surface_view_remotes.setKeepScreenOn(true);  // 화면 포커스 유지
            VideoRendererGui.setView(gl_surface_view_remotes, new VideoViewInit());    // 왜 이걸 해줘야하지?
            /*
                영상통화 화면을 그릴 렌더러를 생성한다.
                (주의) VideoRendererGui.setView 를 한 후 생성해야한다.
                       VideoRendererGui.create 함수는 VideoViewInit 내부에서 호출되면 안된다.
            */
            createRenderer();
        }
    }


    /**
     * 영상통화 거절 처리
     */
    @OnClick(R.id.btn_call_reject)
    public void callReject(){
        // 현재 액티비티를 종료한다.
        finish();
        /*
            이미 통화중인 사람들에게 알림을 줘야하는데 그 부분은 처리 안함. 어차피 시연에서는 초대한 사람을 다 보여줄 것이기 때문에~!
            알림을 주기 위해선 우선 서버에 연결을 하고 join 을 한 상태여야할 것 같다.
         */
    }

    /**
     * 영상통화 종료 처리
     */
    @OnClick(R.id.img_call_end)
    public void callEnd(){
        groupVideoCall.endCall();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(getContext(),"통화 종료버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
    }
}
