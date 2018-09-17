package com.hyunju.jin.movie.activity.movie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Actor;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 특정 배우가 출연한 모든 영화를 보여준다.
 */
public class MoviesByActorActivity extends SuperActivity {

    public static final String KEY_ACTOR_CODE = "actorCode";

    @BindView(R.id.recycler_movies) RecyclerView recycler_movies;

    Actor actor; //

    MovieService movieService;  // 영화 목록 및 배우 정보를 조회하기 위한 HTTP 서비스 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies_by_actor);
        ButterKnife.bind(this);
        initialize();

    }

    private void initialize(){

        // 배우 정보를 로드한다.
        HashMap<String, String> data_getActor = new HashMap<>();
        data_getActor.put("actorCode", actor.getActorCode()+"");

        Call<Actor> call_getActor = movieService.getActor("getActor", data_getActor);
        call_getActor.enqueue(new Callback<Actor>() {
            @Override
            public void onResponse(Call<Actor> call, Response<Actor> response) {
                if(response.isSuccessful()){

                    return;
                }
            }

            @Override
            public void onFailure(Call<Actor> call, Throwable t) {

            }
        });

        // 배우가 출연한 영화 정보를 로드한다.
        HashMap<String, String> data_getMoviesByActor= new HashMap<>();
        data_getMoviesByActor.put("actorCode", actor.getActorCode()+"");

        Call<ArrayList<Movie>> call_data_getMoviesByActor = movieService.getMovieList("getMoviesByActor", data_getMoviesByActor);
        call_data_getMoviesByActor.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){

                    return;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {

            }
        });

    }


}
