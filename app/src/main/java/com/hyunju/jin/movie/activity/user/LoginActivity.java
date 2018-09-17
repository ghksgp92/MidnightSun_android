package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.movie.UserMainActivity;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * 사용자가 로그인하거나 회원가입을 할 수 있는 화면
 *
 *  현재 구글연동 로그인 시, FCM 등록하는 부분이 빠져있음.
 */
public class LoginActivity extends SuperActivity {

    @BindView(R.id.ed_login_id) EditText ed_login_id;
    @BindView(R.id.ed_login_pwd) EditText ed_login_pwd;

    // 구글 연동 로그인
    @BindView(R.id.btn_sign_in_google) SignInButton btn_sign_in_google;
    GoogleSignInClient mGoogleSignInClient;
    private final int GOOGLE_SIGN_IN = 1000;

    UserService userService; // 서버와 HTTPS 통신
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initialize();
    }

    public void initialize(){

        // 구글 로그인 처리를 위한 객체와 View 초기화 작업
        // 주석 좀 더 달아주세요.
        GoogleSignInOptions gso
                = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        btn_sign_in_google.setSize(SignInButton.SIZE_WIDE);
        btn_sign_in_google.setColorScheme(SignInButton.COLOR_LIGHT);

        sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        sharedEditor = SharedPreferencesBuilder.getSharedDefaultConfigEditor(getContext());

        userService = RetrofitClient.getUserService();

        mGoogleSignInClient.signOut();

        addListenerOnView();
    }

    // [킵] 작업 안해둠
    private void addListenerOnView() {

        // 아이디 입력 시 로그인 버튼 활성화 여부를 검사한다.
        ed_login_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 비밀번호 입력 시 로그인 버튼 활성화 여부를 검사한다.
        ed_login_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case GOOGLE_SIGN_IN:    // 구글 연동 로그인

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleGoogleSignInResult(task);
                break;
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null){
                String email = account.getEmail();
                final String id = email.split("@")[0];

                HashMap<String, String> data_signUp = new HashMap<String, String>();
                data_signUp.put("authType", "google");
                data_signUp.put("email", email);
                data_signUp.put("id", id);

                Call<ResponseData> request_signUp = userService.post("signUp", data_signUp);
                request_signUp.enqueue(new Callback<ResponseData>() {
                    @Override
                    public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                        if(response.isSuccessful()){

                            ResponseData resData = response.body();

                            if( resData.getCode() == 1 ){ // 처음으로 연동 로그인한 계정인 경우

                                sharedEditor.putInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, Integer.parseInt(resData.getMsg()));
                                sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_ID, id);
                                sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH, "google");
                                sharedEditor.commit();

                                // 취향 정보를 입력하는 화면으로 이동
                                //Intent intent = new Intent(getContext(), AdditionalInfoActivity.class);
                                Intent intent = new Intent(getContext(), UserMainActivity.class);

                                Toast.makeText(getContext(), "환영합니다.", Toast.LENGTH_SHORT).show();

                                //Intent intent = new Intent(getContext(), MovieSearchResultListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }else if( resData.getCode() == 2 ){ // 이미 가입되어있는 구글 계정인 경우,

                                sharedEditor.putInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, Integer.parseInt(resData.getMsg()));
                                sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_ID, id);
                                sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH, "google");
                                sharedEditor.commit();

                                Toast.makeText(getContext(), "환영합니다.", Toast.LENGTH_SHORT).show();

                                //로그인
                                Intent intent = new Intent(getContext(), UserMainActivity.class);
                                //Intent intent = new Intent(getContext(), MovieSearchResultListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }else{
                                Toast.makeText(getContext(), resData.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.w(TAG, "handleGoogleSignInResult");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData> call, Throwable t) {
                        Log.e(TAG, "signInResult:failed code=" + t.getMessage());
                    }
                });

            }else{
                // 이 경우는 뭐지?
                // 로그인된 계정을 제거해야하나?
            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * ID, Password 입력 값에 따라 로그인 버튼 활성화 상태를 변경하기 위한 메서드.
     * @param disable
     */
    private void changeDisableOnLoginBtn(boolean disable){
        // disable 라는 의미가 좀 모호하지 않니?

    }

    /**
     * 비밀번호 찾기로 이동
     */
    @OnClick(R.id.tv_forgot_pwd)
    public void forgotPassword(){
        Intent forgotPwd = new Intent(getContext(), ForgotPasswordActivity.class);
        startActivity(forgotPwd);
    }

    /**
     * 아이디로 로그인
     */
    @OnClick(R.id.btn_login)
    public void requestUserLogin(){

        // ID, PWD 공백 검사
        if(StringUtils.isEmpty(ed_login_id.getText())){
            Toast.makeText(getContext(), "ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(StringUtils.isEmpty(ed_login_pwd.getText())){
            Toast.makeText(getContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버에 로그인 요청
        HashMap<String, String> data_login = new HashMap<String, String>();
        final String id = ed_login_id.getText().toString();
        data_login.put("id", id);
        data_login.put("pwd", ed_login_pwd.getText().toString());
        data_login.put("fcmInstanceId", FirebaseInstanceId.getInstance().getToken());

        Call<ResponseData> request_login = userService.post("login", data_login);
        request_login.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    ResponseData responseData = response.body();
                    if(responseData.getCode() == 1){

                        sharedEditor.putInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, Integer.parseInt(responseData.getMsg()));
                        sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_ID, id);
                        sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH, "email");
                        sharedEditor.commit();

                        Intent intent = new Intent(getContext(), UserMainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }else{
                        // 자세한 로그인 실패 사유는 보여주지 않는다.
                        // 로그인 실패시 실패 횟수 카운트는 킵. 당장 할게 많아져서
                        Toast.makeText(getContext(), "ID와 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Log.e(TAG, "로그인 요청 실패");
                    Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {

                Log.e(TAG, "로그인 요청실패 (onFailure)");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 회원가입으로 이동
     */
    @OnClick(R.id.tv_sign_up)
    public void signUp(){
        Intent signUp = new Intent(getContext(), SignUpActivity.class);
        startActivity(signUp);
        overridePendingTransition(R.anim.slide_in_up, 0);
    }

    /**
     * 구글 연동 로그인 API 요청
     */
    @OnClick(R.id.btn_sign_in_google)
    public void signUpByGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

}
