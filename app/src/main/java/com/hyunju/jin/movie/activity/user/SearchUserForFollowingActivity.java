package com.hyunju.jin.movie.activity.user;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.adapter.user.SearchUserForFollowingAdapter;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 팔로우할 사용자를 추가할 수 있는 화면.
 * ID로 유저를 검색할 수 있다.
 *
 * (+) 기획적으로 볼때 ID 보단 유저 이름을 쓸 수 있게 하고, 이름으로 검색하는게 더 맞다고 본다.
 */
public class SearchUserForFollowingActivity extends SuperActivity  implements  SearchUserForFollowingListener{

    @BindView(R.id.ed_search_text) EditText ed_search_text;    // 검색할 유저 ID를 입력하는 창

    @BindView(R.id.recycler_view_search_users) RecyclerView recycler_view_search_users; // 유저 ID 검색 결과를 보여주는 리사이클러 뷰
    SearchUserForFollowingAdapter searchUserForFollowingAdapter;    // recycler_view_search_users 의 어댑터
    ArrayList<User> searchResultUserList;   // 유저 ID 검색어 결과 리스트

    @BindView(R.id.msg_result_empty) TextView msg_result_empty;  // 검색결과가 없을 시 메시지를 보여주기 위한 텍스트뷰

    UserService userService;    // 유저 검색을 위해 서버와 통신하는 Retrofit 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user_for_following);
        ButterKnife.bind(this);

        initialize();
    }

    /**
     * 현재 액티비티를 사용하기 위해 필요한 변수 초기화, 객체 생성, 화면 준비작업을 한다.
     */
    private void initialize(){
        userService = RetrofitClient.getUserService();  // Retrofit 객체 생성(?)

        searchResultUserList = new ArrayList<User>();   // 검색 결과를 저장하는 리스트 생성
        // 리스트와 리사이클러 뷰 연결
        searchUserForFollowingAdapter = new SearchUserForFollowingAdapter(getContext(), searchResultUserList, this);
        recycler_view_search_users.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_view_search_users.setAdapter(searchUserForFollowingAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 돋보기 모양의 검색버튼 클릭 시, 서버에 검색 요청을 한다.
     * 입력한 검색어가 없을 경우 검색 요청을 하지 않는다.
     * 검색 결과에는 1) 검색어와 ID가 일치하는 경우, 2) 검색어가 유저 ID 에 포함된 경우가 포함된다.
     * 1) 검색한 사용자 자신과 2) 이미 팔로우 중인 사용자는 제외된다.
     */
    @OnClick(R.id.btn_search)
    public void searchUserByUserID(){

        String searchText = ed_search_text.getText().toString();
        if(StringUtils.isEmpty(searchText)){    // 입력한 검색어가 없는 경우, 서버에 검색 요청을 하지 않는다.
            return;
        }

        // 서버에 보낼 데이터 목록 생성
        HashMap<String, String> data_searchUser = new HashMap<>();
        data_searchUser.put("userCode", loginUser.getUserCode()+"");    // 검색 결과에 자기자신이 포함되지 않도록 현재 로그인한 유저 코드를 서버에 보낸다.
        data_searchUser.put("searchText", searchText+"");   // 검색어를 서버에 보낸다.

        // getUsersByUserID.php 페이지가 요청을 처리한다. 페이지 이름을 매개변수로 전달한다.
        // 정확한 URL 은 getUserList 메소드를 확인할 것.
        Call<ArrayList<User>> call_searchUser = userService.getUserList("getUsersByUserID", data_searchUser);
        call_searchUser.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if(response.isSuccessful()){

                    searchResultUserList.clear();   // 기존 검색결과를 지우고
                    searchResultUserList.addAll(response.body());   // 새로운 검색결과로
                    searchUserForFollowingAdapter.notifyDataSetChanged(); // 리사이클러 뷰를 다시 표시한다.

                    if(searchResultUserList.size() == 0){   // 검색결과가 없는 경우
                        recycler_view_search_users.setVisibility(View.GONE);
                        msg_result_empty.setVisibility(View.VISIBLE);   // '검색결과가 없음' 메시지를 화면에 표시한다.
                    }else{ // 검색결과가 있다면
                        recycler_view_search_users.setVisibility(View.VISIBLE); // 결과를 화면에 표시하고
                        msg_result_empty.setVisibility(View.GONE);  // '검색결과 없음' 메시지는 보이지 않게 한다.
                    }
                    return;
                }

                Log.e(TAG, "사용자 ID 검색 실패");

            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                Log.e(TAG, "사용자 ID 검색 실패 (onFailure)");
            }
        });

    }

    /**
     * [<] 버튼을 누를 시 현재 액티비티를 종료한다.
     */
    @OnClick(R.id.img_back)
    public void back(){
        finish();
    }

    /**
     * 리사이클러 뷰에 연결된 리스트에서 인덱스가 position 에 해당하는 유저의 팔로잉 상태를 변경한다.
     * 현재 팔로우되지 않은 상태라면 팔로우 상태가 되고 현재 팔로우 상태라면 팔로우를 해제한다.
     * 팔로우 상태가 변경되면 서버에도 업데이트를 해야한다.
     * @param position
     */
    @Override
    public void flagFollowing(int position) {

        // 팔로잉 상태가 변경된 유저 객체. 여러번 참조해야해서 별도의 객체에 저장함.
        User followingUser = searchResultUserList.get(position);

        // 서버에 팔로잉 상태 변경을 업데이트하기 전에 화면부터 업데이트한다.
        // 유저 검색결과 리스트에서 팔로잉 상태를 바꾸고
        followingUser.setFollowingState( !followingUser.isFollowingState() );
        // 어댑터에 바뀐 상태를 알린다.
        searchUserForFollowingAdapter.notifyItemChanged(position);

        // 서버에 팔로잉 상태 변경을 업데이트 한다.
        // 서버에 보낼 데이터 목록 생성
        HashMap<String, String> data_updateUserFollowing = new HashMap<>();
        data_updateUserFollowing.put("userCode", loginUser.getUserCode()+"");   // 어떤 사용자의 팔로잉 정보인지 구분하기 위해 재 로그인한 사용자 정보를 서버에 보낸다.
        data_updateUserFollowing.put("followingUserCode",  followingUser.getUserCode()+""); // 팔로우 유저 정보를 서버에 보낸다.
        data_updateUserFollowing.put("followingState", followingUser.isFollowingState()+"");    // 팔로우 상태를 서버에 보낸다. 팔로우를 해제했다면 false 값을 보내게 된다.

        Call<ResponseData> call_updateUserFollowing = userService.post("updateUserFollowing", data_updateUserFollowing);
        call_updateUserFollowing.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    // 이미 팔로잉 상태 변경을 화면에 표시했기 때문에 별다른 작업은 하지 않는다.
                    return;
                }

                Log.e(TAG, "팔로잉 상태 변경 실패");
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "팔로잉 상태 변경 실패 (onFailure)");
            }
        });
    }
}
