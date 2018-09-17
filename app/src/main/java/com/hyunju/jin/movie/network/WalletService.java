package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.TokenReceipt;
import com.hyunju.jin.movie.datamodel.Wallet;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface WalletService {

    @FormUrlEncoded
    @POST("/client/wallet/{req}.php")
    Call<ResponseData> post(@Path("req") String req, @FieldMap Map<String, String> data);

    @GET("client/wallet/{req}.php")
    Call<ArrayList<TokenReceipt>> getTokenReceipt(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/wallet/{req}.php")
    Call<ResponseData> get(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/wallet/{req}.php")
    Call<Void> getNotReturn(@Path("req") String req, @QueryMap Map<String, String> data);
}
