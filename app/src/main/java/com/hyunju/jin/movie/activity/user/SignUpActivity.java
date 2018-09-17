package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;
import com.hyunju.jin.movie.utils.StringValidator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 자체 회원가입을 하는 화면
 * 이메일, ID, 비밀번호가 필요하다.
 */

public class SignUpActivity extends SuperActivity {

    @BindView(R.id.ed_email) EditText ed_email;
    @BindView(R.id.ed_id) EditText ed_id;
    @BindView(R.id.ed_pwd1) EditText ed_pwd1;
    @BindView(R.id.ed_pwd2) EditText ed_pwd2;
    @BindView(R.id.validate_msg_email) TextView validate_msg_email;
    @BindView(R.id.validate_msg_id) TextView validate_msg_id;
    @BindView(R.id.validate_msg_pwd) TextView validate_msg_pwd;
    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){

        /*
            알아두기.
            가입요청 시, validate_msg_email 값이 공백인 경우 이메일 검사에 통과했다고 생각하므로 초기에 msg_email 텍스트뷰의 텍스트를 임의로 설정해야함.
            그 외에 가입에 필요한 필수 정보들(eamil, id, pwd)에도 동일하게 적용.
        */
        validate_msg_email.setVisibility(View.INVISIBLE);
        //validate_msg_email.setText("이메일을 입력해주세요.");
        validate_msg_email.setText("");
        validate_msg_id.setVisibility(View.INVISIBLE);
        //validate_msg_id.setText("ID를 입력해주세요.");
        validate_msg_id.setText("");
        validate_msg_pwd.setVisibility(View.INVISIBLE);
        //validate_msg_pwd.setText("비밀번호를 입력해주세요.");
        validate_msg_pwd.setText("");
        userService = RetrofitClient.getUserService();


        addListenerOnView();
    }

    private void addListenerOnView(){

        // 이메일 입력할때 마다 이메일 형식을 검사하도록 리스너 등록
        ed_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String email = ed_email.getText().toString();

                if( email == null || email.isEmpty() ){
                    validate_msg_email.setText("이메일을 입력해주세요.");
                    validate_msg_email.setVisibility(View.VISIBLE);
                    return;
                }else if( !StringValidator.checkEmail(email)){
                    validate_msg_email.setText("올바른 이메일이 아닙니다.");
                    validate_msg_email.setVisibility(View.VISIBLE);
                }else{
                    validate_msg_email.setText("");
                    validate_msg_email.setVisibility(View.INVISIBLE);
                }
            }
        });

        // 아이디 입력 시 마다 공백 및 유효성 검사하도록 리스너 등록
        ed_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0){
                    validate_msg_id.setText(StringValidator.checkID(ed_id.getText().toString()));
                    validate_msg_id.setVisibility(View.VISIBLE);
                }else{
                    validate_msg_id.setText("ID를 입력해주세요.");
                    validate_msg_id.setVisibility(View.VISIBLE);
                }
            }
        });

        // 비밀번호 입력시 마다 비밀번호 정책 검사하도록 리스너 등록
        ed_pwd1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pwd1 = ed_pwd1.getText().toString();

                if( pwd1 == null || pwd1.isEmpty() ){
                    validate_msg_pwd.setText("비밀번호를 입력해주세요.");
                    validate_msg_pwd.setVisibility(View.VISIBLE);
                    return;
                }

                String err_msg = StringValidator.checkPwd(pwd1);
                if( err_msg.length() != 0 ){    // 구지 이렇게 작성할 필요없는 코드임..
                    validate_msg_pwd.setText(err_msg);
                    validate_msg_pwd.setVisibility(View.VISIBLE);
                }else {
                    validate_msg_pwd.setText("");
                    validate_msg_pwd.setVisibility(View.INVISIBLE);
                }
            }
        });

        // 비밀번호 확인 입력시 마다 비밀번호 정책 검사하도록 리스너 등록
        ed_pwd2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pwd1 = ed_pwd1.getText().toString();
                String pwd2 = ed_pwd2.getText().toString();

                if( pwd2 == null || pwd2.isEmpty() ){
                    validate_msg_pwd.setText("비밀번호를 입력해주세요.");
                    validate_msg_pwd.setVisibility(View.VISIBLE);
                    return;
                }else if( pwd2 != null && !pwd2.isEmpty() && !pwd2.equals(pwd1) ){
                    validate_msg_pwd.setText("비밀번호가 다릅니다.");
                    validate_msg_pwd.setVisibility(View.VISIBLE);
                    return;
                }

                String err_msg = StringValidator.checkPwd(pwd2);
                if( err_msg.length() != 0 ){
                    validate_msg_pwd.setText(err_msg);
                    validate_msg_pwd.setVisibility(View.VISIBLE);
                }else {
                    validate_msg_pwd.setText("");
                    validate_msg_pwd.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @OnClick(R.id.btn_sign_up)
    public void signUp(){

        // validate_msg_email 값이 공백인 경우 이메일 값 검사에 통과했다고 판단한다.
        if( StringUtils.isEmpty(ed_email.getText()) || StringUtils.isNotEmpty(validate_msg_email.getText()) ){
            validate_msg_email.setText("이메일을 확인해주세요.");
            validate_msg_email.setVisibility(View.VISIBLE);
            return;
        } else if ( StringUtils.isEmpty(ed_id.getText()) || ed_id.getText().length() < 4){
            validate_msg_id.setText("ID를 4자 이상 입력해주세요.");
            validate_msg_id.setVisibility(View.VISIBLE);
            return;
        }else if ( StringUtils.isEmpty(ed_pwd1.getText()) || StringUtils.isEmpty(ed_pwd2.getText()) || StringUtils.isNotEmpty(validate_msg_pwd.getText()) ){
            validate_msg_pwd.setText("비밀번호를 확인해주세요.");
            validate_msg_pwd.setVisibility(View.VISIBLE);
            return;
        }

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("authType", "email");
        data.put("email", ed_email.getText().toString());
        data.put("id", ed_id.getText().toString());
        data.put("pwd1", ed_pwd1.getText().toString());
        data.put("pwd2", ed_pwd2.getText().toString());
        //data.put("sex", ( selectedSex == rbtn_sex_female.getId() ? "F" : "M" ) );

        Call<ResponseData> resCall = userService.post("signUp", data);
        resCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {

                if(response.isSuccessful()){
                    ResponseData resData = response.body();

                    if(resData.getCode() == 1){

                        SharedPreferences.Editor sharedEditor;
                        sharedEditor = SharedPreferencesBuilder.getSharedDefaultConfigEditor(getContext());

                        sharedEditor.putInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, Integer.parseInt(resData.getMsg()));
                        sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_ID, ed_id.getText().toString());
                        sharedEditor.putString(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH, "email");
                        sharedEditor.commit();

                        // 추가 정보를 입력하는 화면으로 이동한다.
                        // (참고) 현재 추가정보를 입력하지 않았을 경우에 대한 처리가 되어있지 않음.
                        Intent intent = new Intent(getContext(), AdditionalInfoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                    }else{
                        // 가입 실패 시, 이유를 보여준다.
                        switch (resData.getCode()){
                            case 1000:
                                validate_msg_email.setText(resData.getMsg());
                                break;
                            case 1001:
                                validate_msg_id.setText(resData.getMsg());
                                break;
                            case 1002:
                                validate_msg_pwd.setText(resData.getMsg());
                                break;
                        }
                    }

                }else{
                    Log.e(TAG, "signUp() > onResponse()");
                    Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "signUp() > onFailure");
                // 인터넷 연결이 안된 상태면 여기로 떨어지려나?
            }
        });

    }


}
