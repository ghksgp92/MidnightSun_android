/*
 * Copyright (c) 2017 Razeware LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish, 
 * distribute, sublicense, create a derivative work, and/or sell copies of the 
 * Software in any work that is designed, intended, or marketed for pedagogical or 
 * instructional purposes related to programming, coding, application development, 
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works, 
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hyunju.jin.movie.activity.entertainment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.camera.gvision.CameraSourcePreview;
import com.hyunju.jin.movie.camera.gvision.FaceTracker;
import com.hyunju.jin.movie.camera.gvision.GraphicOverlay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Google Vision Face API 를 사용하는 카메라 화면. 얼굴 인식 마스크를 제공한다.
 *
 * 구글 공식 Face API 문서 https://developers.google.com/vision/face-detection-concepts
 * Face API 튜토리얼 https://www.raywenderlich.com/523-augmented-reality-in-android-with-google-s-face-api
 * 튜토리얼을 소스코드 분석을 위해 참고함.
 */
public final class CameraFaceDetectActivity extends SuperActivity {

    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 255; // 카메라 권한 요청 코드. 256 보다 작아야한다. (왜?)

    private CameraSource mCameraSource = null;

    /*
      버터나이프 라이브러리를 이용해 View 를 참조하는 객체 선언.
      @BindView(레이아웃 xml 파일에서의 View ID 값)을 객체에서 참조하게된다.
     */
    @BindView(R.id.camera_view) CameraSourcePreview camera_view;  // 카메라 뷰
    @BindView(R.id.faceOverlay) GraphicOverlay faceOverlay; // 카메라 위에 마스크를 그릴 뷰
    private boolean mIsFrontFacing = true;  // 현재 사용중인 카메라. 전면카메라를 사용할 경우 true 인가?

    @BindView(R.id.layout_camera_menu) RelativeLayout layout_camera_menu; // 카메라 종류 변경, 촬영 버튼 등이 있는 레이아웃
    @BindView(R.id.layout_face_filter_list) LinearLayout layout_face_filter_list; // 얼굴필터 목록을 보여주는 레이아웃

