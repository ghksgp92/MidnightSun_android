package com.hyunju.jin.movie.activity.entertainment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.camera.Constant;
import com.hyunju.jin.movie.camera.DownloaderStatus;
import com.hyunju.jin.movie.camera.FaceTrackingCameraView;
import com.hyunju.jin.movie.camera.IDetectionBasedTracker;
import com.hyunju.jin.movie.camera.IFaceSwapper;
import com.wang.avi.AVLoadingIndicatorView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.wysaid.myUtils.ImageUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 얼굴 인식 필터를 사용한 카메라.
 * 현재 제공하는 필터: FaceSwap
 *
 * [NDK, Dlib, OpenCV]
 *
 * 아래를 참고하여 작성함.
 * https://github.com/tunaemre/Face-Swap-Android
 * https://www.learnopencv.com/face-swap-using-opencv-c-python/
 */
public class CameraFaceSwapActivity extends SuperActivity implements LoadingListener{

    static {
        if (!OpenCVLoader.initDebug()) {    // OpenCV 라이브러리 로드, 실패시 false
            Log.e("FaceFilterCamera", "OpenCV 라이브러리 로드 실패");
        }
    }

    @BindView(R.id.opencv_camera_view) FaceTrackingCameraView opencv_camera_view;   // 카메라 뷰
    @BindView(R.id.img_face_swap_mask) ImageView img_face_swap_mask; // 서로 체인지할 얼굴 2개의 기준선
    @BindView(R.id.progress_bar) ProgressBar progress_bar;  // 카메라 필터가 준비될때까지 로딩바 표시함. 얼굴인식에 필요한 데이터를 로드하는 작업으로, ExecuteRuntimeFilesLoader 를 참고할 것.

    // 카메라 필터가 준비될때까지 로딩바 표시함. 얼굴인식에 필요한 데이터를 로드하는 작업으로, ExecuteRuntimeFilesLoader 를 참고할 것.
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator; //
    @BindView(R.id.tv_loading) TextView tv_loading;

    // 라이브 얼굴 필터와 관련된 View 참조
    boolean hasFaceSwapFilter = false;  // true 일 경우 FaceSwap 필터 다운로드 한 상태
    boolean availableFaceSwapFilter = false;   // true 일 경우 FaceSwap 필터를 사용할 준비가 된 상태

    @BindView(R.id.img_camera_switch) ImageView img_camera_switch;
    @BindView(R.id.layout_camera_menu) RelativeLayout layout_camera_menu;   // 촬영, 필터 선택 레이아웃

    private AsyncTask<Void, Void, Boolean> mRuntimeFilesLoaderTask = null;  // 얼굴인식 학습파일 다운로드
    private FileDownloader fileDownloaderTask; // 필터 다운로드 객체
    private boolean mIsRuntimeFilesLoaded = false;  // 이게 왜 필요하지?
    private IDetectionBasedTracker mNativeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 카메라 사용중이므로 화면 포커스를? 잃지 않기 위해 설정
        setContentView(R.layout.activity_camera_face_swap);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Log.e("OpenCVFaceDetect", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        }else {
            Log.e("OpenCVFaceDetect", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * 얼굴인식을 위한 openCV 라이브러리 다운로드 콜백 리스너
     */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.e("FaceSwapper", "OpenCV loaded successfully");

                checkFilterDownload();  // 다운로드 된 필터를 확인한다.

                opencv_camera_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {   // 카메라 화면을 누르면
                        if(availableFaceSwapFilter){   // 카메라가 최초로 실행되고 필터가 준비되고 나면
                            layout_camera_menu.setVisibility(View.VISIBLE); // 촬영 버튼이 나타나도록 한다.
                        }
                    }
                });

