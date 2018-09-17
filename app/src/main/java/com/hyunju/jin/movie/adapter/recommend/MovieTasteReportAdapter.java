package com.hyunju.jin.movie.adapter.recommend;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieTasteReportAdapter {

    public class FavoriteActorViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_profile) ImageView img_profile;
        @BindView(R.id.tv_name) TextView tv_name;
        @BindView(R.id.tv_desc) TextView tv_desc;

        public FavoriteActorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }




}