    private FaceTracker faceTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_face_detect);
        ButterKnife.bind(this); // 버터나이프 라이브러리를 이용해 View 참조를 생성한다.

        /*
          화면 가로세로 전환 등으로 인해 화면이 다시 그려진 경우,
          사용자가 사용중이었던 카메라 방향(전면/후면) 정보를 복구한다.
        */
        if (savedInstanceState != null) {
          mIsFrontFacing = savedInstanceState.getBoolean("IsFrontFacing");
        }

        // 카메라 권한이 있는지 확인한다.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {  // 카메라 권한이 있다면
            createCameraSource();
        } else {
            // 권한이 없다면 카메라 권한을 요구한다.
            requestCameraPermission();
        }
    }

  /**
   * 얼굴모양 버튼 클릭 리스너
   */
  @OnClick(R.id.img_face_detect_camera)
  public void showMaskList(){
    layout_camera_menu.setVisibility(View.GONE); // 촬영 버튼 레이아웃을 감추고
    layout_face_filter_list.setVisibility(View.VISIBLE); // 얼굴마스크 목록을 보여준다.
  }

  /**
   * 카메라 화면을 클릭하면 얼굴마스크 목록을 감춘다.
   */
  @OnClick(R.id.faceOverlay)
  public void hideMaskList(){
    layout_camera_menu.setVisibility(View.VISIBLE); // 촬영 버튼 레아웃을 보여주고
    layout_face_filter_list.setVisibility(View.GONE); // 얼굴마스크 목록을 감춘다.
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

    /**
     * 사진을 찍는다.
     */
    @OnClick(R.id.img_camera_shot)
    public void cameraShot(){

        camera_view.getmCameraSource().takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                // (궁금) 왜 bytes 형태로 넘어올까?

                // bytes 형태로 넘어온 사진을 Bitmap 형태로 바꾼다.
                Bitmap loadedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                /*
                  Bitmap 의 회전각을 조절한다.
                  현재 개발 및 테스트를 삼성기기에서 진행하고 있는데 삼성기기에 한해서 세로로 찍은 이미지가 가로로 저장되는 문제가 있어서 추가함.
                  (참고) 삼성 외의 기기에서는 테스트해보지 못함. 다른기기에서도 발생하는 문제일 수도 있겠다.
                   어쩌면 회전 작업이 아예 불필요할 수도 있음. 그렇다면 단말기 제조사나 모델명 등, 단말기를 구분할 수 있는 방법을 찾아야함.
                 */
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.setScale(-1, 1);
                rotateMatrix.postRotate(-270); // 사진이 카메라에서 보인대로 저장되도록 270도 회전한다.
                // getWindowManager().getDefaultDisplay().getRotation()); // 현재 기기의 로테이션 정보 리턴

                //  Matrix 에 설정된 옵션을 따라 Bitmap 생성
                Bitmap rotatedBitmap = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), rotateMatrix, false);

                /*
                    (중요) rotatedBitmap 은 카메라 화면만 저장된 상태다. 얼굴인식 마스크는 카메라 화면 위에 OverlayView 에 그려지므로
                    OverlayView 에 그려진 마스크도 별도로 캡처해야한다. 그 다음 두 이미지를 합쳐서 저장해야한다.
                    여기서 (주의) 해야할 점은, 카메라 화면과 OverlayView 의 크기가 달라서 크기를 동일하게 맞춰준 후 저장해야한다는 점이다.
                 */

                Canvas canvas = new Canvas(rotatedBitmap);  // 카메라 화면을 저장한 이미지로 Canvas 객체를 만든다. 이제 이 이미지에 마스크를 그릴 수 있다.

                // 현재 시점의 마스크를 캡처한다.
                camera_view.setDrawingCacheEnabled(true);  // 이 작업 반드시 해야함!
                 /*
                    View 의 DrawingCache 란? (참고) http://androidhuman.tistory.com/441
                    View 에 표시되는 내용을 Bitmap 형태로 캐싱한 것이다. 화면캡처를 하기위해선 캐시를 사용하겠다고 설정해야한다.
                */

                Bitmap maskBitmap = camera_view.getDrawingCache();
                // 카메라 촬영 사진 높이에 맞게 마스크 이미지 크기를 조절한다. scale 을 이용해 비율을 조정할 것이다.
                Bitmap maskResizeBitmap = Bitmap.createScaledBitmap(maskBitmap,  (maskBitmap.getWidth() * rotatedBitmap.getHeight()) / maskBitmap.getHeight(), rotatedBitmap.getHeight(), true);

                canvas.drawBitmap(maskResizeBitmap, 0, 0, null);    // 크기를 맞춘 마스크 이미지를 카메라 이미지에 위에 그린다.
                camera_view.setDrawingCacheEnabled(false);  // 이 라인을 drawBitmap 보다 먼저 실행하면
                /* 'Canvas: trying to use a recycled bitmap' 에러가 난다. 캡처한 이미지를 Bitmap 으로 만들어서 사용하는데도 이런 에러가 나는 이유가 뭘까? */

                // 사진을 저장하기위해 파일명을 생성한다.
                long currentTime = System.currentTimeMillis();  // 현재 시각을 파일명으로 지정
                String filename = currentTime + ".png"; // 확장자는 png 로 고정한다.

                File screenShot = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/MOVIE", filename);  //Pictures폴더 screenshot.png 파일
                FileOutputStream os = null;
                try{
                    os = new FileOutputStream(screenShot);
                    //  rotatedBitmap = resize(rotatedBitmap, 800, 600);
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 90, os);   // 비트맵을 PNG 파일로 변환
                    os.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

                if(screenShot != null){
                    // 갤러리에서 볼 수 있도록 새로운 파일이 추가됬다는 것을 알린다.
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)));
                }
            }
        });
    }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume called.");
    hideMaskList();
    startCameraSource();
  }

  @Override
  protected void onPause() {
    super.onPause();
    camera_view.stop();
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    super.onSaveInstanceState(savedInstanceState);
    savedInstanceState.putBoolean("IsFrontFacing", mIsFrontFacing);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mCameraSource != null) {
      mCameraSource.release();
    }
  }

  private void requestCameraPermission() {

    final String[] permissions = new String[]{Manifest.permission.CAMERA};
    if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
      Manifest.permission.CAMERA)) {
      ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
      return;
    }

    final Activity thisActivity = this;
    View.OnClickListener listener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
      }
    };
    Snackbar.make(faceOverlay, "This app needs access to the camera in order to detect people’s faces.",
      Snackbar.LENGTH_INDEFINITE)
      .setAction("OK", listener)
      .show();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode != RC_HANDLE_CAMERA_PERM) {
      Log.d(TAG, "Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }

    if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      // We have permission to access the camera, so create the camera source.
      Log.d(TAG, "Camera permission granted - initializing camera source.");
      createCameraSource();
      return;
    }

    // If we've reached this part of the method, it means that the user hasn't granted the app
    // access to the camera. Notify the user and exit.
    Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
      " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        finish();
      }
    };
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.app_name)
      .setMessage("no_camera_permission")
      .setPositiveButton("disappointed_ok", listener)
      .show();
  }

    /**
     * 얼굴인식 카메라를 생성한다.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = createFaceDetector(context);

        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!mIsFrontFacing) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }

        mCameraSource = new CameraSource.Builder(context, detector)
          .setFacing(facing)    // 기기에서 사용할 카메라 지정.
          .setRequestedPreviewSize(480, 360)    // 카메라 미리보기 해상도 지정.
          .setRequestedFps(60.0f)   // 카메라 프레임 속도 설정
          .setAutoFocusEnabled(true)    // 카메라 자동 초점 기능
          .build();
        /*
            각 설정 값에 대해 참고할 내용
            (해상도)
            카메라 해상도가 낮을수록 저사양 기기에서 보다 빠른 얼굴인식 결과를 얻을 수 있다.
            해상도가 높으면 고사양 기기에 적합하며 작은얼굴과 얼굴 특징 감지 결과를 더 정확하게 얻을 수 있다.
            (프레임) 프레임 수가 높을 수록 더 빠르게 얼굴 추적 가능. 높아질수록 프로세서 성능이 요구된다.
         */
    }

    /**
     * 얼굴 탐지기를 생성하고 사용할 준비를 한다.
     */
    @NonNull
    private FaceDetector createFaceDetector(final Context context) {

        // 얼굴 탐지기를 생성한다.
        FaceDetector detector = new FaceDetector.Builder(context)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)    // 탐지할 얼굴 랜드마크 종류 설정
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)    // 탐지할 얼굴 특징 정보 설정
            .setTrackingEnabled(true)   // 얼굴별 ID 기능 활성화. 여러 얼굴을 탐지하기 위해서는 true 로 설정해야함.
            .setMode(FaceDetector.FAST_MODE)
            .setProminentFaceOnly(mIsFrontFacing)   // 눈에 가장 잘띄는 얼굴만 탐지하도록 한다. 근데 여기에 왜 mIsFrontFacing 가 나와?
            .setMinFaceSize(mIsFrontFacing ? 0.35f : 0.15f)
            .build();

        // 얼굴을 탐지한 경우를 처리하는 Factory 객체 생성
        // 프로세서에 탐지 결과를 전달하는 작업을 한다. 왜 프로세서에 전달해야하지?
        MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
            @Override
            public Tracker<Face> create(Face face) {
                // 탐지된 얼굴 인식 결과로 FaceTracker 객체를 생성한다.
                faceTracker = new FaceTracker(faceOverlay, context, mIsFrontFacing);
                return faceTracker;
            }
        };

        // 프로세서와 얼굴 탐지기를 연결한다.
        Detector.Processor<Face> processor = new MultiProcessor.Builder<>(factory).build();
        detector.setProcessor(processor);

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");

            // Check the device's storage.  If there's little available storage, the native
            // face detection library will not be downloaded, and the app won't work,
            // so notify the user.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Log.w(TAG, "low_storage_error");
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.app_name)
                        .setMessage("low_storage_error")
                        .setPositiveButton("disappointed_ok", listener)
                        .show();
            }
        }
        return detector;

    }


    private void startCameraSource() {
        // Make sure that the device has Google Play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                camera_view.start(mCameraSource, faceOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    /*
        얼굴 마스크 선택 시, 선택한 마스크로 교체하는 메서드.
        급해서 이렇게 하드 코딩을 해버림. 마스크 종류가 늘어나면 리스트로 변경하는게 좋겠음~
    */

    @OnClick(R.id.tv_filter_none)
    public void changeMaskNone(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(0);
            layout_camera_menu.setVisibility(View.VISIBLE); // 촬영 버튼 레아웃을 보여주고
            layout_face_filter_list.setVisibility(View.GONE); // 얼굴마스크 목록을 감춘다.
        }
    }

    @OnClick(R.id.btn_mask1)
    public void changeMask1(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(1);
        }
    }

    @OnClick(R.id.btn_mask2)
    public void changeMask2(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(2);
        }
    }

    @OnClick(R.id.btn_mask3)
    public void changeMask3(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(3);
        }
    }

    @OnClick(R.id.btn_mask4)
    public void changeMask4(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(4);
        }
    }

    @OnClick(R.id.btn_mask5)
    public void changeMask5(){
        if(faceTracker != null){
            faceTracker.getmFaceGraphic().setCurrentMaskPosition(5);
        }
    }


}