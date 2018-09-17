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
import com.hyunju.jin.movie.activity.user.SearchUserForFollowingListener;
import com.hyunju.jin.movie.datamodel.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * [마이페이지]-[친구목록]-[유저검색] 화면에서 검색 결과를 보여주는 리사이클러뷰와 연결된 어댑터.
 * 유저를 팔로우하거나 팔로우 해제할 수 있다.
 */
public class SearchUserForFollowingAdapter extends RecyclerView.Adapter<SearchUserForFollowingAdapter.SearchUserForFollowingViewHolder>{

    private Context context;
    ArrayList<User> list;
    private SearchUserForFollowingListener listener;


    public SearchUserForFollowingAdapter(Context context, ArrayList<User> list, SearchUserForFollowingListener listener){
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchUserForFollowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follower, parent, false);
        SearchUserForFollowingViewHolder viewHolder = new SearchUserForFollowingViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserForFollowingViewHolder holder, int position) {

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

    }

    @Override
    public int getItemCount() {
        return (list == null ? 0 : list.size());
    }

    /**
     * ViewHolder 정의
     */
    public class SearchUserForFollowingViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_check_box_call_member) ImageView img_check_box_call_member;  // 사용자 체크 박스. 유저 검색 후 팔로우하는 경우에는 사용하지 않는다.
        @BindView(R.id.img_user_profile) ImageView img_user_profile;
        @BindView(R.id.tv_user_id) public TextView tv_user_id;  // 사용자 ID
        @BindView(R.id.tv_following_state) public TextView tv_following_state;  // 팔로우 버튼. 팔로잉 상태라면 팔로잉으로 표시된다.
        public int position;

        public SearchUserForFollowingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        /**
         * 현재 팔로우되지 않은 상태라면 팔로우 상태가 되고 현재 팔로우 상태라면 팔로우를 해제한다.
         */
        @OnClick(R.id.tv_following_state)
        public void flagFollowingState(){
            listener.flagFollowing(position);
        }
    }
}
