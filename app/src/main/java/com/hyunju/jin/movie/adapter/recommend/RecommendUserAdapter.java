package com.hyunju.jin.movie.adapter.recommend;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecommendUserAdapter extends RecyclerView.Adapter<RecommendUserAdapter.RecommendUserViewHolder>{

    Context context;
    ArrayList<User> list;

    public RecommendUserAdapter(Context context, ArrayList<User> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecommendUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_taste_report, parent, false);
        RecommendUserViewHolder viewHolder = new RecommendUserViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendUserViewHolder holder, int position) {
        User mate = list.get(position);
        // Glide 사용법이 바뀜. [RequestOptions] http://bumptech.github.io/glide/doc/migrating.html#requestoptions
        Glide.with(context).load(mate.getProfileImg()).apply(new RequestOptions().error(R.drawable.img_user)).into(holder.img_profile);
        holder.tv_name.setText(mate.getId());
        holder.tv_desc.setText( (mate.getMateScore()*100) +"% 취향 일치" );
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class RecommendUserViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_profile) CircularImageView img_profile;
        @BindView(R.id.tv_name) TextView tv_name;
        @BindView(R.id.tv_desc) TextView tv_desc;

        public RecommendUserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
