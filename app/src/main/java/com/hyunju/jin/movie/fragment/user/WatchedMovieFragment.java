package com.hyunju.jin.movie.fragment.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyunju.jin.movie.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 아직 작업 안함.
 */
public class WatchedMovieFragment extends Fragment {


    public WatchedMovieFragment(){}

    public static WatchedMovieFragment getInstance(){
        WatchedMovieFragment frg = new WatchedMovieFragment();
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

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_watched_movie, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
