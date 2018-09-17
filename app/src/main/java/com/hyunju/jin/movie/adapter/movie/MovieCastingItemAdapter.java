package com.hyunju.jin.movie.adapter.movie;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Casting;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieCastingItemAdapter extends RecyclerView.Adapter<MovieCastingItemAdapter.MovieCastingItemViewHolder>{

    Context context;
    ArrayList<Casting> list;

    private static String TAG = "MovieCastingItemAdapter";

    public MovieCastingItemAdapter(Context context, ArrayList<Casting> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MovieCastingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_casting, parent, false);
        MovieCastingItemAdapter.MovieCastingItemViewHolder viewHolder = new MovieCastingItemAdapter.MovieCastingItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCastingItemViewHolder holder, int position) {

        Casting casting = list.get(position);
        Glide.with(context).load(casting.getActor().getActorProfile()).into(holder.img_actor_profile);
        String actorName = casting.getActor().getActorName();
        if(StringUtils.isEmpty(actorName)){
            actorName = casting.getActor().getActorName_en();
        }
        holder.tv_actor_name.setText(actorName);
        holder.tv_casting_type.setText(casting.getCastingType());
        holder.tv_casting_name.setText(casting.getCastingName());

        holder.position = position;
    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class MovieCastingItemViewHolder extends RecyclerView.ViewHolder{

        public int position;

        @BindView(R.id.img_actor_profile) CircularImageView img_actor_profile;
        @BindView(R.id.tv_actor_name) TextView tv_actor_name;
        @BindView(R.id.tv_casting_type) TextView tv_casting_type;
        @BindView(R.id.tv_casting_name) TextView tv_casting_name;

        public MovieCastingItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }
    }

}
