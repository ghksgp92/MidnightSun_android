package com.hyunju.jin.movie.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.user.LoginActivity;
import com.hyunju.jin.movie.activity.movie.UserMainActivity;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

/**
 * 앱이 처음 기동될 때 나타나는 스플래쉬 화면
 * 자동로그인 처리를 한다. 이전에 로그인한 기록이 있으면 UserMainActivity 로 이동하고 로그인 기록이 없다면 LoginActivity 로 이동한다.
 */
public class SplashActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;     // 구글 연동 로그인
    UserService userService;     // 서버에 HTTP 요청을 하는 객체.

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    // https://android.jlelse.eu/the-complete-android-splash-screen-guide-c7db82bce565
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_Launcher);
        setContentView(R.layout.activity_splash);

        initialize();

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                autoLoginCheck();   // 자동로그인을 해야하는지 확인한다.
            }
        }, 1000);   // 스플래쉬 화면이 보이도록 1초 뒤에 실행함.
    }

    public void initialize(){

        // 구글 로그인 처리를 위한 객체와 View 초기화 작업
        // 주석 좀 더 적어야할듯.
        GoogleSignInOptions gso
                = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(this);
        sharedEditor = SharedPreferencesBuilder.getSharedDefaultConfigEditor(this);

        userService = RetrofitClient.getUserService();
    }

    /**
     * 자동 로그인을 처리하는 메서드.
     * 가입 방법에 따라 자동 로그인 처리를 별개로 해줘야한다.
     * 로그인 정보가 없다면 자동로그인과 관련된 값들을 초기화 시켜준다.
     * @return
     */
    private boolean autoLoginCheck(){

        boolean autoLogin = false;

        // 이전에 로그인한 기록이 있는지 확인한다.
        String loginUser_authType = sharedPreferences.getString(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH, "");
        if( "email".equals(loginUser_authType) ){
            // [킵] 서버에서 계정 확인 후 자동로그인 처리. 꼭 필요한 작업인지 의문이라.
            autoLogin = true;

        }else if( "google".equals(loginUser_authType) ){
            GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);
            if( lastAccount != null ){
                // [킵] 서버에서 계정 확인 후 자동로그인 처리
                autoLogin = true;
            }
        }

        if(autoLogin){  // 자동로그인 한다.
            Intent intent = new Intent(this, UserMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        }else{
            // 자동로그인 관련된 값들을 초기화 시킨다.
            sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_CODE);
            sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_ID);
            sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH);
            sharedEditor.commit();

            // 로그인 화면으로 이동한다.
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        return autoLogin;
    }

}
