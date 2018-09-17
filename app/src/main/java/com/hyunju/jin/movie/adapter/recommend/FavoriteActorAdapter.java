package com.hyunju.jin.movie.adapter.recommend;

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
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MoviesByActorActivity;
import com.hyunju.jin.movie.datamodel.Actor;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FavoriteActorAdapter extends RecyclerView.Adapter<FavoriteActorAdapter.ActorReportViewHolder>{

    Context context;
    ArrayList<Actor> list;

    public FavoriteActorAdapter(Context context, ArrayList<Actor> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ActorReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_taste_report, parent, false);
        ActorReportViewHolder viewHolder = new ActorReportViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ActorReportViewHolder holder, int position) {
        Actor actor = list.get(position);

        holder.position = position;
        Glide.with(context).load(actor.getActorProfile()).into(holder.img_profile);
        holder.tv_name.setText(actor.getActorName());
        holder.tv_watch_movie_count.setText(actor.getCount()+"편 관람");
        holder.tv_desc.setText("이 배우의 모든 작품 보기");

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ActorReportViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_profile) CircularImageView img_profile;
        @BindView(R.id.tv_name) TextView tv_name;
        @BindView(R.id.tv_desc) TextView tv_desc;
        @BindView(R.id.tv_watch_movie_count) TextView tv_watch_movie_count;
        int position;

        public ActorReportViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;

        }

        @OnClick(R.id.tv_desc)
        public void showMoviesByActor(){

            Intent movies = new Intent(context, MoviesByActorActivity.class);
            movies.putExtra(MoviesByActorActivity.KEY_ACTOR_CODE, list.get(position));
            context.startActivity(movies);

        }
    }
}
