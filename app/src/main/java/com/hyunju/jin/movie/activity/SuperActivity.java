package com.hyunju.jin.movie.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

/**
 * 모든 액티비티가 상속받는 액티비티
 * 액티비티에서 공통으로 사용하는 변수들을 선언 및 생성한다.
 */
public class SuperActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();  // 현재 액티비티 이름을 나타낸다. 로그를 남길때 사용한다.
    public Handler mHandler;
    public Gson gson; // 서버와 json 형태로 데이터를 주고받기 위해 선언함.
    public User loginUser; // 현재 로그인한 사용자 정보. 사용자 정보가 서버와 통신하거나 화면에 표시할 일이 많아서 객체에 저장해두고 사용한다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
        gson = new GsonBuilder().create();

        // 현재 로그인한 사용자 정보로 User 객체 생성
        loginUser = new User();

        // 로그인 후 로그아웃을 하기 전까지 SharedPreferences 에 로그인 정보가 저장되어 있다.
        // 현재는 사용자 코드와 아이디만 저장한다. 저장된 이름은 SharedPreferencesBuilder 아래에서 관리한다.
        SharedPreferences sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferences.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);
        loginUser.setUserCode(loginUserCode);
        loginUser.setId(sharedPreferences.getString(SharedPreferencesBuilder.USR_LOGIN_USER_ID, ""));

    }

    /**
     * 현재 Activity 를 리턴한다. 익명 이너클래스에서 Activity 에 접근하기 위해 정의함.
     * 예를들어, Retrofit Callback 함수에서 액티비티에 접근해야할 경우 사용할 수 있다.
     * @return
     */
    public SuperActivity getActivity(){ return this; }

    /**
     * 현재 Context 를 리턴한다.
     * getActivity() 와 같이 익명 이너클래스에서 Activity 에 접근하기 위해 정의함.
     *
     * 내가 만들긴 했으나.. 그냥 getActivity() 를 사용하면 될 것 같은데 이 메소드는 왜 만들었을까?
     * @return
     */
    public Context getContext(){ return this; }

}
