package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.Actor;
import com.hyunju.jin.movie.datamodel.Director;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieReportData;
import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface RecommendService {

    @GET("client/recommend/{req}.php")
    Call<ResponseData> get(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/recommend/{req}.php")
    Call<ArrayList<Movie>> getMovieList(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/recommend/{req}.php")
    Call<ArrayList<Actor>> getActorList(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/recommend/{req}.php")
    Call<ArrayList<Director>> getDirectorLIst(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/recommend/{req}.php")
    Call<ArrayList<MovieReportData>> getReportDatas(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/recommend/{req}.php")
    Call<ArrayList<User>> getMates(@Path("req") String req, @QueryMap Map<String, String> data);

    @FormUrlEncoded
    @POST("client/recommend/{req}.php")
    Call<ResponseData> post(@Path("req") String req, @FieldMap Map<String, String> data);
}
