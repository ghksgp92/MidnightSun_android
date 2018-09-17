package com.hyunju.jin.movie.adapter.posting;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.posting.PostingDetailActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostingAdapter extends RecyclerView.Adapter<PostingAdapter.PostingViewHolder>{

    Context context;
    ArrayList<Posting> list;

    public PostingAdapter(Context context, ArrayList<Posting> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public PostingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_posting, parent, false);
        PostingViewHolder viewHolder = new PostingViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostingViewHolder holder, int position) {

        Posting posting = list.get(position);
        holder.position = position;
        holder.tv_posting_title.setText(posting.getPostingTitle());

        if(posting.getWriter() != null) {
            User writer = posting.getWriter();
            if (StringUtils.isNotEmpty(posting.getWriter().getProfileImg())) {
                Glide.with(context).load(RetrofitClient.WEB_SERVER + RetrofitClient.WEB_SERVER_PORT + "/images/user/" + writer.getProfileImg()).into(holder.img_posting_writer_profile);
            }
            holder.tv_posting_writer_id.setText(writer.getId());
        }

        String movieTags = "";
        if( posting.getPostingMovieTagList() != null && posting.getPostingMovieTagList().size() > 0){

            for(Movie movie : posting.getPostingMovieTagList()){
                movieTags = movieTags + "" + movie.getMovieTitle().replaceAll(" ", "")+"　";
            }
            holder.tv_posting_movie_tags.setVisibility(View.VISIBLE);
        }else{
            holder.tv_posting_movie_tags.setVisibility(View.GONE);
        }
        holder.tv_posting_movie_tags.setText(movieTags);
        holder.tv_posting_register_date.setText(posting.getWriteDate());
    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class PostingViewHolder extends RecyclerView.ViewHolder{

        // 작성자 정보
        @BindView(R.id.img_posting_writer_profile) CircularImageView img_posting_writer_profile;
        @BindView(R.id.tv_posting_writer_id) TextView tv_posting_writer_id;

        // 포스팅 정보
        @BindView(R.id.tv_posting_title) TextView tv_posting_title;
        @BindView(R.id.tv_posting_movie_tags) TextView tv_posting_movie_tags;
        @BindView(R.id.tv_posting_register_date) TextView tv_posting_register_date;

        public int position;

        public PostingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.layout_posting)
        public void showPosting(){
            /*Intent posting = new Intent(context, PostingDetailActivity.class);
            posting.putExtra(PostingDetailActivity.DATA_KEY_POSTING, list.get(position));
            context.startActivity(posting);*/

            Intent posting = new Intent(context, PostingDetailActivity.class);
            posting.putExtra(PostingDetailActivity.DATA_KEY_POSTING, list.get(position));
            context.startActivity(posting);
        }
    }
}
