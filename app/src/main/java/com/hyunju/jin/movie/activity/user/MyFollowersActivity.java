package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.videocall.VideoCallActivity;
import com.hyunju.jin.movie.adapter.user.MyFollowersAdapter;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 내가 팔로우한 유저를 보여주는 화면.
 * [전화모양 버튼]을 눌러 최대 6명까지 선택한 후 영상통화를 할 수 있다.
 * [+] 버튼을 눌러 팔로우할 유저를 추가할 수 있다.
 */
public class MyFollowersActivity extends SuperActivity implements MyFollowersListener{

    @BindView(R.id.recycler_view_followers) RecyclerView recycler_view_followers;   // 현재 팔로우중인 사용자를 보여주는 리사이클러 뷰
    ArrayList<User> myFollowUsers;  // 현재 팔로우중인 사용자 목록을 저장하는 리스트
    MyFollowersAdapter myFollowersAdapter;

    @BindView(R.id.btn_request_call) Button btn_request_call;   // 통화 걸기 버튼. selectModeFroMultiVideoCall 가 true 일 때만 보인다.

    HashMap<Integer, User> selectedUsersForVideoCall;   // 영상통화를 하기위해 선택한 사용자 목록. 최대 6명까지 영상통화가 가능하다.

    UserService userService;    // 팔로우 유저 조회를 위해 서버와 통신하는 Retrofit 객체
    private boolean selectModeFroMultiVideoCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_followers);
        ButterKnife.bind(this);
        initialize();
    }

    /**
     * 현재 액티비티를 사용하기 위해 필요한 변수 초기화, 객체 생성, 화면 준비작업을 한다.
     */
    private void initialize(){
        selectModeFroMultiVideoCall = false;
        btn_request_call.setVisibility(View.GONE);
        userService = RetrofitClient.getUserService();

        // 팔로우 유저를 저장하는 ArrayList 생성
        myFollowUsers = new ArrayList<User>();
        // 리사이클러뷰와 ArrayList 연결
        myFollowersAdapter = new MyFollowersAdapter(getContext(), myFollowUsers, this);
        recycler_view_followers.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler_view_followers.setAdapter(myFollowersAdapter);

        // 영상통화 사용자 목록 초기화
        selectedUsersForVideoCall = new HashMap<>();
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadMyFollowUsers();    // 팔로우 목록 로드
    }


    /**
     * 서버에 자신이 팔로우 중인 유저목록을 요청한다.
     */
    private void loadMyFollowUsers(){

        // 프로그래스바 보이기. 아직 코딩안했는데 하게되면 프로그래스바 감추는 작업도 해야함.

        // 서버에 보낼 데이터 준비
        HashMap<String, String> data_loadMyFollowUsers = new HashMap<String, String>();
        data_loadMyFollowUsers.put("userCode", loginUser.getUserCode()+""); // 자신의 팔로우 유저 목록이 필요하므로, 자신의 유저 코드를 서버에 보낸다.

        Call<ArrayList<User>> call_loadMyFollowUsers = userService.getUserList("loadMyFollowUsers", data_loadMyFollowUsers);
        call_loadMyFollowUsers.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if(response.isSuccessful()){

                    myFollowUsers.clear();
                    myFollowUsers.addAll(response.body());
                    myFollowersAdapter.notifyDataSetChanged();

                    if(myFollowUsers.size() == 0){   // 검색결과가 없는 경우
                        recycler_view_followers.setVisibility(View.GONE);
                        //msg_result_empty.setVisibility(View.VISIBLE);   // '검색결과가 없음' 메시지를 화면에 표시한다.
                    }else{ // 검색결과가 있다면
                        recycler_view_followers.setVisibility(View.VISIBLE); // 결과를 화면에 표시하고
                        //msg_result_empty.setVisibility(View.GONE);  // '검색결과 없음' 메시지는 보이지 않게 한다.
                    }

                    return;
                }

                Log.e(TAG, "팔로우 목록 가져오기 실패");
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                Log.e(TAG, "팔로우 목록 가져오기 실패 (onFailure)");
            }
        });


    }

    /**
     * 전화모양 버튼 클릭 시,
     * 영상통화를 할 사용자를 선택하는 모드로 변경한다. (유저 앞에 체크박스가 나타나게 된다.)
     */
    @OnClick(R.id.img_call)
    public void selectModeForMultiVideoCall(){
        selectModeFroMultiVideoCall = !selectModeFroMultiVideoCall;
        if(selectModeFroMultiVideoCall ){
            btn_request_call.setVisibility(View.VISIBLE);
        }else{
            // 기존에 선택한 모든 사용자 목록 초기화
            selectedUsersForVideoCall.clear();
            btn_request_call.setVisibility(View.GONE);
        }
        myFollowersAdapter.notifyDataSetChanged();
    }

    /**
     * 선택한 사용자들과 다중 영상통화를 시작한다.
     * 영상 통화는 Janus API 를 사용한다.
     */
    @OnClick(R.id.btn_request_call)
    public void requestVideoCall(){

        if(selectedUsersForVideoCall == null || selectedUsersForVideoCall.size() == 0){
            Toast.makeText(getContext(), "사용자를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 여기서 영상통화를 위한 권한을 허가 받은 후에 통화 화면으로 넘어가는게 맞지 않나?

        // 전화거는 화면으로 이동한다.
        Intent dialog = new Intent(getContext(), VideoCallActivity.class);
        dialog.putExtra(VideoCallActivity.DATA_KEY_SENDER, loginUser); // 통화 발신자
        // 함께 영상통화를 할 사용자 정보를 전달해야한다.
        // 왜 2가지 형태로 보내는지는 VideoCallActivity 주석 참고
        dialog.putExtra(VideoCallActivity.DATA_KEY_REC, new ArrayList<User>()); // null 을 보내고 싶은 거였는데 안되서 아무 데이터도 없는 리스트를 보냄.
        dialog.putExtra(VideoCallActivity.DATA_KEY_REC_MAP, selectedUsersForVideoCall);
        startActivity(dialog);

        // 영상통화를 시작하므로 영상통화를 하기 위해 선택한 사용자 목록 초기화
        selectedUsersForVideoCall.clear();
    }


    /**
     * 플러스모양 버튼 클릭 시,
     * 유저를 검색하여 팔로우할 수 있는 화면으로 이동한다.
     */
    @OnClick(R.id.img_add_user)
    public void openActivityForSearchUser(){
        Intent searchUser = new Intent(getContext(), SearchUserForFollowingActivity.class);
        startActivity(searchUser);
    }

    /**
     * 현재 영상통화할 사용자를 선택하는 모드라면 true를 리턴한다.
     */
    @Override
    public boolean getCurrentSelectMode() {
        return selectModeFroMultiVideoCall;
    }

    /**
     * 영상통화할 사용자를 추가하거나 삭제한다.
     */
    @Override
    public void selectUserForVideoCall(int userCode, User user) {
        if(selectedUsersForVideoCall.containsKey(userCode)){    // 이미 영상통화할 사용자에 추가된 사용자라면
            selectedUsersForVideoCall.remove(userCode); // 목록에서 제거한다.
        }else{  // 선택하지 않은 사용자라면
            if(selectedUsersForVideoCall.size() >= 3){
                Toast.makeText(getContext(), "영상통화는 최대 3명과 가능해요.", Toast.LENGTH_SHORT).show();
                return;
            }else {
                selectedUsersForVideoCall.put(userCode, user);  // 목록에 추가
            }
        }
        // 체크박스 상태를 바꾸기 위해 noti
        myFollowersAdapter.notifyDataSetChanged();

    }

    /**
     *
     * @param userCode
     * @return
     */
    @Override
    public boolean containsVideoCallUserList(int userCode) {
        return selectedUsersForVideoCall.containsKey(userCode);
    }
}
