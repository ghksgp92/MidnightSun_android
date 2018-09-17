package com.hyunju.jin.movie.adapter.recommend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Director;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoriteDirectorAdapter extends RecyclerView.Adapter<FavoriteDirectorAdapter.FavoriteDirectorViewHolder>{

    Context context;
    ArrayList<Director> list;

    public FavoriteDirectorAdapter(Context context, ArrayList<Director> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FavoriteDirectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_taste_report, parent, false);
        FavoriteDirectorViewHolder viewHolder = new FavoriteDirectorViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteDirectorViewHolder holder, int position) {

        Director director = list.get(position);
        Glide.with(context).load(director.getDirectorProfile()).apply(new RequestOptions().error(R.drawable.img_user)).into(holder.img_profile);
        holder.tv_name.setText(director.getDirectorName());
        holder.tv_watch_movie_count.setText(director.getCount()+"편 관람");
        holder.tv_desc.setText("이 감독의 모든 작품 보기");


    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class FavoriteDirectorViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_profile) CircularImageView img_profile;
        @BindView(R.id.tv_name) TextView tv_name;
        @BindView(R.id.tv_desc) TextView tv_desc;
        @BindView(R.id.tv_watch_movie_count) TextView tv_watch_movie_count;

        public FavoriteDirectorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
