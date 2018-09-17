package com.hyunju.jin.movie.fcm;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hyunju.jin.movie.activity.videocall.VideoCallActivity;
import com.hyunju.jin.movie.datamodel.User;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
    백그라운드에서 앱의 알림을 수신하는 것 외에 다른 방식으로 메시지를 처리하려는 경우에 필요합니다.
    포그라운드 앱의 알림 수신, 데이터 페이로드 수신, 업스트림 메시지 전송 등을 수행하려면 이 서비스를 확장해야 합니다.
 */
public class MyFireBaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "FCM MessagingService";

    private final int MSG_TYPE_GROUP_VIDEO_CALL_REC = 1;  // 그룹 영상통화 수신

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*
         1. 앱이 실행 안되있거나, 백그라운드에 있을 경우 이 서비스를 타지 않는다.
         2. 그럼에도 불구하고 알림메시지(notification)가 올 경우 처리가된다.
         2-2. 그이유는 알림메시지 처리 주체가 안드로이드이기 때문에, 앱이 백그라운드가 되어
         메세지 처리 주체가 없으면 안드로이드가 처리해준다.
        */
        dispatcher(remoteMessage);
    }

    private void dispatcher(RemoteMessage remoteMessage){
        if(remoteMessage == null || remoteMessage.getData() == null){
            Log.e(TAG, "수신 메시지 없음");
            return;
        }

        Map<String, String> dataMap = remoteMessage.getData();
        if(StringUtils.isEmpty(dataMap.get("type"))){
            Log.e(TAG, "처리 불가능한 메시지");
            return;
        }

        int type = Integer.parseInt(dataMap.get("type"));
        switch (type){
            case MSG_TYPE_GROUP_VIDEO_CALL_REC:      // 새로운 예약 알림
                receiveGroupVideoCall(dataMap);
                break;

        }
    }

    /**
     * 영상통화 수신 처리
     * FCM data 필드 값을 매개변수로 받는다. data 에는 영상통화와 관련된 데이터가 담겨있다.
     */
    private void receiveGroupVideoCall(Map<String, String> dataMap){
        Gson gson = new Gson();

        // 키값 "senderUser" 는 영상통화를 건 사용자 정보를 의미한다.
        User sender = gson.fromJson(dataMap.get(VideoCallActivity.DATA_KEY_SENDER), User.class);
        // 키값 "receivers" 는 영상통화에 참여하는 사용자 정보를 의미한다. 영상통화를 건 사용자는 제외된다.

        //receivers = (ArrayList<User>) gson.fromJson(dataMap.get(VideoCallActivity.DATA_KEY_REC), receivers.getClass());
        // Key-Value 형태로 넘어온다. key 값을 주의할 것! 잘못꺼내면 에러난다.
        HashMap<Integer, User> receiversHashMap = new HashMap<Integer, User>();
        receiversHashMap = (HashMap<Integer, User>) gson.fromJson(dataMap.get(VideoCallActivity.DATA_KEY_REC_MAP), receiversHashMap.getClass());

        // receivers 는 ArrayList 형태와
        ArrayList<User> receivers = new ArrayList<User>();

        try {

            /*
                난 왜 json to object 작업을 직접하고 있는거지?
                receivers = (ArrayList<User>) gson.fromJson(dataMap.get(VideoCallActivity.DATA_KEY_REC), receivers.getClass());
                이렇게 하면 LinkedMap 으로 변환이 된다. 이해가 되지 않네..
             */

            ArrayList<User> receiversTest = (ArrayList<User>) gson.fromJson(dataMap.get(VideoCallActivity.DATA_KEY_REC), receivers.getClass());
            String str_receivers = dataMap.get(VideoCallActivity.DATA_KEY_REC);
            JSONArray jsonArr = new JSONArray(str_receivers);

            for(int i = 0; i < jsonArr.length(); i++) {
                JSONObject obj = jsonArr.getJSONObject(i);

                int userCode = obj.getInt("userCode");
                String id = obj.getString("id");
                String profileImg = obj.getString("profileImg");
                String fcmInstanceId = obj.getString("fcmInstanceId");

                User user = new User(userCode, id, profileImg, fcmInstanceId);
                receivers.add(user);
            }

            Log.e(TAG, "JSONObject 완료");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "JSONObject 에러");
        }

        // 키값 "groupVideoCallRoomID" 은 영상통화 비디오 룸 ID 를 의미한다.
        int videoRoomID = Integer.parseInt(dataMap.get(VideoCallActivity.DATA_KEY_ROOM_ID));

        // 영상통화를 처리 하는 액티비티를 시작한다.
        Intent dialog = new Intent(this, VideoCallActivity.class);
        // 아래의 3가지 정보를 반드시 전달해야한다.
        dialog.putExtra(VideoCallActivity.DATA_KEY_SENDER, sender); // 영상통화를 건 사용자
        dialog.putExtra(VideoCallActivity.DATA_KEY_REC, receivers); // 영상통화 참여자들 (ArrayList 형태)
        dialog.putExtra(VideoCallActivity.DATA_KEY_REC_MAP, receiversHashMap);   // 영상통화 참여자들
        dialog.putExtra(VideoCallActivity.DATA_KEY_ROOM_ID, videoRoomID);  // 영상통화 비디오 룸 ID

        /*
            (주의) Service 에서 startActivity() 를 하기 위해서는 반드시 FLAG_ACTIVITY_NEW_TASK 플래그를 설정해줘야한다.
         */
        dialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 근데 왜 Task 를 새로 만들어야 할까?
        startActivity(dialog);

    }
}
