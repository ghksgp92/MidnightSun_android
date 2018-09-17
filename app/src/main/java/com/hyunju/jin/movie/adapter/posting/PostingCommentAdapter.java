package com.hyunju.jin.movie.adapter.posting;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.posting.PostingCommentListener;
import com.hyunju.jin.movie.datamodel.PostingComment;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.utils.DateFormatUtils;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class PostingCommentAdapter extends RecyclerView.Adapter<PostingCommentAdapter.PostingCommentViewHolder>{

    Context context;
    ArrayList<PostingComment> list;
    PostingCommentListener listener;
    User user;

    public PostingCommentAdapter(Context context, ArrayList<PostingComment> list, PostingCommentListener listener){
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.user = new User();
        SharedPreferences sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(context);
        int loginUserCode = sharedPreferences.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);
        user.setUserCode(loginUserCode);
    }

    @NonNull
    @Override
    public PostingCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_posting_comment, parent, false);
        PostingCommentViewHolder viewHolder = new PostingCommentViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostingCommentViewHolder holder, int position) {

        PostingComment postingComment = list.get(position);
        holder.position = position;

        holder.tv_posting_comment_writer_id.setText(postingComment.getWriter().getId());
        holder.tv_posting_comment_write_date.setText( DateFormatUtils.getyyyyMMdd(postingComment.getRegisterDate()) );
        holder.tv_posting_comment.setText(postingComment.getPostingCommentContent());

        if( user.getUserCode() == postingComment.getWriter().getUserCode() ){
            holder.divider_check_mine.setVisibility(View.VISIBLE);
            holder.tv_check_mine.setVisibility(View.VISIBLE);
        }else{
            holder.divider_check_mine.setVisibility(View.GONE);
            holder.tv_check_mine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class PostingCommentViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tv_posting_comment_writer_id) TextView tv_posting_comment_writer_id;
        @BindView(R.id.tv_posting_comment_write_date) TextView tv_posting_comment_write_date;
        @BindView(R.id.tv_posting_comment) TextView tv_posting_comment;

        @BindView(R.id.divider_check_mine) TextView divider_check_mine;     // 작성날짜와 내댓글 표시의 구분문자 TextView 참조
        @BindView(R.id.tv_check_mine) TextView tv_check_mine;               // 내댓글 TextView 참조

        int position = 0;

        public PostingCommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        /**
         * 클릭한 댓글의 작성자를 태그한다.
         */
        @OnClick(R.id.layout_posting_comment)
        public void addUserTag(){

            // 클릭한 댓글의 작성자 정보를 액티비티에 보낸다
            //listener.addUserTag(list.get(position).getWriter());
        }
    }
}
