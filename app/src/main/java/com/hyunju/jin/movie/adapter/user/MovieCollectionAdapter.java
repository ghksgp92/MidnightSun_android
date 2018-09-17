package com.hyunju.jin.movie.adapter.user;

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
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.community.CollectionItemsActivity;
import com.hyunju.jin.movie.datamodel.MovieCollection;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieCollectionAdapter extends RecyclerView.Adapter<MovieCollectionAdapter.MovieCollectionViewHolder>{

    Context context;
    ArrayList<MovieCollection> list;

    public MovieCollectionAdapter(Context context, ArrayList<MovieCollection> movieCollectionList){
        this.context = context;
        this.list = movieCollectionList;
    }

    @NonNull
    @Override
    public MovieCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_collection, parent, false);
        MovieCollectionViewHolder viewHolder = new MovieCollectionViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCollectionViewHolder holder, int position) {
        holder.position = position;

        MovieCollection movieCollection = list.get(position);
        holder.position = position;

        if(movieCollection.getCollectionItemCount() != 0){
            if( movieCollection.getCollectionItemTop1() != null || movieCollection.getCollectionItemTop1().getPoster() != null) {
                Glide.with(context).load(movieCollection.getCollectionItemTop1().getPoster()).into(holder.img_collection_cover);
            }
        }else{

        }

        holder.tv_collection_item_count.setText(movieCollection.getCollectionItemCount() + " movies");
        holder.tv_collection_name.setText(movieCollection.getCollectionTitle());

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class MovieCollectionViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_collection_cover) ImageView img_collection_cover;
        @BindView(R.id.tv_collection_title) TextView tv_collection_name;
        @BindView(R.id.tv_collection_item_count) TextView tv_collection_item_count;

        int position;

        public MovieCollectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.layout_container)
        public void showCollectionItem(){
            Intent collectionItems = new Intent(context, CollectionItemsActivity.class);
            collectionItems.putExtra(CollectionItemsActivity.DATA_KEY_COLLECTION, list.get(position));
            context.startActivity(collectionItems);
        }
    }
}
