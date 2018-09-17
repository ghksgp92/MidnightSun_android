package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.Wallet;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface WalletNodeService {

    @GET("{req}")
    Call<Wallet> get(@Path("req") String req, @QueryMap Map<String, String> data);

    @POST("{req}")
    Call<Wallet> postBody(@Path("req") String req, @Body Wallet wallet);

}
