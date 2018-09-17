package com.hyunju.jin.movie.activity;

import android.content.Intent;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.entertainment.EntertainmentActivity;
import com.hyunju.jin.movie.activity.posting.PostingMainActivity;
import com.hyunju.jin.movie.activity.movie.UserMainActivity;
import com.hyunju.jin.movie.activity.recommend.RecommendMovieActivity;
import com.hyunju.jin.movie.activity.user.MyPageActivity;

import butterknife.OnClick;

/**
 * 화면에 하단 메뉴가 있는 액티비티들이 상속받는 액티비티.
 * 레이아웃은 없으며 하단 메뉴의 이벤트 처리를 한곳에 모아 정의해두면 관리가 편리하기 때문에 만들었음.
 */
public class BottomMenuActivity extends SuperActivity {

    /**
     * [ETC] 버튼 클릭이벤트 처리.
     * 카메라, AR 포스터 인식, AR 캐릭터 컨트롤 메뉴가 있는 화면으로 이동한다.
     */
    @OnClick(R.id.layout_bottom_menu_etc)
    public void goCamera(){
        Intent camera = new Intent(getContext(), EntertainmentActivity.class);
        camera.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(camera);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * [POSTING] 버튼 클릭이벤트 처리.
     * 포스팅 게시판으로 이동한다.
     */
    @OnClick(R.id.layout_bottom_menu_community)
    public void goCommunity(){
        Intent community  = new Intent(getContext(), PostingMainActivity.class);
        // 탭처럼 작동하도록 하기위해 새 메뉴를 누르면 이전까지의 액티비티들을 모두 제거한다.
        community.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        //community.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(community);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * [MOVIE] 버튼 클릭이벤트 처리.
     * 인기 영화 조회, 영화 검색을 할 수 있는 화면으로 이동한다. 앱의 메인화면이기도 하다.
     */
    @OnClick(R.id.layout_bottom_menu_home)
    public void goHome(){
        Intent home = new Intent(getContext(), UserMainActivity.class);;
        // 탭처럼 작동하도록 하기위해 새 메뉴를 누르면 이전까지의 액티비티들을 모두 제거한다.
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        home.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(home);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * [RECOMMEND] 버튼 클릭이벤트 처리.
     * 사용자별 추천영화 화면으로 이동한다.
     */
    @OnClick(R.id.layout_bottom_menu_recommend)
    public void goRecommend(){
        Intent recommend = new Intent(getContext(), RecommendMovieActivity.class);;
        // 탭처럼 작동하도록 하기위해 새 메뉴를 누르면 이전까지의 액티비티들을 모두 제거한다.
        recommend.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(recommend);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * [MY] 버튼 클릭이벤트 처리.
     * 마이페이지 화면으로 이동한다.
     */
    @OnClick(R.id.layout_bottom_menu_my_page)
    public void goMyPage(){
        Intent myPage = new Intent(getContext(), MyPageActivity.class);;
        // 탭처럼 작동하도록 하기위해 새 메뉴를 누르면 이전까지의 액티비티들을 모두 제거한다.
        myPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myPage);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
