package com.hyunju.jin.movie.adapter.movie;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.activity.movie.MovieSearchListener;
import com.hyunju.jin.movie.datamodel.Movie;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 영화 검색결과 목록에서 하나의 영화를 나타내는 어댑터?
 * 말이 뭐이렇게 이상해
 */

public class MovieSearchResultItemAdapter extends RecyclerView.Adapter<MovieSearchResultItemAdapter.MovieSearchResultItemViewHolder>{

    Context context;
    ArrayList<Movie> list;
    MovieSearchListener listener;

    private static String TAG = "MovieSearchResultItemAdapter";

    public MovieSearchResultItemAdapter(Context context, ArrayList<Movie> list, MovieSearchListener movieSearchListener){
        this.context = context;
        this.list = list;
        this.listener = movieSearchListener;
    }


    @Override
    public MovieSearchResultItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_search_result, parent, false);
        MovieSearchResultItemViewHolder viewHolder = new MovieSearchResultItemViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MovieSearchResultItemViewHolder holder, int position) {

        Movie movie = list.get(position);
        if(StringUtils.isNotEmpty(movie.getPoster())) {
            Glide.with(context).load(movie.getPoster()).into(holder.img_movie_poster);
        }

        holder.tv_movie_title.setText(movie.getMovieTitle());
        holder.position = position;

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class MovieSearchResultItemViewHolder extends RecyclerView.ViewHolder{

        public @BindView(R.id.layout_item_container) RelativeLayout layout_item_container;
        public @BindView(R.id.img_movie_poster) RoundedImageView img_movie_poster;
        public @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        public int position;

        public MovieSearchResultItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.layout_item_container)
        public void clickMovie(){

            listener.selectMovie(list.get(position));

        }

    }
}
