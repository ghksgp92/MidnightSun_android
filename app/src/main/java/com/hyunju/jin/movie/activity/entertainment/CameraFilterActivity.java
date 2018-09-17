package com.hyunju.jin.movie.activity.entertainment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.nofolder.ImageFilterAdapter;
import com.hyunju.jin.movie.activity.gallery.nofolder.ImageFilterListenter;
import com.hyunju.jin.movie.datamodel.ImageFilter;

import org.opencv.android.OpenCVLoader;
import org.wysaid.camera.CameraInstance;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.view.CameraGLSurfaceView;
import org.wysaid.view.CameraRecordGLSurfaceView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *
 */
public class CameraFilterActivity extends SuperActivity implements ImageFilterListenter {

    static {
        if (!OpenCVLoader.initDebug()) {    // OpenCV 라이브러리 로드
            Log.e("FaceFilterCamera", "OpenCV 라이브러리 로드 실패");
        }
    }

    private final int PM_CAMERA = 1000; // 사용자 카메라 사용권한 요청코드
    @BindView(R.id.camera_view) CameraRecordGLSurfaceView camera_view; // 이미지 필터 카메라 뷰
    @BindView(R.id.img_camera_switch) ImageView img_camera_switch;    // 카메라 방향전환 버튼
    @BindView(R.id.layout_camera_menu) RelativeLayout layout_camera_menu;

    @BindView(R.id.recycler_filter_list) RecyclerView recycler_filter_list; // 사용 가능한 필터 목록을 보여주는 View
    ArrayList<ImageFilter> imageFilters;
    ImageFilterAdapter imageFilterAdapter;

    String currentCamera;
    String currentFilterConfig;

    int displayWidth;
    int displayHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_filter);
        ButterKnife.bind(this);

        initialize();
    }

    /**
     * 카메라를 사용하기 전 해야할 작업들
     */
    private void initialize(){

        // 카메라 뷰가 화면에 가득차도록 하기 위해서 폰의 화면 사이즈를 구한다.
        Display display = getWindowManager().getDefaultDisplay();
        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1 ){ // SDK 13 부터
            Point size = new Point();
            display.getSize(size);
            displayWidth = size.x;
            displayHeight = size.y;
        }else{
            displayWidth = display.getWidth();
            displayHeight = display.getHeight();
        }

        // 필터 목록을 불러온다.
        imageFilters = ImageFilter.getGalleryFilterList();
        currentFilterConfig = imageFilters.get(0).getFilter_config();   // 원본 필터
        imageFilterAdapter = new ImageFilterAdapter(getContext(), imageFilters, this);
        recycler_filter_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycler_filter_list.setAdapter(imageFilterAdapter);

        currentCamera = ""; // 반드시 공백으로 설정할 것.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 사용자 앨범에 접근할 수 있는 권한을 얻는다.
        String[] perms = {Manifest.permission.CAMERA};
        if ( !EasyPermissions.hasPermissions(this, perms)) {    // 권한이 없다면
            EasyPermissions.requestPermissions(this, "권한이 필요합니다.", PM_CAMERA, perms);
        }else {
            resumeFilterCamera();   // 필터 카메라를 활성화 한다.
        }
    }

    /**
     * 필터 카메라 활성화
     */
    private void resumeFilterCamera(){

        if(currentCamera.equals("filter")){
            return;
        }

        // 필터 카메라 초기화
        camera_view.presetCameraForward(false);
        camera_view.presetRecordingSize(displayWidth, displayHeight);  // 이걸 해야 얼굴인식 표시가 나옴
        camera_view.setPictureSize(2048, 2048, true); // > 4MP
        camera_view.setZOrderOnTop(false);
        camera_view.setZOrderMediaOverlay(true);

        camera_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: { // 카메라 화면을 누르면
                        recycler_filter_list.setVisibility(View.GONE); // 이미지 필터 목록이 사라지고
                        layout_camera_menu.setVisibility(View.VISIBLE); // 사진 촬영, 필터(얼굴/이미지) 선택 버튼이 보이도록 한다.
                    }
                }
                return true;
            }
        });

        camera_view.setOnCreateCallback(new CameraGLSurfaceView.OnCreateCallback() {
            @Override
            public void createOver() {
                Log.e(TAG, "필터 카메라 생성");
            }
        });

        camera_view.setVisibility(View.VISIBLE);
        img_camera_switch.setVisibility(View.VISIBLE);
        CameraInstance.getInstance().stopCamera(); // 이건 뭐니?
        camera_view.onResume(); // 필터 카메라를 활성화한다.
        currentCamera = "filter";
    }


    /**
     * 필터 카메라 비활성화
     */
    private void releaseFilterCamera(){
        CameraInstance.getInstance().stopCamera();
        currentCamera = "";
        camera_view.release(null);
        camera_view.onPause();
        camera_view.setVisibility(View.INVISIBLE);
        recycler_filter_list.setVisibility(View.GONE);
        img_camera_switch.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseFilterCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case PM_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    camera_view.onResume();
                }else {
                    Toast.makeText( getContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

    }

    /**
     * 얼굴 필터 카메라로 변경한다.
     */
    @OnClick(R.id.img_face_swap_camera)
    public void switchToFaceSwapCamera(){
        Intent faceSwap = new Intent(getContext(), CameraFaceSwapActivity.class);
        startActivity(faceSwap);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    @OnClick(R.id.img_face_detect_camera)
    public void switchToFaceDetectCamera(){
        Intent faceDetect = new Intent(getContext(), CameraFaceDetectActivity.class);
        startActivity(faceDetect);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * 카메라 방향을 바꾸는 메서드
     */
    @OnClick(R.id.img_camera_switch)
    public void switchCamera(){
        camera_view.switchCamera();
    }

    /**
     * 필터 목록을 보여주는 메서드
     */
    @OnClick(R.id.img_camera_filter)
    public void showFilterList(){
        resumeFilterCamera();  // 필터 카메라로 변경한다.
        recycler_filter_list.setVisibility(View.VISIBLE);   // 필터 목록을 보여준다.
        layout_camera_menu.setVisibility(View.GONE);
    }


    @OnClick(R.id.img_camera_shot)
    public void cameraShot(){
        camera_view.takePicture(new CameraRecordGLSurfaceView.TakePictureCallback() {
            @Override
            public void takePictureOK(Bitmap bmp) {
                if (bmp != null) {
                    long currentTime = System.currentTimeMillis();
                    String filename = currentTime + ".jpg";
                    //bmp.recycle();    // 원래 안그랬는데 얘때문에 지금 에러남.
                    String s = ImageUtil.saveBitmap(bmp);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
                }
            }
        }, null, currentFilterConfig, 1.0f, true);
    }



    @Override
    public void chagneFilter(int filterCode, String filterConfig) {
        camera_view.setFilterWithConfig(filterConfig);
        currentFilterConfig = filterConfig;
    }

    @Override
    public int currentPosition() {
        return 0;
    }
}
