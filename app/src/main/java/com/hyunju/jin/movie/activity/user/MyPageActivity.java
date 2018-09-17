package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.BottomMenuActivity;
import com.hyunju.jin.movie.adapter.user.MyPageViewPagerAdapter;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 앱 하단의 [MYPAGE] 를 누르면 보이는 화면.
 * 영화 VOD 이어보기, 자신이 쓴 포스팅 확인, 영화 컬렉션 조회, 모바일 지갑 조회 기능을 사용할 수 있다. 
 */
public class MyPageActivity extends BottomMenuActivity{

    // 하단 메뉴 [MYPAGE] 와 관련된 View 참조
    @BindView(R.id.img_user_profile) CircularImageView img_user_profile;
    @BindView(R.id.img_bottom_menu_my_page) ImageView img_bottom_menu_my_page;
    @BindView(R.id.tv_bottom_menu_label_my_page) TextView tv_bottom_menu_label_my_page;

    // 사용자 아이디를 보여주는 텍스트뷰
    @BindView(R.id.tv_user_id) TextView tv_user_id;

    @BindView(R.id.tab_my_page_category) TabLayout tab_my_page_category; // 마이페이지 메뉴를 보여주는 탭
    @BindView(R.id.vpager_my_page) ViewPager vpager_my_page; // 탭과 연결된 ViewPager
    MyPageViewPagerAdapter mypageViewPagerAdapter;
    String[] myPageTabTitles;

    UserService userService; // 사용자 정보, 포스팅, 컬렉션 정보를 서버에 요청하기 위한 HTTP 서비스 객체.
    // Q. 이런 애들은 주석을 어떻게 달아야하는 걸까? 이렇게 적어도 다른사람이 이해하는게 어려울 것 같다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        ButterKnife.bind(this);
        initialize();
    }

    /**
     * 변수 할당, 객체 초기화 및 화면 준비 작업.
     */
    private void initialize(){

        // 하단 메뉴 버튼 색을 바꿔 현재 [MYPAGE] 화면을 보고 있음을 표시한다.
        img_bottom_menu_my_page.setImageResource(R.drawable.ic_menu_my_page_on);
        tv_bottom_menu_label_my_page.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null));

        // 서버와 통신하기 위해 HTTP 서비스 객체를 초기화 한다.
        userService = RetrofitClient.getUserService();

        // 마이페이지 메뉴 목록을 만든다. 인덱스 순서대로 메뉴가 만들어진다.
        myPageTabTitles = new String[3];
        myPageTabTitles[0] = MyPageViewPagerAdapter.TAB_WATCHING_MOVIE; // 영화 VOD 이어보기
        myPageTabTitles[1] = MyPageViewPagerAdapter.TAB_POSTING; // 내가 쓴 포스팅 조회
        myPageTabTitles[2] = MyPageViewPagerAdapter.TAB_MOVIE_COLLETION; // 영화 컬렉션 조회

        // 메뉴 목록으로 Tab 을 생성하고 ViewPager 와 연결한다.
        mypageViewPagerAdapter = new MyPageViewPagerAdapter(getSupportFragmentManager(), myPageTabTitles);
        vpager_my_page.setAdapter(mypageViewPagerAdapter);
        tab_my_page_category.setupWithViewPager(vpager_my_page);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    /**
     * 서버에 사용자 정보를 요청한다.
     */
    private void loadUserInfo(){

        // 서버에 보낼 데이터 목록 생성 (Key, Value)
        HashMap<String, String> data_getUser = new HashMap<String, String>();
        data_getUser.put("userCode", loginUser.getUserCode()+"");   // 현재 로그인한 사용자 코드를 서버에 보낸다.

        Call<User> call_getUser = userService.getUser("getUser", data_getUser);
        call_getUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){

                    loginUser = (User) response.body(); // 현재 로그인한 사용자 정보를 서버에서 리턴받은 정보로 업데이트한다.
                    tv_user_id.setText(loginUser.getId());  // 아이디를 화면에 표시한다.

                    if( !StringUtils.isEmpty(loginUser.getProfileImg())){   // 프로필 사진이 있을 경우
                        // 서버에서 프로필 사진을 로드한다. ProfileImg 는 URL 형식이므로 그대로 사용해도 된다.
                        Glide.with(getContext()).load(loginUser.getProfileImg()).apply(new RequestOptions().error(R.drawable.img_user)).into(img_user_profile);
                    }else{
                        Glide.with(getContext()).load(R.drawable.img_user).into(img_user_profile);
                    }
                    return;
                }

                Log.e(TAG, "사용자 정보 로드 실패");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "사용자 정보 로드 실패 (onFailure)");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 사용자 정보 수정 화면으로 이동한다.
     */
    @OnClick(R.id.btn_my_info_edit)
    public void editMyPageConfig(){
        Intent config = new Intent(getContext(), MyPageConfigActivity.class);
        startActivity(config);
    }

    @OnClick(R.id.layout_friend_list)
    public void openActivityMyFollowerList(){
        Intent myFollowerList = new Intent(getContext(), MyFollowersActivity.class);
        startActivity(myFollowerList);
    }


    /**
     * 블록체인 기반 토큰 지갑 화면으로 이동한다. 토큰은 포인트 개념으로 생각하면 된다.
     */
    @OnClick(R.id.layout_token)
    public void openTokenWallet(){
        Intent wallet = new Intent(getContext(), WalletRegisterActivity.class);
        startActivity(wallet);
    }


    /**
     * 이어보기 정보를 리셋한다. 개발중에 필요해서 만듬.
     * @param view
     */
    public void tmp(View view){
        SharedPreferencesBuilder.getSharedDefaultConfigEditor(getContext()).remove(SharedPreferencesBuilder.KEY_WATCHING_MOVIE).commit();
    }

}
