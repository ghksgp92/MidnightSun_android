package com.hyunju.jin.movie.activity.gallery.folder;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 사진/동영상 폴더를 나타내는 GridView 와 연결되는 Adapter 정의
 */
public class GalleryType1FolderAdapter extends RecyclerView.Adapter<GalleryType1FolderAdapter.GalleryFolderViewHolder>{

    Context context;
    GalleryType1Listener listener;
    private ArrayList<HashMap< String, String >> list;

    public GalleryType1FolderAdapter(Context context, GalleryType1Listener galleryListener, ArrayList<HashMap< String, String >> galleryFolderList){
        this.context = context;
        this.listener = galleryListener;
        this.list = galleryFolderList;
    }

    @NonNull
    @Override
    public GalleryFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery_folder, parent, false);
        GalleryFolderViewHolder viewHolder = new GalleryFolderViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryFolderViewHolder holder, int position) {

        if( list == null || list.get(position) == null){
            return;
        }

        HashMap <String, String> song = list.get(position);
        holder.gallery_title.setText(song.get(GalleryFunction.KEY_ALBUM));
        holder.gallery_count.setText(song.get(GalleryFunction.KEY_COUNT));
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


    public class GalleryFolderViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_media_thumbnail) ImageView img_media_thumbnail;
        @BindView(R.id.gallery_count) TextView gallery_count;
        @BindView(R.id.gallery_title) TextView gallery_title;

        public int position;

        public GalleryFolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.card_view_album_folder)
        public void selectAlbumFolder(){

            String album_name = list.get(+position).get(GalleryFunction.KEY_ALBUM);
            listener.loadAlbum(album_name);
        }

    }

}
