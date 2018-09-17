package com.hyunju.jin.movie.adapter.movie;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.hyunju.jin.movie.datamodel.Movie;

import java.util.ArrayList;

public class MoviesByActorAdapter extends RecyclerView.Adapter<MoviesByActorAdapter.MoviesByActorViewHolder>{


    Context context;
    ArrayList<Movie> list;

    public MoviesByActorAdapter(Context context, ArrayList<Movie> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MoviesByActorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesByActorViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MoviesByActorViewHolder extends RecyclerView.ViewHolder{

        public MoviesByActorViewHolder(View itemView) {
            super(itemView);
        }
    }
}
