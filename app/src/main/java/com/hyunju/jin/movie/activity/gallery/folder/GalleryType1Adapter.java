package com.hyunju.jin.movie.activity.gallery.folder;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GalleryType1Adapter extends RecyclerView.Adapter<GalleryType1Adapter.GalleryViewHolder>{

    Context context;
    GalleryType1Listener listener;
    private ArrayList<HashMap< String, String >> list;
    private final String TAG = "GalleryAdapter";

    public GalleryType1Adapter(Context context, GalleryType1Listener galleryListener, ArrayList<HashMap< String, String >> galleryList){
        this.context = context;
        this.listener = galleryListener;
        this.list = galleryList;
    }


    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        GalleryType1Adapter.GalleryViewHolder viewHolder = new GalleryType1Adapter.GalleryViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        if( list == null || list.get(position) == null){
            return;
        }

        HashMap <String, String> song = list.get(position);
        holder.position = position;

        Glide.with(context).load(new File(song.get(GalleryFunction.KEY_PATH))).into(holder.img_media_thumbnail);


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

        public int position;

        public GalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;

        }
        @OnClick(R.id.layout_media)
        public void clickMedia(){
            HashMap<String, String> item = list.get(+position);
            listener.selectMedia(list.get(+position));
        }
    }
}
