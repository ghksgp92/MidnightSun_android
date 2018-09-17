package com.hyunju.jin.movie.fragment;


import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.community.MyCollectionInfoEditActivity;
import com.hyunju.jin.movie.adapter.movie.MovieCollectionAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollectionListDialogFragment extends DialogFragment implements MyCollectionListListener {

    @BindView(R.id.recycler_my_collection_list) RecyclerView recycler_my_collection_list;
    ArrayList<MovieCollection> movieCollectionList;
    MovieCollectionAdapter movieCollectionAdapter;

    private Movie movie;    // 현재 컬렉션에 추가하려는 영화 정보를 담은 객체
    private MovieService movieService;

    private final String TAG = "MyCollectionList";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_show_collection, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        getDialog().getWindow().setLayout((width-100), ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(this, view);

        movieService = RetrofitClient.getMovieService(); // movieCollectonGETbyMovie
        movieCollectionList = new ArrayList<MovieCollection>();
        movieCollectionAdapter = new MovieCollectionAdapter(getActivity(), this, movieCollectionList);
        recycler_my_collection_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_my_collection_list.setAdapter(movieCollectionAdapter);

        Bundle bundle = getArguments();
        movie = (Movie) bundle.getSerializable("movie");    // 이런 키값들을 어디에 정리해두는게 맞을까..

        return view;
    }


    /**
     * 새로운 컬렉션을 만들고 그 컬렉션에 영화를 추가하는 화면으로 이동한다.
     */
    @OnClick(R.id.tv_new_collection)
    public void addCollection(){
        Intent newCollection = new Intent(getActivity(), MyCollectionInfoEditActivity.class);
        newCollection.putExtra(MyCollectionInfoEditActivity.KEY_FLAG, MyCollectionInfoEditActivity.FLAG_ADD);
        ArrayList<Movie> collectionList = new ArrayList<Movie>();
        collectionList.add(movie);
        newCollection.putExtra(MyCollectionInfoEditActivity.KEY_COLLECTION_LIST, collectionList);
        startActivity(newCollection);
    }


    @OnClick(R.id.btn_dismiss)
    public void close(){
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> data_movieCollectonGETbyMovie = new HashMap<String, String>();
        SharedPreferences sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(getActivity());
        int loginUserCode = sharedPreferences.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);
        data_movieCollectonGETbyMovie.put("userCode", loginUserCode+"");
        data_movieCollectonGETbyMovie.put("movieCode", movie.getMovieCode()+"");
        Call<ArrayList<MovieCollection>> call_movieCollectonGETbyMovie = movieService.getMovieCollectionList("movieCollectonGETbyMovie", data_movieCollectonGETbyMovie);
        call_movieCollectonGETbyMovie.enqueue(new Callback<ArrayList<MovieCollection>>() {
            @Override
            public void onResponse(Call<ArrayList<MovieCollection>> call, Response<ArrayList<MovieCollection>> response) {
                if(response.isSuccessful()){
                    movieCollectionList.clear();
                    movieCollectionList.addAll(response.body());
                    movieCollectionAdapter.notifyDataSetChanged();
                }else{
                    Log.e(TAG, "내 컬렉션 목록 조회");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<MovieCollection>> call, Throwable t) {
                Log.e(TAG, "내 컬렉션 목록 조회 (onFailure)");
            }
        });

    }

    @Override
    public void selectCollection(final MovieCollection movieCollection) {

        // 컬렉션에 이미 영화가 있는데 클릭한 경우, 해당 컬렉션에서 제외시킨다.
        movieCollection.setMovieContainCheck( !movieCollection.isMovieContainCheck() );

        HashMap<String, String> data_movieCollectionItemUPDATE = new HashMap<String, String>();
        data_movieCollectionItemUPDATE.put("collectionCode", movieCollection.getCollectionCode()+"");
        data_movieCollectionItemUPDATE.put("movieCode", movie.getMovieCode()+"");
        data_movieCollectionItemUPDATE.put("flag", (movieCollection.isMovieContainCheck() ? "ADD" : "DELETE"));
        Call<ResponseData> call_movieCollectionItemUPDATE = movieService.post("movieCollectionItemUPDATE", data_movieCollectionItemUPDATE);
        call_movieCollectionItemUPDATE.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    movieCollectionAdapter.notifyDataSetChanged();
                }else{
                    Log.e(TAG, "컬렉션에 아이템 업데이트 실패");
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "컬렉션에 아이템 업데이트 실패 (onFailure)");

            }
        });

    }



}
