package com.hyunju.jin.movie.fragment.user;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.adapter.user.PostingByUserAdapter;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.network.PostingService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * 고민거리: 내가 좋아요 한 포스팅 목록을 보여줄 자리가 없네.
 */
public class PostingByUserFragment extends Fragment {

    final String TAG = "PostingByUserFragment";

    @BindView(R.id.layout_posting_empty) LinearLayout layout_posting_empty;

    // 내가 쓴 포스팅을 보여주는 View과 관련 객체
    @BindView(R.id.recycler_posting_list) RecyclerView recycler_posting_list;
    ArrayList<Posting> postingList;
    PostingByUserAdapter postingByUserAdapter;

    PostingService postingService;

    public PostingByUserFragment(){}

    public static PostingByUserFragment getInstance(){
        PostingByUserFragment frg = new PostingByUserFragment();
        Bundle bundle = new Bundle();
        //bundle.putSerializable("movie", movie);
        frg.setArguments(bundle);
        return frg;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 액티비티로부터 데이터를 전달받을 경우 아래와 같이 사용할 수 있음.
        //movie = (movie) getArguments().getSerializable("movie");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_posting_by_user, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        postingService = RetrofitClient.getPostingService();
        postingList = new ArrayList<Posting>();
        postingByUserAdapter = new PostingByUserAdapter(getContext(), postingList);
        recycler_posting_list.setAdapter(postingByUserAdapter);
        recycler_posting_list.setLayoutManager(new LinearLayoutManager(getContext()));

        recycler_posting_list.setVisibility(View.GONE);
        layout_posting_empty.setVisibility(View.VISIBLE);
        //initiallize();

    }

    private void initiallize(){

        HashMap<String, String> data_postingListOrderBy = new HashMap<String, String>();
        SharedPreferences sharedPreferences = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferences.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);
        data_postingListOrderBy.put("userCode", loginUserCode+"");
        Call<ArrayList<Posting>> call_postingListOrderBy = postingService.getPostingList("postingListOrderBy", data_postingListOrderBy);
        call_postingListOrderBy.enqueue(new Callback<ArrayList<Posting>>() {
            @Override
            public void onResponse(Call<ArrayList<Posting>> call, Response<ArrayList<Posting>> response) {
                if(response.isSuccessful()){
                    postingList.addAll(response.body());
                    postingByUserAdapter.notifyDataSetChanged();

                    return;
                }
                Log.e(TAG, "마이페이지 포스팅 목록 조회");

            }

            @Override
            public void onFailure(Call<ArrayList<Posting>> call, Throwable t) {
                Log.e(TAG, "마이페이지 포스팅 목록 조회 (onFailure)");

            }
        });


    }

}

