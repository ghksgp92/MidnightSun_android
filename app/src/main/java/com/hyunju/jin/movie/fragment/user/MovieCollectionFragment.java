package com.hyunju.jin.movie.fragment.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.community.MyCollectionInfoEditActivity;
import com.hyunju.jin.movie.adapter.user.MovieCollectionAdapter;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import android.support.v7.widget.LinearLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieCollectionFragment extends Fragment {

    private final String TAG = "MovieCollectionFragment";

    @BindView(R.id.layout_movie_collection_empty) LinearLayout layout_movie_collection_empty;

    @BindView(R.id.recycler_movie_collection_list) RecyclerView recycler_movie_collection_list;
    ArrayList<MovieCollection> movieCollectionList;
    MovieCollectionAdapter movieCollectionAdapter;

    private MovieService movieService;


    public MovieCollectionFragment(){}

    public static MovieCollectionFragment getInstance(){
        MovieCollectionFragment frg = new MovieCollectionFragment();
        Bundle bundle = new Bundle();
        //bundle.putSerializable("movie", movie);
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

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_movie_collection, container, false);
        ButterKnife.bind(this, view);

        movieCollectionList = new ArrayList<MovieCollection>();
        movieCollectionAdapter = new MovieCollectionAdapter(getContext(), movieCollectionList);
        recycler_movie_collection_list.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_movie_collection_list.setAdapter(movieCollectionAdapter);

        movieService = RetrofitClient.getMovieService();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        HashMap<String, String> data_movieCollectonGETbyUser = new HashMap<String, String>();
        SharedPreferences sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(getActivity());
        int loginUserCode = sharedPreferences.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);
        data_movieCollectonGETbyUser.put("userCode", loginUserCode+"");
        Call<ArrayList<MovieCollection>> call_movieCollectonGETbyUser = movieService.getMovieCollectionList("movieCollectionGETbyUser", data_movieCollectonGETbyUser);
        call_movieCollectonGETbyUser.enqueue(new Callback<ArrayList<MovieCollection>>() {
            @Override
            public void onResponse(Call<ArrayList<MovieCollection>> call, Response<ArrayList<MovieCollection>> response) {
                if(response.isSuccessful()){
                    movieCollectionList.clear();
                    movieCollectionList.addAll(response.body());
                    movieCollectionAdapter.notifyDataSetChanged();
                    if(movieCollectionList.size() > 0){
                        layout_movie_collection_empty.setVisibility(View.GONE);
                        recycler_movie_collection_list.setVisibility(View.VISIBLE);
                    }else{
                        layout_movie_collection_empty.setVisibility(View.VISIBLE);
                        recycler_movie_collection_list.setVisibility(View.GONE);
                    }
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



    @OnClick({R.id.img_new_collection, R.id.tv_new_collection})
    public void addCollection(){
        Intent newCollection = new Intent(getActivity(), MyCollectionInfoEditActivity.class);
        newCollection.putExtra(MyCollectionInfoEditActivity.KEY_FLAG, MyCollectionInfoEditActivity.FLAG_ADD);
        startActivity(newCollection);
    }
}
