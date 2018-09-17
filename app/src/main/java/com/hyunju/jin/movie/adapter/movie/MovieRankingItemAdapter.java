package com.hyunju.jin.movie.adapter.movie;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.datamodel.Movie;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieRankingItemAdapter extends  RecyclerView.Adapter<MovieRankingItemAdapter.MovieRankingItemViewHolder>{

    Context context;
    ArrayList<Movie> list;

    private static String TAG = "MovieRankingItemAdapter";

    public MovieRankingItemAdapter(Context context, ArrayList<Movie> list){
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public MovieRankingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_ranking, parent, false);
        MovieRankingItemAdapter.MovieRankingItemViewHolder viewHolder = new MovieRankingItemAdapter.MovieRankingItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieRankingItemViewHolder holder, int position) {
        // list size 보다 더 큰 값이 들어올 일은 없는건가?
        if( list == null || list.get(position) == null){
            return;
        }

        Movie movie = list.get(position);

        holder.position = position;
        if(StringUtils.isNotEmpty(movie.getPoster())) {
            Glide.with(context).load(movie.getPoster()).into(holder.img_movie_poster);
        }

        holder.tv_movie_title.setText(movie.getMovieTitle());

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }


    public class MovieRankingItemViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        @BindView(R.id.img_movie_poster) ImageView img_movie_poster;

        public int position;

        public MovieRankingItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.img_movie_poster)
        public void showMovieDetailInfo(){

            Intent intent = new Intent(context,  MovieDetailActivity.class);
            intent.putExtra("movie", list.get(position));
            context.startActivity(intent);
        }

    }
}
