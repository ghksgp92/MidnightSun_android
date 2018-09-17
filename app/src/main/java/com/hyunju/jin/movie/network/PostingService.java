package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.datamodel.PostingComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
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
 * 별점/코멘트 추가, 조회, 수정
 * 포스팅 등록, 수정, 삭제 등..
 */
public interface PostingService {

    ///////////////// GET 요청 /////////////////
    
    @GET("client/posting/{req}.php")
    Call<Posting> getPosting(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/posting/{req}.php")
    Call<ArrayList<Posting>> getPostingList(@Path("req") String req, @QueryMap Map<String, String > data);

    @GET("client/posting/{req}.php")
    Call<ArrayList<PostingComment>> getPostingCommentList(@Path("req") String req, @QueryMap Map<String, String > data);
    
    ///////////////// POST 요청 /////////////////
    
    @Multipart
    @POST("client/posting/{req}.php")
    Call<ResponseData> postMultiPart(@Path("req") String req, @PartMap Map<String, RequestBody> data, @Part ArrayList<MultipartBody.Part> files);

    @FormUrlEncoded
    @POST("client/posting/{req}.php")
    Call<PostingComment> postingCommentUPDATE (@Path("req") String req, @FieldMap Map<String, String> data);

}
