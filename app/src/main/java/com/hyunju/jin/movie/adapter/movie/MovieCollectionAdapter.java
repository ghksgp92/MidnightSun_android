package com.hyunju.jin.movie.adapter.movie;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.fragment.MyCollectionListListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieCollectionAdapter extends RecyclerView.Adapter<MovieCollectionAdapter.MovieCollectionViewHolder>{

    Context context;
    MyCollectionListListener listener;
    ArrayList<MovieCollection> list;

    public MovieCollectionAdapter(Context context, MyCollectionListListener listener, ArrayList<MovieCollection> movieCollectionList){
        this.context = context;
        this.listener = listener;
        this.list = movieCollectionList;
    }

    @NonNull
    @Override
    public MovieCollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_collection_simple, parent, false);
        MovieCollectionViewHolder viewHolder = new MovieCollectionViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCollectionViewHolder holder, int position) {
        if( list == null || list.get(position) == null){
            return;
        }

        MovieCollection collection = list.get(position);
        holder.tv_collection_name.setText(collection.getCollectionTitle());
        holder.position = position;

        if(collection.isMovieContainCheck()){
            holder.img_contain_check.setImageResource(R.drawable.ic_check_on);
        }else{
            holder.img_contain_check.setImageResource(R.drawable.ic_check_off);
            holder.img_contain_check.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_off));
        }

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class MovieCollectionViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_collection_title) TextView tv_collection_name;
        @BindView(R.id.img_contain_check) ImageView img_contain_check;

        public int position;

        public MovieCollectionViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.layout_collection_item)
        public void selectCollection(){

            //list.get(position).setMovieContainCheck( !list.get(position).isMovieContainCheck() );
            listener.selectCollection(list.get(position));
        }
    }
}