                new ExecuteRuntimeFilesLoader().Run();  // 얼굴인식 필터 미리 로드

            }else{
                super.onManagerConnected(status);
            }
            opencv_camera_view.setCvCameraViewListener(mCameraViewDefaultListener);
            opencv_camera_view.enableView();    // 최초 실행 시엔 필터가 적용되지 않은 상태이므로 카메라를 바로 시작한다.
        }
    };

    /**
     * 필터가 다운로드 됬는지 확인하는 메서드
     * 필터가 다운로드 되지 않았다면 다운로드가 필요하다는 표시를 보여준다.
     * @return
     */
    private void checkFilterDownload() {

        // FaceSwap 필터가 다운로드 됬는지 확인한다.
        File localFile = new File(getDir(Constant.FaceLandmarksDownloadPath, Context.MODE_PRIVATE), Constant.FaceLandmarksFileName);
        if( localFile.exists() ){
            hasFaceSwapFilter = true;
        }else{
            hasFaceSwapFilter = false;
        }
    }

    /**
     * 프로그래스바를 띄운다.
     * 카메라를 사용하기 위해 필요한 작업을 시작할 때 호출함
     */
    @Override
    public void showLoading() {
        img_camera_switch.setVisibility(View.GONE);
        layout_camera_menu.setVisibility(View.GONE);
        tv_loading.setVisibility(View.VISIBLE);
        loading_indicator.smoothToShow();

        // 이땐 화면을 클릭해도 카메라 촬영 메뉴가 안나오도록 해야하는데, 그 처리는 아직 안했음!
    }

    /**
     * 프로그래스바를 감춘다.
     * 카메라를 사용하기 위해 필요한 작업을 완료하면 호출함
     */
    @Override
    public void hideLoading() {
        img_camera_switch.setVisibility(View.VISIBLE);  // 카메라 방향 전환 버튼을 보여준다.
        layout_camera_menu.setVisibility(View.VISIBLE); // 카메라 기본 메뉴 레이아웃을 보여준다. 레이아웃에는 촬영버튼과 다른 카메라로 전환하는 버튼이 있다.
        tv_loading.setVisibility(View.GONE);
        loading_indicator.smoothToHide();

        Log.e(TAG, "hideLoading() 완료");
    }


    /**
     * 필터 다운로드를 하는 AsyncTask
     */
    private class FileDownloader extends AsyncTask<Void, Integer, DownloaderStatus> {
        private HttpURLConnection mConnection;

        private int mDownloadedByte = 0;
        private boolean mIsDownloading = true;

        private String mFileURL;
        private String mLocalPath;
        private String mLocalFileName;
        private File mLocalFile;
        private int mFilterCode;

        private static final int MAX_BUFFER_SIZE = 256;

        private FileDownloader(String fileURL, String localPath, String localFileName, int filterCode) {
            this.mFileURL = fileURL;
            this.mLocalPath = localPath;
            this.mLocalFileName = localFileName;
            this.mFilterCode = filterCode;
            executeOnExecutor(THREAD_POOL_EXECUTOR);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected DownloaderStatus doInBackground(Void... param) {
            try {
                mLocalFile = new File(getDir(mLocalPath, Context.MODE_PRIVATE),  mLocalFileName);
                if (mLocalFile.exists()) mLocalFile.delete();
                mLocalFile.createNewFile();
            }catch (Exception e) {
                e.printStackTrace();
                return DownloaderStatus.FileSaveError;
            }

            try{
                mConnection = (HttpURLConnection) new URL(mFileURL).openConnection();
                final double fileSize = mConnection.getContentLength();

                Log.e("File Size", String.valueOf(fileSize));

                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(mLocalFile));
                mConnection.connect();

                InputStream inputStream = mConnection.getInputStream();

                while (mIsDownloading) {
                    byte buffer[];
                    if (fileSize - mDownloadedByte >= MAX_BUFFER_SIZE) {
                        buffer = new byte[MAX_BUFFER_SIZE];
                    }else {
                        buffer = new byte[(int) (fileSize - mDownloadedByte)];
                    }

                    int read = inputStream.read(buffer);
                    if (read == -1){
                        break;
                    }
                    outputStream.write(buffer, 0, read);
                    mDownloadedByte += read;
                }

                if (mIsDownloading) mIsDownloading = false;
                outputStream.close();
                return DownloaderStatus.Success;

            }
            catch (Exception e) {
                e.printStackTrace();
                return DownloaderStatus.DownloadError;
            }
        }

        @Override
        protected void onCancelled() {
            if (mLocalFile.exists()) mLocalFile.delete();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(DownloaderStatus result) {
            fileDownloaderTask = null;

            switch(mFilterCode){
                case 1: // FaceSwap 필터
                    if (result == DownloaderStatus.Success){
                        hasFaceSwapFilter = true;
                    }else{
                        Log.e(TAG, "얼굴 필터 다운로드 실패");
                    }

                    break;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (opencv_camera_view != null) {
            opencv_camera_view.disableView(); // 카메라 종료
        }
    }

    @Override
    protected void onDestroy() {
        if (mRuntimeFilesLoaderTask != null) {
            mRuntimeFilesLoaderTask.cancel(true);
            mRuntimeFilesLoaderTask = null;
        }

        super.onDestroy();
        if (opencv_camera_view != null) opencv_camera_view.disableView();
    }

    private class ExecuteRuntimeFilesLoader
    {
        @SuppressLint("NewApi")
        private void Run()  {
            mRuntimeFilesLoaderTask = new BaseAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            availableFaceSwapFilter = false;    // 얼굴 바꾸기 필터가 아직 준비되지 않았으므로 false
            showLoading();
        }

        private class BaseAsyncTask extends AsyncTask<Void, Void, Boolean> {
            protected Boolean doInBackground(Void... args) {
                try {
                    String poseModelPath = new File(getDir(Constant.FaceLandmarksDownloadPath, Context.MODE_PRIVATE), Constant.FaceLandmarksFileName).getAbsolutePath();
                    if (!IFaceSwapper.loadPoseModel(poseModelPath)) {
                        // 지금 로드를 못해서 얼굴 체인지를 못하는데.. 왜 로드를 못하는지를 모르겠다.
                        return false;
                    }

                    Log.i("FaceSwapper", "PoseModel loaded successfully");
                    String cascadePath = IDetectionBasedTracker.copyCascade(getApplicationContext(), R.raw.haarcascade_frontalface_alt2, "haarcascade_frontalface_alt2.xml");
                    if (cascadePath == null) {
                        return false;
                    }

                    Log.i("FaceSwapper", "Cascade copied successfully");
                    mNativeDetector = new IDetectionBasedTracker(cascadePath, 0);
                    mNativeDetector.start();

                    return true;
                }catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            protected void onPostExecute(Boolean result) {
                mRuntimeFilesLoaderTask = null;
                mIsRuntimeFilesLoaded = result;

                if (result && opencv_camera_view != null) {
                    opencv_camera_view.enableView();
                    Toast.makeText(getContext(), "필터 준비 완료!", Toast.LENGTH_SHORT).show();
                    hideLoading();
                    availableFaceSwapFilter = true;
                    selectFaceSwapFilter();

                }else {
                    Toast.makeText(CameraFaceSwapActivity.this, "Runtime error.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     *  얼굴 스왑을 사용할 준비가 됬는지 확인하고, 준비가 되었다면 화면에 가이드라인을 표시한다.
     */
    public void selectFaceSwapFilter(){
        if(!hasFaceSwapFilter){  // 필터 다운로드가 되지 않았다면 다운로드를 해야한다.
            // 여러번 누르지 못하도록 처리해야함.
            Toast.makeText(getContext(), "필터 다운로드중! 조금만 기다려주세요.", Toast.LENGTH_SHORT).show();
            new FileDownloader(Constant.FaceLandmarksURL, Constant.FaceLandmarksDownloadPath, Constant.FaceLandmarksFileName, 1);
            return;
        }else if(!availableFaceSwapFilter){
            Toast.makeText(getContext(), "필터 준비중! 조금만 기다려주세요.", Toast.LENGTH_SHORT).show();
            // selectFaceSwapFilter(); // 무한 루프로 빠질 위험 있음
            return;
        }
        opencv_camera_view.disableView();
        img_face_swap_mask.setVisibility(View.VISIBLE);
        opencv_camera_view.setCvCameraViewListener(mCameraViewListener);
        opencv_camera_view.enableView();
    }


    private Mat mRgba, mGray, mTemp;
    private int mAbsoluteFaceSize = 0;
    private boolean mIsTakePictureRequested= false;
    private  float  mRelativeFaceSize  =  0.2f ;

    // 일반 카메라 리스너
    private CameraBridgeViewBase.CvCameraViewListener2 mCameraViewDefaultListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {
            mGray = new Mat();
            mRgba = new Mat();
        }

        @Override
        public void onCameraViewStopped() {
            mGray.release();
            mRgba.release();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();

            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }

            // 사진 촬영
            if (mIsTakePictureRequested) {
                mTemp = mRgba;
                mIsTakePictureRequested = false;
                runTakePictureTask();
            }

            return mRgba;
        }
    };

    // FaceSwap 카메라 리스너
    private CameraBridgeViewBase.CvCameraViewListener2 mCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStopped() {
            mGray.release();
            mRgba.release();
        }

        @Override
        public void onCameraViewStarted(int width, int height) {
            mGray = new Mat();
            mRgba = new Mat();
        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            mGray = inputFrame.gray();
            mRgba = inputFrame.rgba();

            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
               mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
            }

            MatOfRect faces = new MatOfRect();

            if (mNativeDetector != null) {
                mNativeDetector.detect(mGray, faces);
            }

            Rect[] facesArray = faces.toArray();
            //Log.e("OpenCVFaceDetect", "FaceCount:" + facesArray.length);
            if (facesArray.length == 2) {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        img_face_swap_mask.setVisibility(View.GONE);
                        findViewById(R.id.imgFaceSwapMaskLeftBorder).setVisibility(View.GONE);
                        findViewById(R.id.imgFaceSwapMaskRightBorder).setVisibility(View.GONE);
                        findViewById(R.id.imgFaceSwapMaskTopBorder).setVisibility(View.GONE);
                        findViewById(R.id.imgFaceSwapMaskBottomBorder).setVisibility(View.GONE);
                    }
                });
            } else {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        img_face_swap_mask.setVisibility(View.VISIBLE);
                        findViewById(R.id.imgFaceSwapMaskLeftBorder).setVisibility(View.VISIBLE);
                        findViewById(R.id.imgFaceSwapMaskRightBorder).setVisibility(View.VISIBLE);
                        findViewById(R.id.imgFaceSwapMaskTopBorder).setVisibility(View.VISIBLE);
                        findViewById(R.id.imgFaceSwapMaskBottomBorder).setVisibility(View.VISIBLE);
                    }
                });
            }

            // 얼굴을 바꿔주는 작업을 한다.
            IFaceSwapper.swapFaces(mRgba.getNativeObjAddr(), faces.getNativeObjAddr());

            // 사진 촬영
            if (mIsTakePictureRequested) {
                mTemp = mRgba;
                mIsTakePictureRequested = false;
                runTakePictureTask();
            }
            return mRgba;
        }
    };

    @OnClick(R.id.img_camera_shot)
    public void cameraShot(){
        mIsTakePictureRequested = true;
    }

    private File mLastCapturedPhoto = null;

    @OnClick(R.id.img_camera_switch)
    public void switchCamera(){
        opencv_camera_view.switchCamera();

    }

    private void runTakePictureTask() {
        runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
               File tempPhoto = takePicture(mTemp);

                if (tempPhoto != null) {
                    mLastCapturedPhoto = tempPhoto;
                    findViewById(R.id.btnFaceSwapShowCapturedImage).setVisibility(View.VISIBLE);
                }

                findViewById(R.id.imgFaceSwapCameraShootMask).setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        findViewById(R.id.imgFaceSwapCameraShootMask).setVisibility(View.GONE);
                    }
                }, 100);
            }
        });
    }


    public File takePicture(Mat cameraFrame) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(cameraFrame.cols(), cameraFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cameraFrame, bitmap);
            if (bitmap != null) {
                String s = ImageUtil.saveBitmap(bitmap);
                bitmap.recycle();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 이미지 필터 카메라로 전환한다.
     */
    @OnClick(R.id.img_camera_filter)
    public void changeCameraToImageFilter(){
        Intent imageFilter = new Intent(getContext(), CameraFilterActivity.class);
        startActivity(imageFilter);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * 얼굴 인식 카메라로 전환한다.
     */
    @OnClick(R.id.img_face_filter_camera)
    public void changeCameraToFaceDetect(){
        Intent faceDetect = new Intent(getContext(), CameraFaceDetectActivity.class);
        startActivity(faceDetect);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
