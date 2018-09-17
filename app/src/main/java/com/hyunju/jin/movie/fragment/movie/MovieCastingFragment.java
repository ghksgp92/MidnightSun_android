package com.hyunju.jin.movie.fragment.movie;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.adapter.movie.MovieCastingItemAdapter;
import com.hyunju.jin.movie.datamodel.Casting;
import com.hyunju.jin.movie.datamodel.Movie;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieCastingFragment extends Fragment {

    @BindView(R.id.recycler_casting) RecyclerView recycler_casting;
    ArrayList<Casting> castingList;
    MovieCastingItemAdapter movieCastingItemAdapter;


    private Movie movie;    // 캐스팅(감독, 배우) 정보를 담은 객체

    public static MovieCastingFragment getInstance(Movie movie){
        MovieCastingFragment frg = new MovieCastingFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("movie", movie);
        frg.setArguments(bundle);
        return frg;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movie = (Movie) getArguments().getSerializable("movie");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_casting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        castingList = movie.getCastingList();
        movieCastingItemAdapter = new MovieCastingItemAdapter(getContext(), castingList);
        recycler_casting.setAdapter(movieCastingItemAdapter);

        recycler_casting.setLayoutManager(new LinearLayoutManager(getContext()));
        //recycler_movie_search_result_list.setHasFixedSize(true);


    }
}
