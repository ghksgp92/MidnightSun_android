package com.hyunju.jin.movie.activity.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.WalletService;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletTokenSendActivity extends SuperActivity {

    @BindView(R.id.ed_send_address) EditText ed_send_address;
    @BindView(R.id.ed_send_token) EditText ed_send_token;


    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator;
    @BindView(R.id.btn_send) Button btn_send;

    WalletService walletService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_token_send);
        ButterKnife.bind(this);

        initialize();
    }


    private void initialize(){
        //walletNodeService = RetrofitClient.getWalletNodeService();
        walletService = RetrofitClient.getWalletService();
    }


    @OnClick(R.id.btn_send)
    public void sendToken(){

        // 값 검사. null 이면 안되고 숫자는 양수만

        loading_indicator.smoothToShow();
        btn_send.setVisibility(View.GONE);

        HashMap<String, String> data_registerWalletAddress= new HashMap<>();
        data_registerWalletAddress.put("userCode", loginUser.getUserCode()+"");
        data_registerWalletAddress.put("address", ed_send_address.getText().toString());
        data_registerWalletAddress.put("val", ed_send_token.getText().toString());

        Call<ResponseData> call_registerWalletAddress = walletService.post("sendToken", data_registerWalletAddress);
        call_registerWalletAddress.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return;
                }

                Toast.makeText(getContext(), "토큰 전송 요청", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "requestRegisterWalletAddress() 실패");
                loading_indicator.smoothToHide();
                btn_send.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(getContext(), "토큰 전송 요청 (onFailure)", Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage());
                loading_indicator.smoothToHide();
                btn_send.setVisibility(View.VISIBLE);
            }
        });
    }
}
