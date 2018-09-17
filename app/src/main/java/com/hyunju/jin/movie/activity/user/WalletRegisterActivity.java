package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Wallet;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.WalletNodeService;
import com.hyunju.jin.movie.network.WalletService;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRegisterActivity extends SuperActivity {

    @BindView(R.id.ed_account_private_key) EditText ed_account_private_key;
    @BindView(R.id.btn_register) Button btn_register;
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator;

    WalletNodeService walletNodeService;
    WalletService walletService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_register);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize(){
        walletNodeService = RetrofitClient.getWalletNodeService();
        walletService = RetrofitClient.getWalletService();
    }

    /**
     * 사용자가 입력한 개인키로 지갑 주소를 구하는 메소드.
     */
    @OnClick(R.id.btn_register)
    public void walletRegister(){

        loading_indicator.smoothToShow();
        btn_register.setVisibility(View.GONE);

        // 암호키를 서버에 보낸다.
        Wallet wallet = new Wallet(ed_account_private_key.getText().toString());
        Call<Wallet> call_registerAccount = walletNodeService.postBody("registerAccount", wallet);
        call_registerAccount.enqueue(new Callback<Wallet>() {
            @Override
            public void onResponse(Call<Wallet> call, Response<Wallet> response) {
                if(response.isSuccessful()){
                    requestRegisterWalletAddress(response.body().getAddress());
                    return;
                }

                Toast.makeText(getContext(), "지갑 주소 구하기 실패", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "walletRegister() 실패");
                loading_indicator.smoothToHide();
                btn_register.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Wallet> call, Throwable t) {
                Toast.makeText(getContext(), "지갑 주소 구하기 실패 (onFailure)", Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage());
                loading_indicator.smoothToHide();
                btn_register.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 지갑 주소를 사용자 정보에 저장하도록 서버에 요청한다.
     * 리턴 후 지갑정보 화면으로 이동한다.
     * @param address
     */
    private void requestRegisterWalletAddress(final String address){

        HashMap<String, String> data_registerWalletAddress= new HashMap<>();
        data_registerWalletAddress.put("userCode", loginUser.getUserCode()+"");
        data_registerWalletAddress.put("address", address);

        Call<ResponseData> call_registerWalletAddress = walletService.post("registerWalletAddress", data_registerWalletAddress);
        call_registerWalletAddress.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    Intent myWallet = new Intent(getContext(), MyWalletActivity.class);
                    myWallet.putExtra("address", address);
                    startActivity(myWallet);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return;
                }

                Toast.makeText(getContext(), "지갑정보 등록 실패", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "requestRegisterWalletAddress() 실패");
                loading_indicator.smoothToHide();
                btn_register.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(getContext(), "지갑정보 등록 실패 (onFailure)", Toast.LENGTH_SHORT).show();
                Log.e(TAG, t.getMessage());
                loading_indicator.smoothToHide();
                btn_register.setVisibility(View.VISIBLE);
            }
        });


    }
}
