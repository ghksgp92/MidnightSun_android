package com.hyunju.jin.movie.adapter.videocall;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 그룹 영상통화 화면에서 전화를 받기 전에 영상통화 참가자들을 보여주기 위해 사용한다.
 */
public class GroupVideoCallUsersAdapter extends RecyclerView.Adapter<GroupVideoCallUsersAdapter.GroupVideoCallUsersViewHolder>{

    Context context;
    ArrayList<User> list;

    public GroupVideoCallUsersAdapter(Context context, ArrayList<User> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public GroupVideoCallUsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_video_call_user, parent, false);
        GroupVideoCallUsersViewHolder viewHolder = new GroupVideoCallUsersViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVideoCallUsersViewHolder holder, int position) {

        User user = list.get(position);
        holder.position = position;

        Glide.with(context).load(user.getProfileImg()).apply(new RequestOptions().error(R.drawable.user_avatar)).into(holder.img_user_profile);
        holder.tv_user_id.setText(user.getId()+"");

    }

    @Override
    public int getItemCount() {
        return (list == null ? 0 : list.size());
    }

    public class GroupVideoCallUsersViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_user_profile) ImageView img_user_profile;    // 사용자 프로필사진
        @BindView(R.id.tv_user_id) TextView tv_user_id; // 사용자 아이디

        int position;
        public GroupVideoCallUsersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }
    }
}
