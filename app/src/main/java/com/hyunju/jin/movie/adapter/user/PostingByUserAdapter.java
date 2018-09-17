package com.hyunju.jin.movie.adapter.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.Posting;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostingByUserAdapter extends RecyclerView.Adapter<PostingByUserAdapter.PostingByUserViewHolder>{

    Context context;
    ArrayList<Posting> list;

    public PostingByUserAdapter(Context context, ArrayList<Posting> postingList){
        this.context = context;
        this.list = postingList;
    }

    @NonNull
    @Override
    public PostingByUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_posting_by_user, parent, false);
        PostingByUserViewHolder viewHolder = new PostingByUserViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostingByUserViewHolder holder, int position) {

        holder.position = position;
        Posting posting = list.get(position);

        holder.tv_posting_title.setText(posting.getPostingTitle());
        //holder.tv_posting_contents_preview.setText(posting.getPostingContents());

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class PostingByUserViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_posting_title) TextView tv_posting_title;
        @BindView(R.id.tv_posting_contents_preview) TextView tv_posting_contents_preview;
        @BindView(R.id.img_posting_cover) RoundedImageView img_posting_cover;

        int position;

        public PostingByUserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }
    }
}
