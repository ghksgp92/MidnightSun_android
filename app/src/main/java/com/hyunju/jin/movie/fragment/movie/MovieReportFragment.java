package com.hyunju.jin.movie.fragment.movie;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Movie;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieReportFragment extends Fragment {

    private Movie movie;

    @BindView(R.id.pie_chart_gender_report) PieChart pie_chart_gender_report; // 남녀 평점비중을 보여주는 파이차트
    @BindView(R.id.bar_chart_age_report) BarChart bar_chart_age_report;

    public static MovieReportFragment getInstance(Movie movie){
        MovieReportFragment frg = new MovieReportFragment();
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
        View view = inflater.inflate(R.layout.fragment_movie_report, container, false);
        ButterKnife.bind(this, view);

        initialize();
        return view;
    }

    private void initialize(){

        pie_chart_gender_report.addPieSlice(new PieModel("남자", 38, Color.parseColor("#FE6DA8")));
        pie_chart_gender_report.addPieSlice(new PieModel("여자", 62, Color.parseColor("#56B7F1")));
        /*pie_chart_gender_report.addPieSlice(new PieModel("Freetime", 15, Color.parseColor("#FE6DA8")));
        pie_chart_gender_report.addPieSlice(new PieModel("Sleep", 25, Color.parseColor("#56B7F1")));
        pie_chart_gender_report.addPieSlice(new PieModel("Work", 35, Color.parseColor("#CDA67F")));
        pie_chart_gender_report.addPieSlice(new PieModel("Eating", 9, Color.parseColor("#FED70E")));*/
        // pie_chart_gender_report.setUsePieRotation(false);
        pie_chart_gender_report.startAnimation();

        bar_chart_age_report.addBar(new BarModel("10대",2.3f, 0xFF123456));
        bar_chart_age_report.addBar(new BarModel("20대", 2.f,  0xFF343456));
        bar_chart_age_report.addBar(new BarModel("30대", 3.3f, 0xFF563456));
        bar_chart_age_report.addBar(new BarModel("40대 이상", 1.1f, 0xFF873F56));
        bar_chart_age_report.startAnimation();

    }


}
