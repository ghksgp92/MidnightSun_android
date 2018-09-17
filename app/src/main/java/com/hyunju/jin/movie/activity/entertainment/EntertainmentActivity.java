package com.hyunju.jin.movie.activity.entertainment;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.BottomMenuActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * [ETC] 메뉴 화면. 사용자에게 재미를 주는 기능들을 모아둔 화면이다.
 */
public class EntertainmentActivity extends BottomMenuActivity {

    @BindView(R.id.img_bottom_menu_etc) ImageView img_bottom_menu_etc;
    @BindView(R.id.tv_bottom_menu_label_camera) TextView tv_bottom_menu_label_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entertainment);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){
        // 하단 메뉴에 현재 [ETC] 메뉴를 보고 있음을 표시한다.
        img_bottom_menu_etc.setImageResource(R.drawable.ic_menu_etc_on);
        tv_bottom_menu_label_camera.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null));
    }

    /**
     * [카메라] 버튼 이벤트 처리. 이미지 필터 카메라를 실행한다.
     */
    @OnClick(R.id.layout_camera)
    public void openCamera(){
        Intent camera = new Intent(getContext(), CameraFilterActivity.class);
        startActivity(camera);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * [AR 예고편 재생] 버튼 이벤트 처리.
     * 영화 포스터를 인식해서 그 위에 영화 예고편을 재생하는 액티비티를 실행한다.
     */
    @OnClick(R.id.layout_ar_poster)
    public void openARPoster(){
        Intent ar = new Intent(getContext(), ARPosterActivity.class);
        startActivity(ar);
    }

    /**
     * [AR 캐릭터] 버튼 이벤트 처리.
     * 영화 포스터를 인식해서 그 위에 돌고래 캐릭터를 컨트롤할 수 있는 AR 을 띄우는 유니티 어플을 실행한다.
     * 어플이 설치되어있지 않다면 에러 발생함.
     */
    @OnClick(R.id.layout_ar_test)
    public void openARTest(){
        Intent whale = new Intent(Intent.ACTION_MAIN);
        whale.setComponent(new ComponentName("com.hjjin.ar.test", "com.unity3d.player.UnityPlayerActivity"));
        startActivity(whale);
    }

    /**
     * [2D 게임 : 산타의 모험] 버튼 이벤트 처리
     * 유니티 기반의 2D 게임 어플을 실행한다. 어플이 설치되어있지 않다면 에러 발생함.
     */
    @OnClick(R.id.layout_santa_game)
    public void openSantaGame(){
        Intent santa = new Intent(Intent.ACTION_MAIN);
        santa.setComponent(new ComponentName("com.hyunju.jin.santa", "com.unity3d.player.UnityPlayerActivity"));
        startActivity(santa);
    }

}
