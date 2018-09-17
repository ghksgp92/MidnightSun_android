package com.hyunju.jin.movie.adapter.posting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.datamodel.Gallery;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentEditMediaAdapter extends RecyclerView.Adapter<CommentEditMediaAdapter.CommentEditMediaViewHolder>{

    Context context;
    ArrayList<Gallery> list;

    public CommentEditMediaAdapter(Context context, ArrayList<Gallery> selectList){
        this.context = context;
        this.list = selectList;
    }

    @NonNull
    @Override
    public CommentEditMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_edit_photo, parent, false);
        CommentEditMediaViewHolder viewHolder = new CommentEditMediaViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentEditMediaViewHolder holder, int position) {
        if( list == null || list.get(position) == null){
            return;
        }

        HashMap <String, String> media = list.get(position).getMedia();
        holder.position = position;

        Glide.with(context).load(new File(media.get(GalleryFunction.KEY_PATH))).into(holder.img_media_thumbnail);
        // 비디오 타입이면 시간도 표시한다.

        holder.tv_video_time.setVisibility(View.GONE);
        if( media.get(GalleryFunction.KEY_MIME_TYPE).contains("video") ){

            long duration_milliseconds = Long.parseLong(media.get(GalleryFunction.KEY_DURATION));

            String duration = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration_milliseconds),
                    TimeUnit.MILLISECONDS.toSeconds(duration_milliseconds) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration_milliseconds)));

            holder.tv_video_time.setText(duration);
            holder.tv_video_time.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class CommentEditMediaViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_media_thumbnail) ImageView img_media_thumbnail;
        @BindView(R.id.tv_video_time) TextView tv_video_time;

        int position;

        public CommentEditMediaViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }
    }
}
