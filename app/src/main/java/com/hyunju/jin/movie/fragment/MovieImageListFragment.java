package com.hyunju.jin.movie.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Movie;

import butterknife.ButterKnife;

public class MovieImageListFragment extends Fragment {

    private Movie movie;

    public static MovieImageListFragment getInstance(Movie movie){
        MovieImageListFragment frg = new MovieImageListFragment();
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
        View view = inflater.inflate(R.layout.fragment_movie_image_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }
}
