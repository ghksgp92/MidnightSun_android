package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import butterknife.OnClick;

public class GalleryType2Adapter extends RecyclerView.Adapter<GalleryType2Adapter.GalleryViewHolder>{

    Context context;
    GalleryType2Listener listener;
    private ArrayList<Gallery> list;
    private final String TAG = "GalleryAdapter";

    public GalleryType2Adapter(Context context, GalleryType2Listener galleryListener, ArrayList<Gallery> galleryList){
        this.context = context;
        this.listener = galleryListener;
        this.list = galleryList;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        GalleryType2Adapter.GalleryViewHolder viewHolder = new GalleryType2Adapter.GalleryViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        holder.position = position;

        HashMap <String, String> media = list.get(position).getMedia();

        Glide.with(context).load(new File(media.get(GalleryFunction.KEY_PATH))).into(holder.img_media_thumbnail);

        // 사용자앨범과 선택결과가 저장된 값을 비교해야함
        int selected_order = listener.checkSelectedList(list.get(position));

        if(selected_order > 0){
            holder.tv_selected_order.setText(selected_order+"");
            holder.tv_selected_order.setBackgroundResource(R.drawable.circle_filled_01);
            holder.layout_selected_order.setBackgroundResource(R.color.colorAlpha03);
        }else{
            holder.tv_selected_order.setText(" ");
            holder.tv_selected_order.setBackgroundResource(R.drawable.circle_01);
            holder.layout_selected_order.setBackgroundResource(R.color.transparent);
        }

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


    public class GalleryViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_media_thumbnail) ImageView img_media_thumbnail;
        @BindView(R.id.layout_selected_order) RelativeLayout layout_selected_order;
        @BindView(R.id.tv_selected_order) TextView tv_selected_order;
        @BindView(R.id.tv_video_time) TextView tv_video_time;

        public int position;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;

        }

        /**
         *
         */
        @OnClick(R.id.tv_selected_order)
        public void clickMedia(){
            listener.selectMedia(list.get(+position));
        }

        @OnClick(R.id.layout_media)
        public void showMedia(){

        }
    }
}