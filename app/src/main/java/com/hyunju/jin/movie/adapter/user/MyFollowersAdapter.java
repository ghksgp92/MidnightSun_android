package com.hyunju.jin.movie.adapter.user;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.user.MyFollowersListener;
import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 *
 */
public class MyFollowersAdapter extends RecyclerView.Adapter<MyFollowersAdapter.MyFollowersViewHolder>{

    private Context context;
    ArrayList<User> list;
    private MyFollowersListener listener;

    public MyFollowersAdapter(Context context, ArrayList<User> list, MyFollowersListener listener){
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyFollowersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follower, parent, false);
        MyFollowersViewHolder viewHolder = new MyFollowersViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyFollowersViewHolder holder, int position) {

        holder.position = position;

        // 프로필 사진 가져오기
        Glide.with(context).load(list.get(position).getProfileImg()).apply(new RequestOptions().error(R.drawable.img_user)).into(holder.img_user_profile);

        // 사용자 ID 텍스트 뷰에서 보여주기
        holder.tv_user_id.setText(list.get(position).getId());

        // 팔로우 상태 표시
        if(list.get(position).isFollowingState()){  // 팔로우한 상태라면
            holder.tv_following_state.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.round_filled_03, null));
            holder.tv_following_state.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.backgroundColor, null));
        }else{  // 팔로우하지 않은 상태라면
            holder.tv_following_state.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.round_filled_02, null));
            holder.tv_following_state.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.pointColor, null));
        }

        if(listener.getCurrentSelectMode()){
            // 선택한 사용자인지 확인한 후 체크박스 모양 설정
            if(listener.containsVideoCallUserList(list.get(position).getUserCode())){
                Glide.with(context).load(R.drawable.ic_check_box_on).into(holder.img_check_box_call_member);
            }else{
                Glide.with(context).load(R.drawable.ic_check_box_off).into(holder.img_check_box_call_member);
            }
            holder.img_check_box_call_member.setVisibility(View.VISIBLE);
        }else{
            holder.img_check_box_call_member.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return (list == null ? 0 : list.size());
    }

    public class MyFollowersViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_check_box_call_member) ImageView img_check_box_call_member;  // 사용자 체크 박스. 영상통화를 할 사용자를 선택할때 선택된 사용자를 표시하기 위해 사용한다.
        @BindView(R.id.img_user_profile) ImageView img_user_profile;
        @BindView(R.id.tv_user_id) public TextView tv_user_id;  // 사용자 ID
        @BindView(R.id.tv_following_state) public TextView tv_following_state;  // 팔로우 버튼. 팔로잉 상태라면 팔로잉으로 표시된다.
        public int position;

        public MyFollowersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        @OnClick(R.id.img_check_box_call_member)
        public void checkBoxClick(){
            User user = list.get(position);
            listener.selectUserForVideoCall(user.getUserCode(), user);
        }
    }
}
