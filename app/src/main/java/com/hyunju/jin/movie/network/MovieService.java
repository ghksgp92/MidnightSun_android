package com.hyunju.jin.movie.network;

import com.hyunju.jin.movie.datamodel.Actor;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieCollection;

import java.util.ArrayList;
import java.util.List;
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
 * @author hjjin on 2018-05-28.
 *         Description:
 */

public interface MovieService {

    @GET("client/movie/{req}.php")
    Call<ResponseData> get(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/movie/{req}.php")
    Call<Movie> getMovie(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/movie/{req}.php")
    Call<Actor> getActor(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/movie/{req}.php")
    Call<ArrayList<Movie>> getMovieList(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/movie/{req}.php")
    Call<ArrayList<MovieCollection>> getMovieCollectionList(@Path("req") String req, @QueryMap Map<String, String> data);

    @GET("client/movie/{req}.php")
    Call<MovieCollection> getMovieCollection(@Path("req") String req, @QueryMap Map<String, String> data);

    @FormUrlEncoded
    @POST("client/movie/{req}.php")
    Call<ResponseData> post(@Path("req") String req, @FieldMap Map<String, String> data);

    @Multipart
    @POST("client/movie/{req}.php")
    Call<ResponseData> postMultiPart(@Path("req") String req,  @PartMap Map<String, RequestBody> data, @Part List<MultipartBody.Part> files);

    @Multipart
    @POST("client/movie/{req}.php")
    Call<Movie> getMovieByImageSearch(@Path("req") String req, @PartMap Map<String, RequestBody> data, @Part ArrayList<MultipartBody.Part> files);

}
