package com.hyunju.jin.movie.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Movie;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieBasicInfoFragment extends Fragment {

    @BindView(R.id.tv_movie_genre) TextView tv_movie_genre;
    @BindView(R.id.tv_production_country) TextView tv_production_country;
    @BindView(R.id.tv_movie_running_time) TextView tv_movie_running_time;
    @BindView(R.id.tv_movie_summary) TextView tv_movie_summary;

    private Movie movie;

    public MovieBasicInfoFragment(){}

    public static MovieBasicInfoFragment getInstance(Movie movie){
        MovieBasicInfoFragment frg = new MovieBasicInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_movie_basic_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tv_movie_genre.setText(" "); // [킵] 장르정보
        tv_movie_running_time.setText("러닝타임 "+movie.getRunningTime()+"분");   // runningTime은 숫자만 들어가므로 반드시 문자화 시켜줘야함.
        if(StringUtils.isNotEmpty(movie.getSummary())) {
            tv_movie_summary.setText(movie.getSummary());
        }else{
            tv_movie_summary.setText(" ");
        }
    }


}
