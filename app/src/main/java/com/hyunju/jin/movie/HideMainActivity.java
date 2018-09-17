package com.hyunju.jin.movie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hyunju.jin.movie.activity.movie.VideoPlayerActivity;
import com.hyunju.jin.movie.activity.user.LoginActivity;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.movie.UserMainActivity;

import org.apache.commons.lang3.StringUtils;
import org.wysaid.view.ImageGLSurfaceView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 앱 개발 과정에서 편의를 위해 특정 액티비티에 대한 바로가기를 모아둔 화면
 * 사용자들에게 노출되면 안됨.
 */
public class HideMainActivity extends SuperActivity {

    @BindView(R.id.tv_hls_url) TextView tv_hls_url;
    @BindView(R.id.gl_image) ImageGLSurfaceView gl_image;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_main);
        ButterKnife.bind(this);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dep_movie_poster);
        gl_image.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
            @Override
            public void surfaceCreated() {
                gl_image.setImageBitmap(mBitmap);
            }
        });
    }

    @OnClick({ R.id.btn_main, R.id.btn_user_main, R.id.btn_hls_sample, R.id.btn_vod})
    public void onClick(View view){
        Log.d(TAG, "onClick");
        switch(view.getId()) {
            case (R.id.btn_main):
                Intent main = new Intent(this, LoginActivity.class);
                startActivity(main);
                break;
            case (R.id.btn_user_main):
                Intent userMain = new Intent(this, UserMainActivity.class);
                startActivity(userMain);
                break;
            case(R.id.btn_hls_sample):
                Intent hlsSample = new Intent(this, VideoPlayerActivity.class);
                hlsSample.putExtra("hlsURL", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8");
                startActivity(hlsSample);
                break;
            case(R.id.btn_vod):
                Intent vod = new Intent(this, VideoPlayerActivity.class);
                String hlsURL = tv_hls_url.getText().toString();
                if(StringUtils.isEmpty(hlsURL)){
                    hlsURL = "http://183.111.227.218/hls/lalaland.m3u8";
                }

                vod.putExtra("hlsURL", hlsURL);
                startActivity(vod);
                break;
        }
    }

}
