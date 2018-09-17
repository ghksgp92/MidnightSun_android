package com.hyunju.jin.movie.activity.user;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import butterknife.ButterKnife;

/**
 * 아직 이 액티비티를 호출하는 구간이 없음.
 */

/**
 * 이어보기 목록을 보여주는 화면
 */
public class WatchingMovieActivity extends SuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watching_movie);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize() {
        SharedPreferences sharedPreferencesBuilder = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        String watchingMovieValue = sharedPreferencesBuilder.getString(SharedPreferencesBuilder.KEY_WATCHING_MOVIE, "");

        HashMap<Integer, Movie> watchingMovieList = new HashMap<Integer, Movie>();
        if (StringUtils.isNotEmpty(watchingMovieValue)) { // 이어보기 기록이 있는지 확인한다.
            try {
                watchingMovieList.putAll((HashMap<Integer, Movie>) gson.fromJson(watchingMovieValue, new TypeToken<HashMap<Integer, Movie>>() {
                }.getType()));

            } catch (Exception e) {

            }
        }
    }
}
