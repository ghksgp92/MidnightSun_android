package com.hyunju.jin.movie.activity.posting;

import android.content.Intent;
import android.support.v4.content.res.ResourcesCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.BottomMenuActivity;
import com.hyunju.jin.movie.adapter.posting.PostingAdapter;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.PostingService;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 포스팅 글을 볼 수 있는 화면
 * 포스팅 글 조회 시 검색(글 제목, 내용, 영화제목, 영화 영문제목)  -> 컬럼별로 조회하느니 그냥 이 컬럼들을 다 합쳐놓고 검색하는게 낫.. 진 않네 그러면 span 으로 표시하기 번거아니구나.. 상관없음.
 * 정렬 날짜순, 추천순, 댓글순 등등..
 * 필터링 필요하다. 스포 있/없, 본 글 제외 (이게 필요한가?)
 */
public class PostingMainActivity extends BottomMenuActivity {

    private final int COLLECTION_ADD = 1000;

    @BindView(R.id.img_bottom_menu_community) ImageView img_community;
    @BindView(R.id.tv_bottom_menu_label_community) TextView tv_community;
    // 포스팅 목록을 보여주는 View
    @BindView(R.id.recycler_posting) RecyclerView recycler_posting;

    PostingService postingService;
    Posting posting;

    ArrayList<Posting> postingList;
    PostingAdapter postingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting_main);
        ButterKnife.bind(this);

        initialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPosting();
    }

    private void initialize() {

        // 하단 메뉴에서 현재 화면 버튼 표시 변경
        img_community.setImageResource(R.drawable.ic_menu_community_on);
        tv_community.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null));

        postingService = RetrofitClient.getPostingService();
        postingList = new ArrayList<Posting>();

        postingAdapter = new PostingAdapter(getContext(), postingList);
        recycler_posting.setLayoutManager( new LinearLayoutManager(getContext()) );
        recycler_posting.setAdapter(postingAdapter);

    }

    private void loadPosting(){
        HashMap<String, String> data_postingListOrderBy = new HashMap<String, String>();

        Call<ArrayList<Posting>> call_postingListOrderBy = postingService.getPostingList("postingListOrderBy", data_postingListOrderBy);
        call_postingListOrderBy.enqueue(new Callback<ArrayList<Posting>>() {
            @Override
            public void onResponse(Call<ArrayList<Posting>> call, Response<ArrayList<Posting>> response) {
                if(response.isSuccessful()){
                    postingList.clear();
                    postingList.addAll(response.body());
                    postingAdapter.notifyDataSetChanged();

                }else{
                    Log.e(TAG, "포스팅 목록 조회");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Posting>> call, Throwable t) {
                Log.e(TAG, "포스팅 목록 조회 (onFailure)");
            }
        });

    }

    /**
     *
     */
    @OnClick(R.id.ic_posting_write)
    public void writePosting(){
        Intent write = new Intent(getContext(), PostingEditActivity.class);
        write.putExtra(PostingEditActivity.DATA_KEY_POSTING, new Posting(Posting.DATA_VAL_REQ_TYPE_ADD, loginUser));
        startActivity(write);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case COLLECTION_ADD:    // 새로운 컬렉션 추가 요청
                // 서버에서 추가된 컬렉션 정보만 불러온다.

                Log.d(TAG, "COLLECTION_ADD");
                break;
        }
    }
}