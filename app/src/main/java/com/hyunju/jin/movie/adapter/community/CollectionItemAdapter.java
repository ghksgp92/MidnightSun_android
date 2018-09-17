package com.hyunju.jin.movie.adapter.community;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.datamodel.Movie;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CollectionItemAdapter extends RecyclerView.Adapter<CollectionItemAdapter.CollectionItemViewHolder>{

    Context context;
    ArrayList<Movie> list;

    public CollectionItemAdapter(Context context, ArrayList<Movie> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CollectionItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_collection_item, parent, false);
        CollectionItemViewHolder viewHolder = new CollectionItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionItemViewHolder holder, int position) {

        Movie movie = list.get(position);
        holder.position = position;
        Glide.with(context).load(movie.getPoster()).into(holder.img_movie_poster);
        holder.tv_movie_title.setText(movie.getMovieTitle());

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class CollectionItemViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_movie_poster) RoundedImageView img_movie_poster;
        @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        int position;

        public CollectionItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.layout_container)
        public void showMovie(){
            Intent movie = new Intent(context, MovieDetailActivity.class);
            movie.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, list.get(position));
            context.startActivity(movie);
        }
    }
}
