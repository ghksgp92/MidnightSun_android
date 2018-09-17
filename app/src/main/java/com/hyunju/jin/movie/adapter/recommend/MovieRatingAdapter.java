package com.hyunju.jin.movie.adapter.recommend;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.activity.recommend.MovieRatingInsertListener;
import com.hyunju.jin.movie.datamodel.Movie;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieRatingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // ViewType 정의
    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    private boolean isLoadingAdded = false; // 다음 페이지를 로드중이라면 true

    private Context context;
    private ArrayList<Movie> list;
    MovieRatingInsertListener listener;

    public MovieRatingAdapter(Context context,  MovieRatingInsertListener listener){
        this.context = context;
        this.list = new ArrayList<Movie>();
        this.listener = listener;
    }

    public ArrayList<Movie> getList() {
        return list;
    }

    public void setList(ArrayList<Movie> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){
            case VIEW_TYPE_LOADING:
                View loading = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                LoadingViewHolder loadingVH = new LoadingViewHolder(loading);
                return loadingVH;
            case VIEW_TYPE_ITEM:
                View item = LayoutInflater.from(context).inflate(R.layout.item_movie_rating, parent, false);
                MovieRatingViewHolder itemVH = new MovieRatingViewHolder(item);
                return itemVH;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {

            case VIEW_TYPE_LOADING:
                // 해야할 일 없음
                break;
            case VIEW_TYPE_ITEM:
                MovieRatingViewHolder itemVH = (MovieRatingViewHolder) holder;
                Movie movie = list.get(position);
                itemVH.position = position;
                Glide.with(context).load(movie.getPoster()).into(itemVH.img_movie_poster);
                itemVH.tv_movie_title.setText(movie.getMovieTitle());
                itemVH.tv_movie_title_en.setText(movie.getMovieTitle_en());
                itemVH.rating_bar.setRating(movie.getRatingValue());
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return (position == list.size() - 1 && isLoadingAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    /*
        데이터 리스트와 관련된 메서드
     */

    public void add(Movie mc) {
        list.add(mc);
        notifyItemInserted(list.size() - 1); // 뭐 하는 거지? 마지막에 그냥 빈 영화객체를 넣네.
    }

    public void addAll(List<Movie> mcList) {
        for (Movie mc : mcList) {
            add(mc);
        }
    }

    public void remove(Movie city) {
        int position = list.indexOf(city);
        if (position > -1) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /*
       페이징 처리와 관련된 메서드
    */

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Movie());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = list.size() - 1;
        Movie item = getItem(position);

        if (item != null) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Movie getItem(int position) {
        return list.get(position);
    }

    /**
     * ViewType = VIEW_TYPE_ITEM 인 경우
     */
    public class MovieRatingViewHolder extends RecyclerView.ViewHolder{

        public @BindView(R.id.img_movie_poster) RoundedImageView img_movie_poster;
        public @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        public @BindView(R.id.tv_movie_title_en) TextView tv_movie_title_en;
        public @BindView(R.id.rating_bar) RatingBar rating_bar;  // 0.5 단위로 지정할 수는 없고 보여지기만 함. 나중에 수정해

        int position;
        public MovieRatingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;

            rating_bar.setOnRatingBarChangeListener(ratingBarChangeListener);

        }

        @OnClick(R.id.img_movie_poster)
        public void showMovieDetail(){
            Intent movie = new Intent(context, MovieDetailActivity.class);
            movie.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, list.get(position));
            context.startActivity(movie);
        }

        android.widget.RatingBar.OnRatingBarChangeListener ratingBarChangeListener = new android.widget.RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(android.widget.RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser){
                    list.get(position).setRatingValue(rating);
                    listener.onRatingChanged(list.get(position));
                }
            }
        };
    }

    /**
     * ViewType = VIEW_TYPE_LOADING 인 경우
     */
    public class LoadingViewHolder extends RecyclerView.ViewHolder{

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
