package com.hyunju.jin.movie.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
    SharedPreferences 작업과 관련된 메서드, Key 값을 모아둔 클래스.
 */

public class SharedPreferencesBuilder {

    /**
     * 앱 내부의 Key-Value 저장소인 SharedPreferences 접근 객체를 리턴한다.
     * 접근객체? 뭔가 이상해.
     */
    public static SharedPreferences getSharedDefaultConfig(Context context){

        SharedPreferences shared = context.getSharedPreferences(FN_APP_CONFIG, Context.MODE_PRIVATE | Context.MODE_APPEND);

        /*
            MODE_WORLD_READABLE 은 Android 4.2(젤리빈) 부터 사용하지 않는다. (API level 17)
            모드에 대해서는 거의 신경안쓰고 있었는데, 정확히 알아둘 필요가 있어보인다. 현재까지 문제가 없었던 이유는 내부 저장소를 내 앱에서만 접근했기 때문이다.
         */

        return shared;
    }

    public static SharedPreferences.Editor getSharedDefaultConfigEditor(Context context){
        return getSharedDefaultConfig(context).edit();
    }

    // 앱 내부저장소 파일 명. (FN: File name 의 약자)
    private static final String FN_APP_CONFIG = "movie.config";  // 자동로그인, 이어보기 내역이 저장되는 파일명

    // 자동로그인 처리와 관련된 Key
    public static final String USR_LOGIN_USER_CODE = "loginUserCode";       // 사용자 코드
    public static final String USR_LOGIN_USER_ID = "loginUser";             // 사용자 ID
    public static final String USR_LOGIN_USER_AUTH = "loginUser_authType";  // 사용자 가입 형태 (이메일, 구글)

    // 이어보기 내역이 저장된 Key
    public static final String KEY_WATCHING_MOVIE = "watchingMovieList";     // 이어보기 내역

}
