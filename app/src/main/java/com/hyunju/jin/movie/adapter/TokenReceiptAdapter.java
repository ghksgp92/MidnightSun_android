package com.hyunju.jin.movie.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.TokenReceipt;
import com.hyunju.jin.movie.datamodel.User;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TokenReceiptAdapter extends RecyclerView.Adapter<TokenReceiptAdapter.TokenReceiptViewHolder> {

    Context context;
    ArrayList<TokenReceipt> list;
    User user;

    public TokenReceiptAdapter(Context context, ArrayList<TokenReceipt> list, User user){
        this.context = context;
        this.list = list;
        this.user = user;   // 현재 로그인한 사용자 정보가 필요함
    }

    @NonNull
    @Override
    public TokenReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_token_receipt, parent, false);
        TokenReceiptViewHolder viewHolder = new TokenReceiptViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TokenReceiptViewHolder holder, int position) {

        TokenReceipt tokenReceipt = list.get(position);

        if( "R".equals(tokenReceipt.getState()) ){
            holder.loading_indicator.smoothToShow();
            holder.img_task_type.setVisibility(View.GONE);
        }else{
            holder.loading_indicator.setVisibility(View.GONE);
            holder.img_task_type.setVisibility(View.VISIBLE);
        }

        String taskType = tokenReceipt.getTaskType();
        if("1".equals(taskType)){ // 포스팅 추천인 경우
            if(tokenReceipt.getReceiver() == user.getUserCode()){ // 추천 받은 사람일 때
                holder.tv_task_type.setText("포스팅을 추천 받음");
                holder.tv_token_value.setText("+ "+tokenReceipt.getVal());
            }
            // 보낸 사람일때는 기록하지 않음.

        }else if("2".equals(taskType)){   // 토큰 전송인 경우
            if(tokenReceipt.getSender() == user.getUserCode()){ // 보낸 사람일 때
                holder.tv_task_type.setText("토큰을 보냄");
                holder.tv_token_value.setText("- "+tokenReceipt.getVal());
            }else{
                holder.tv_task_type.setText("토큰을 받음");
                holder.tv_token_value.setText("+ "+tokenReceipt.getVal());
            }
        }

        holder.tv_write_date.setText(tokenReceipt.getWriteDate());

    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        return list.size();
    }

    public class TokenReceiptViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_task_type) ImageView img_task_type;
        @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator;
        @BindView(R.id.tv_task_type) TextView tv_task_type; // 토큰을 어떻게 이용했는지에 대해 나타냄
        @BindView(R.id.tv_write_date) TextView tv_write_date;   // 토큰 사용 이력이 남겨진 시각
        @BindView(R.id.tv_token_value) TextView tv_token_value; // 토큰 수

        public int position;

        public TokenReceiptViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }
    }
}
