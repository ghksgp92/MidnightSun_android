package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Retrofit을 사용한..
 * 인터페이스 이름 바꾸는게 좋겠어.
 */

public interface UserService {

    @GET("/client/user/{reqPageName}.php")
    Call<ResponseData> get(@Path("reqPageName") String reqPageName, @QueryMap Map<String, String> data);

    @GET("/client/user/{reqPageName}.php")
    Call<User> getUser(@Path("reqPageName") String reqPageName, @QueryMap Map<String, String> data);

    @GET("/client/user/{reqPageName}.php")
    Call<ArrayList<User>> getUserList(@Path("reqPageName") String reqPageName, @QueryMap Map<String, String> data);

    @FormUrlEncoded
    @POST("client/user/{reqPageName}.php")
    Call<ResponseData> post(@Path("reqPageName") String reqPageName, @FieldMap Map<String, String> data);

    @Multipart
    @POST("client/user/{reqPageName}.php")
    Call<ResponseData> updateUserProfile(@Path("reqPageName") String reqPageName, @PartMap Map<String, RequestBody> data, @Part MultipartBody.Part file);
}
