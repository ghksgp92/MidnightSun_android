package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.adapter.TokenReceiptAdapter;
import com.hyunju.jin.movie.datamodel.TokenReceipt;
import com.hyunju.jin.movie.datamodel.Wallet;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.WalletNodeService;
import com.hyunju.jin.movie.network.WalletService;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyWalletActivity extends SuperActivity {

    @BindView(R.id.tv_user_id) TextView tv_user_id; // 사용자 ID
    @BindView(R.id.tv_wallet_address) TextView tv_wallet_address; // 지갑 주소
    @BindView(R.id.tv_token_balance) TextView tv_token_balance; // 현재 토큰 수
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator;

    @BindView(R.id.recycler_view_token_receipt) RecyclerView recycler_view_token_receipt;
    TokenReceiptAdapter tokenReceiptAdapter;
    ArrayList<TokenReceipt> tokenReceipts;

    Wallet wallet;
    WalletNodeService walletNodeService;
    WalletService walletService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            wallet = new Wallet();
            wallet.setAddress(bundle.getString("address"));
            if(StringUtils.isEmpty(wallet.getAddress())){
                finish();
            }
        }

        initialize();
    }

    private void initialize(){

        walletNodeService = RetrofitClient.getWalletNodeService();
        walletService = RetrofitClient.getWalletService();

        tv_user_id.setText(loginUser.getId());
        tv_wallet_address.setText(wallet.getAddress());

        tokenReceipts = new ArrayList<TokenReceipt>();
        tokenReceiptAdapter = new TokenReceiptAdapter(getContext(), tokenReceipts, loginUser);
        recycler_view_token_receipt.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_view_token_receipt.setAdapter(tokenReceiptAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // 화면이 전환될때마다 토큰 정보 다시 불러오기
        tv_token_balance.setText("");
        loading_indicator.smoothToShow();

        requesterTokenBalance();
        loadMyTokenReceipt();
    }

    /**
     * 서버에 토큰 수를 업데이트하라는 요청을 보내는 메서드.
     */
    private void requesterTokenBalance(){

        HashMap<String, String> data_requesterBalance = new HashMap<>();
        data_requesterBalance.put("userCode", loginUser.getUserCode()+"");
        data_requesterBalance.put("address", wallet.getAddress());

        Call<Void> call_requesterBalance = walletService.getNotReturn("requesterBalance", data_requesterBalance);
        call_requesterBalance.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.e(TAG, "requesterBalance 성공");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "requesterBalance 실패");
                Log.e(TAG, t.getMessage());
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 요청이 완료되면 토큰 수를 불러온다.
                HashMap<String, String> data_getBalance = new HashMap<>();
                data_getBalance.put("userCode", loginUser.getUserCode()+"");
                data_getBalance.put("address", wallet.getAddress());

                Call<ResponseData> call_getBalance = walletService.get("getBalance", data_getBalance);
                call_getBalance.enqueue(new Callback<ResponseData>() {
                    @Override
                    public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                        if(response.isSuccessful()){
                            int tokenBalance = Integer.parseInt(response.body().getMsg());
                            tv_token_balance.setText(tokenBalance+"");
                            loading_indicator.smoothToHide();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseData> call, Throwable t) {
                        Log.e(TAG, t.getMessage());
                    }
                });
            }
        }, 1000);
    }

    @OnClick(R.id.img_refresh)
    public void refresh(){
        // 화면이 전환될때마다 토큰 정보 다시 불러오기
        tv_token_balance.setText("");
        loading_indicator.smoothToShow();

        requesterTokenBalance();
        loadMyTokenReceipt();
    }

    private void loadMyTokenReceipt(){

        HashMap<String, String> data_loadMyTokenReceipt = new HashMap<>();
        data_loadMyTokenReceipt.put("userCode", loginUser.getUserCode()+"");
        Call<ArrayList<TokenReceipt>> call_loadMyTokenReceipt = walletService.getTokenReceipt("loadMyTokenReceipt", data_loadMyTokenReceipt);
        call_loadMyTokenReceipt.enqueue(new Callback<ArrayList<TokenReceipt>>() {
            @Override
            public void onResponse(Call<ArrayList<TokenReceipt>> call, Response<ArrayList<TokenReceipt>> response) {
                if(response.isSuccessful()){

                    tokenReceipts.clear();
                    tokenReceipts.addAll(response.body());
                    tokenReceiptAdapter.notifyDataSetChanged();

                    return;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<TokenReceipt>> call, Throwable t) {

            }
        });
    }

    @OnClick(R.id.btn_send_token)
    public void sendToken(){

        Intent sendToken = new Intent(getContext(), WalletTokenSendActivity.class);
        startActivity(sendToken);

    }

    // 개발용 메소드. 현재 사용자에게 임의로 포스팅 추천 토큰을 발행한다.
    @OnClick(R.id.img_recommend)
    public void testRecommend(){

        Random random = new Random();

        HashMap<String, String> data_recommendPosting = new HashMap<>();
        data_recommendPosting.put("userCode", "957");
        data_recommendPosting.put("receiverUserCode", loginUser.getUserCode()+"");

        Call<ResponseData> call_recommendPosting = walletService.post("recommendPosting", data_recommendPosting);
        call_recommendPosting.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    Log.e(TAG, "내글 추천하기 성공");
                    return;
                }
                Log.e(TAG, "내글 추천하기 실패");
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "내글 추천하기 실패 (onFailure)");
                Log.e(TAG, t.getMessage());
            }
        });

    }
}
