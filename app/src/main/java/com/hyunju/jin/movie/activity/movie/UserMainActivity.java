package com.hyunju.jin.movie.activity.movie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.BottomMenuActivity;
import com.hyunju.jin.movie.adapter.movie.MovieRankingItemAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 메인 화면. 로그인 하거나 [MOVIE] 버튼을 누르면 볼 수 있다.
 */
public class UserMainActivity extends BottomMenuActivity {

    // 화면 하단 메뉴 View
    @BindView(R.id.img_bottom_menu_home) ImageView img_bottom_menu_home;
    @BindView(R.id.tv_bottom_menu_label_home) TextView tv_bottom_menu_label_home;

    // 영화 순위를 보여주기 위해 사용되는 뷰와 객체
    @BindView(R.id.rc_movie_ranking_list) RecyclerView rc_movie_ranking_list;
    ArrayList<Movie> movieRankingList;
    MovieRankingItemAdapter movieRankingItemAdapter;
    int movieRankingPage;

    // VOD 로 시청 가능한 영화 목록을 보여주는 뷰
    @BindView(R.id.rc_streaming_available_list) RecyclerView rc_streaming_available_list;
    ArrayList<Movie> movieStreamingAvailableList;
    MovieRankingItemAdapter movieStreamingAvailableItemAdapter;

    MovieService movieService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize(){

        // 하단 메뉴에서 현재 화면 버튼 표시 변경
        img_bottom_menu_home.setImageResource(R.drawable.ic_menu_home_on);
        tv_bottom_menu_label_home.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null));

        movieService = RetrofitClient.getMovieService();
        movieRankingList = new ArrayList<Movie>();
        movieRankingItemAdapter = new MovieRankingItemAdapter(getContext(), movieRankingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //layoutManager.setStackFromEnd(true);
        rc_movie_ranking_list.setHasFixedSize(false);
        rc_movie_ranking_list.setLayoutManager(layoutManager);
        rc_movie_ranking_list.setAdapter(movieRankingItemAdapter);

        // 스트리밍 가능 영상
        movieStreamingAvailableList = new ArrayList<Movie>();
        movieStreamingAvailableItemAdapter = new MovieRankingItemAdapter(getContext(), movieStreamingAvailableList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        //layoutManager2.setStackFromEnd(true);
        rc_streaming_available_list.setHasFixedSize(false);
        rc_streaming_available_list.setLayoutManager(layoutManager2);
        rc_streaming_available_list.setAdapter(movieStreamingAvailableItemAdapter);

        // 인기 영화 정보 로드
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("offset", movieRankingPage*10+"");
        data.put("limit", 10+"");
        Call<ArrayList<Movie>> resCall = movieService.getMovieList("getTodayMovieRanking", data);
        resCall.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    // 페이징 추가하기. 매일 50개씩임
                    movieRankingList.clear();
                    movieRankingList.addAll(response.body());
                    movieRankingItemAdapter.notifyDataSetChanged();
                    rc_movie_ranking_list.setVerticalScrollbarPosition(0);
                    rc_movie_ranking_list.smoothScrollToPosition(0);

                    return;
                }
                Log.e(TAG, "인기 영화 로드 실패");

            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                Log.e(TAG, "인기 영화 로드 실패 (onFailure)");
            }
        });

        // 스트리밍 가능 영화 로드 [킵] 변수이름 좀..
        HashMap<String, String> data2 = new HashMap<String, String>();
        data.put("offset", movieRankingPage*10+"");
        data.put("limit", 10+"");
        Call<ArrayList<Movie>> resCall2 = movieService.getMovieList("getStreamingAvailableList", data);
        resCall2.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    // 페이징 추가하기. 매일 50개씩임
                    movieStreamingAvailableList.clear();
                    movieStreamingAvailableList.addAll(response.body());
                    movieStreamingAvailableItemAdapter.notifyDataSetChanged();
                    rc_streaming_available_list.setVerticalScrollbarPosition(0);
                    rc_streaming_available_list.smoothScrollToPosition(0);
                    return;
                }
                Log.e(TAG, "스트리밍 가능 영화 로드 실패");
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                Log.e(TAG, "스트리밍 가능 영화 로드 실패 (onFailure)");
            }
        });

        addListenerOnView();
    }

    private void addListenerOnView(){

    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    /**
     * 검색어를 통해 영화를 검색하는 화면으로 이동한다.
     */
    @OnClick(R.id.btn_search)
    public void search(){
        Intent movieSearch = new Intent(getContext(), MovieSearchResultListActivity.class);
        movieSearch.putExtra(MovieSearchResultListActivity.DATA_KEY_CALL, "search");
        startActivity(movieSearch);
    }

}
