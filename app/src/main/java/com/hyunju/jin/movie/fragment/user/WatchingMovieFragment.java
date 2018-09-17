package com.hyunju.jin.movie.fragment.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.VideoPlayerActivity;
import com.hyunju.jin.movie.activity.user.MypageListener;
import com.hyunju.jin.movie.adapter.user.WatchingMovieAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WatchingMovieFragment extends Fragment implements MypageListener{

    private String TAG = "WatchingMovieFragment";

    @BindView(R.id.layout_watching_movie_empty) LinearLayout layout_watching_movie_empty;
    @BindView(R.id.recycler_watching_movie_list) RecyclerView recycler_watching_movie_list;

    public Gson gson;       // 기기에 저장된 json 형태의 이어보기 기록을 자바 객체로 변환하기 위해 선언
    ArrayList<Movie> watchingMovieList;
    WatchingMovieAdapter watchingMovieAdapter;

    public WatchingMovieFragment(){}

    public static WatchingMovieFragment getInstance(){
        WatchingMovieFragment frg = new WatchingMovieFragment();
        Bundle bundle = new Bundle();
        frg.setArguments(bundle);
        return frg;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //movie = (movie) getArguments().getSerializable("movie");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_watching_movie, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 필요한 초기화 Activity로 치면 onCreate 겠지?
        // 아. 생명주기 다 까먹어벌임....

        gson = new GsonBuilder().create();
        watchingMovieList = new ArrayList<Movie>();
        watchingMovieAdapter = new WatchingMovieAdapter(getContext(), watchingMovieList, this);
        recycler_watching_movie_list.setAdapter(watchingMovieAdapter);
        recycler_watching_movie_list.setLayoutManager(new LinearLayoutManager(getContext()));


    }

    @Override
    public void onResume() {
        super.onResume();
        loadWatchingMovieList();
    }

    /**
     * 사용자 기기에 저장된 이어보기 정보를 조회해서 보여준다.
     */
    private void loadWatchingMovieList(){

        // 사용자의 이어보기 정보는 기기에서 관리하는데 하나의 기기에서 여러 사용자가 로그인해서 사용할 경우를 생각해
        // '사용자코드+watchingMovieList'를 key 값으로 해서 사용자를 구분한다.

        SharedPreferences sharedPreferencesBuilder = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferencesBuilder.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);  // 현재 로그인한 사용자 userCode 를 구하고

        String watchingMoieDataKey = loginUserCode+SharedPreferencesBuilder.KEY_WATCHING_MOVIE;

        String watchingMovieValue = sharedPreferencesBuilder.getString( watchingMoieDataKey, ""); // 이어보기 데이터를 조회한다.

        if(StringUtils.isNotEmpty(watchingMovieValue)){

            HashMap<Integer, Movie> loadWatchingMovieList = new HashMap<Integer, Movie>();
            try {
                loadWatchingMovieList.putAll( (HashMap<Integer, Movie>) gson.fromJson(watchingMovieValue, new TypeToken<HashMap<Integer, Movie>>() {}.getType()));

                watchingMovieList.addAll(loadWatchingMovieList.values());

                layout_watching_movie_empty.setVisibility(View.GONE);
                recycler_watching_movie_list.setVisibility(View.VISIBLE);
                watchingMovieAdapter.notifyDataSetChanged();

            }catch (Exception e){
                Log.e(TAG, "이어보기 기록 조회 실패");

                // 이어보기 기록 조회에 실패할 경우는 개발 중 변경사항으로 실패했다고 가정하고
                // 화면에 이를 표시하고
                recycler_watching_movie_list.setVisibility(View.GONE);
                layout_watching_movie_empty.setVisibility(View.VISIBLE);
                // 이어보기 기록을 없애도록 한다.
                SharedPreferences.Editor sharedEditor = SharedPreferencesBuilder.getSharedDefaultConfigEditor(getContext());
                sharedEditor.remove(watchingMoieDataKey);
                sharedEditor.commit();
            }

        }else{ // 이어보기 기록이 없을 경우 화면에 이를 표시한다.
            recycler_watching_movie_list.setVisibility(View.GONE);
            layout_watching_movie_empty.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.tv_search_streaming_movie)
    public void searchStreamingAvailableMovie(){

    }

    /**
     * 이어보기에서 선택한 영화를 스트리밍 한다. 이전에 보던 기록이 있기때문에 이어보기를 할 것인지 물어본다.
     * (참고) 이 코드는 MovieDetailActivity 에서도 완전히 동일하게 사용하고 있음.
     */
    @Override
    public void startStreaming(final Movie movie) {
        // 이전에 시청 기록이 있는지 확인한다.
        boolean checkWatchingMovie = false;

        SharedPreferences sharedPreferencesBuilder = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferencesBuilder.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);  // 현재 로그인한 사용자 userCode 를 구하고
        String watchingMoieDataKey = loginUserCode+SharedPreferencesBuilder.KEY_WATCHING_MOVIE;
        String watchingMovieValue = sharedPreferencesBuilder.getString(watchingMoieDataKey, "");

        // 이어보기 기록에 해당 영화가 있는지 확인한다.
        if(StringUtils.isNotEmpty(watchingMovieValue)){

            HashMap<Integer, Movie> watchingMovieList = new HashMap<Integer, Movie>();

            try {
                watchingMovieList.putAll( (HashMap<Integer, Movie>) gson.fromJson(watchingMovieValue, new TypeToken<HashMap<Integer, Movie>>() {}.getType()));
                checkWatchingMovie = ( watchingMovieList.get(movie.getMovieCode()) != null ) ;

            }catch (Exception e){
                Log.e(TAG, "이어보기 기록 조회 실패");
            }

            // 시청기록이 있다면 이어보기를 할 것인지 묻는다.
            if(checkWatchingMovie){

                final long restroePalyPosition = watchingMovieList.get(movie.getMovieCode()).getPlayTime();
                // [수정] 라이브러리 제거함
                /*CustomAlertDialogue.Builder alert = new CustomAlertDialogue.Builder(getContext())
                        .setStyle(CustomAlertDialogue.Style.DIALOGUE)
                        .setCancelable(false)
                        .setTitle("이어보기 기록이 있어요.")
                        .setMessage("이어보시겠어요?")
                        .setNegativeText("처음부터")
                        .setNegativeColor(R.color.positive)
                        .setOnNegativeClicked(new CustomAlertDialogue.OnNegativeClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
                                vod.putExtra("playingVideo", movie);
                                startActivity(vod);
                                dialog.dismiss();
                            }
                        })
                        .setPositiveText("이어보기")
                        .setPositiveColor(R.color.negative)
                        .setPositiveTypeface(Typeface.DEFAULT_BOLD)
                        .setOnPositiveClicked(new CustomAlertDialogue.OnPositiveClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);

                                // 이어보기를 한다면 해당 위치에서부터 재생할 수 있도록, 파라미터로 영화정보와 재생시간을 보낸다.
                                vod.putExtra(VideoPlayerActivity.DATA_KEY_PLAY_MOVIE, movie);
                                vod.putExtra(VideoPlayerActivity.DATA_KEY_PLAY_POSITION, restroePalyPosition);
                                startActivity(vod);
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .setDecorView(getActivity().getWindow().getDecorView())
                        .build();
                alert.show();*/

            }else{ // 처음 보는 영화라면 바로 재생한다.
                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
                vod.putExtra("playingVideo", movie);
                startActivity(vod);
            }

        }else{ // 이어보기 기록이 아예 없을 경우 바로 재생한다.

            Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
            vod.putExtra("playingVideo", movie);
            startActivity(vod);
        }
    }
}
