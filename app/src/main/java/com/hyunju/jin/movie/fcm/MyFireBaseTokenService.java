package com.hyunju.jin.movie.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * 해당 단말기의 FCM InstanceId 를 생성한다.
 */
public class MyFireBaseTokenService extends FirebaseInstanceIdService {

    private final String TAG = "MyFireBaseTokenService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "생성된 토큰 :"+refreshedToken);

        // 서버의 USR 테이블에 토큰을 저장하는 작업은 LoginActivity 에서 로그인 성공 시 하도록 되어있다.

        // 17-11-15 기준으로
        // 토큰은 단말기당 1개 생성되고, 앱을 재설치하면 이 메소드가 다시 호출되기 때문에 토큰이 새로 생성된다.
        // 따라서 한번 생성된 토큰은 재설치하기 전까지 (= 현재 이 메소드가 호출되기 전까지) 항상 같은 값을 리턴한다.

        // 1인 1단말기라 가정하고 USR 테이블에 토큰을 저장함
    }

}
