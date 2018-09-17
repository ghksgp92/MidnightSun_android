package com.hyunju.jin.movie.adapter.user;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.activity.user.MypageListener;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.utils.DateFormatUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WatchingMovieAdapter extends RecyclerView.Adapter<WatchingMovieAdapter.WatchingMovieViewHolder> {

    Context context;
    ArrayList<Movie> list;
    MypageListener listener;
    String TAG = "WatchingMovieAdapter";

    public WatchingMovieAdapter(Context context, ArrayList<Movie> watchingMovieList, MypageListener mypageListener) {
        this.context = context;
        this.list = watchingMovieList;
        this.listener = mypageListener;
    }

    @NonNull
    @Override
    public WatchingMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_watching_movie, parent, false);
        WatchingMovieViewHolder viewHolder = new WatchingMovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WatchingMovieViewHolder holder, int position) {

        holder.position = position;
        Movie movie = list.get(position);

        Glide.with(context).load(movie.getPoster()).into(holder.img_movie_poster);
        holder.tv_movie_title.setText(movie.getMovieTitle());   // 영화 제목
        holder.tv_movie_title_en.setText(movie.getMovieTitle_en()); // 영화 영문제목

        // 마지막 시청 시간
        // 어제, 2일전~일주일전 그외엔 날짜만.
        holder.tv_last_watching_time.setText(movie.getLastWatchingTime());

        holder.tv_play_tme.setText(DateFormatUtils.getHHmmssByLong(movie.getPlayTime())); // 사용자가 본 시간
        holder.tv_duration.setText(DateFormatUtils.getHHmmssByLong(movie.getDuration())); // 영화 전체 시간

        double playTime = ((double) movie.getPlayTime() / movie.getDuration()) * 100;
        holder.progressBar_play_time.setProgress((int) playTime);   // 전체 영화 시간 중 본 시간의 비율을 구해 progressbar 형태로 보여준다.

    }

    @Override
    public int getItemCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public class WatchingMovieViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_movie_poster) RoundedImageView img_movie_poster;
        @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        @BindView(R.id.tv_movie_title_en) TextView tv_movie_title_en;
        @BindView(R.id.tv_last_watching_time) TextView tv_last_watching_time;
        //@BindView(R.id.progressBar_play_time)  ProgressBar progressBar_play_time;
        @BindView(R.id.progressBar_play_time) RoundCornerProgressBar progressBar_play_time;
        @BindView(R.id.tv_play_tme) TextView tv_play_tme;
        @BindView(R.id.tv_duration) TextView tv_duration;

        int position;


        public WatchingMovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            position = 0;
        }

        @OnClick(R.id.img_movie_poster)
        public void showMovieDetail() {
            Intent movie = new Intent(context, MovieDetailActivity.class);
            movie.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, list.get(position));
            context.startActivity(movie);
        }

        @OnClick(R.id.layout_watching_movie_info)
        public void startStreaming() {
            listener.startStreaming(list.get(position));
        }
    }
}
