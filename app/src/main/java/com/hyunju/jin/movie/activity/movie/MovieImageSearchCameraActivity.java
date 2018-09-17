package com.hyunju.jin.movie.activity.movie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.nofolder.GalleryType2Activity;
import com.hyunju.jin.movie.camera.FaceTrackingCameraView;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.view.CameraGLSurfaceView;
import org.wysaid.view.CameraRecordGLSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 촬영한 이미지로 영화를 검색하는 화면. 찍은 사진을 서버에 보내면 서버에서 어떤 영화인지 추론한다.
 * 왼쪽 하단에서 갤러리를 열어 사진을 선택하면, 그 사진으로 영화를 검색할 수 있다.
 *
 * 현재 사용하지 않음. 카메라로 찍은 사진에 대한 인식률이 매우 낮거나 부정확함.
 * 다양한 학습데이터가 부족한 것이 원인일듯.
 *
 * inception_v3
 *
 * 구지 openCV 카메라를 사용할 필요가 없었는데..
 */
public class MovieImageSearchCameraActivity extends SuperActivity {

    static {
        if (!OpenCVLoader.initDebug()) {    // OpenCV 라이브러리 로드, 실패시 false
            Log.e("FaceFilterCamera", "OpenCV 라이브러리 로드 실패");
        }
    }

    public static final int REQ_GALLERY = 2;    // 갤러리 사진 선택 작업 요청 코드
    private final int PM_CAMERA = 1000; // 사용자 카메라 사용권한 요청코드
    @BindView(R.id.camera_view) FaceTrackingCameraView camera_view; // 이미지 필터 카메라 뷰
    @BindView(R.id.img_camera_switch) ImageView img_camera_switch;    // 카메라 방향전환 버튼

    // 카메라가 화면 가득 차지하도록 폰 사이즈를 구해 저장할 변수.
    int cameraWidth, cameraHeight;

    MovieService movieService;
    Movie photoToMovie;

    // 카메라와 관련된 변수
    private Mat mRgba, mGray, mTemp;
    private boolean mIsTakePictureRequested= false; // 카메라 촬영 시 true가 되고 그 시점의 화면을 촬영한다.
    private File mLastCapturedPhoto = null; // 촬영한 사진

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 카메라 사용중이므로 화면 포커스를? 잃지 않기 위해 설정
        setContentView(R.layout.activity_movie_image_search_camera);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){
        movieService = RetrofitClient.getMovieService();

        // 카메라 뷰가 화면에 가득차도록 하기 위해서 폰의 화면 사이즈를 구한다.
        Display display = getWindowManager().getDefaultDisplay();
        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1 ){ // SDK 13 부터
            Point size = new Point();
            display.getSize(size);
            cameraWidth = size.x;
            cameraHeight = size.y;
        }else{
            cameraWidth = display.getWidth();
            cameraHeight = display.getHeight();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 사용자 앨범에 접근할 수 있는 권한을 얻는다.
        String[] perms = {Manifest.permission.CAMERA};
        if ( !EasyPermissions.hasPermissions(this, perms)) {    // 권한이 없다면
            EasyPermissions.requestPermissions(this, "권한이 필요합니다.", PM_CAMERA, perms);
        }else {
            resumeCamera();   // 카메라를 활성화 한다.
        }
    }

    /**
     * 필터 카메라 활성화
     */
    private void resumeCamera(){

        if(!OpenCVLoader.initDebug()){
            Log.e("OpenCVFaceDetect", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        }else {
            Log.e("OpenCVFaceDetect", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.e("FaceSwapper", "OpenCV loaded successfully");
                camera_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {   // 카메라 화면을 누르면

                    }
                });
            }else{
                super.onManagerConnected(status);
            }
            camera_view.setCvCameraViewListener(mCameraViewDefaultListener);
            camera_view.enableView();
        }
    };

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

           /* if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }
*/
            // 사진 촬영
            if (mIsTakePictureRequested) {
                mTemp = mRgba;
                mIsTakePictureRequested = false;
                runTakePictureTask();
            }

            return mRgba;
        }
    };

    private void runTakePictureTask() {
        runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                File tempPhoto = takePicture(mTemp);

                if (tempPhoto != null) {
                    mLastCapturedPhoto = tempPhoto;
                }
            }
        });
    }


    public File takePicture(Mat cameraFrame) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(cameraFrame.cols(), cameraFrame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(cameraFrame, bitmap);
            if (bitmap != null) {
                String imagePath = ImageUtil.saveBitmap(bitmap);
                bitmap.recycle();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + imagePath)));

                Intent search = new Intent(getContext(), MovieImageSearchActivity.class);
                search.putExtra(MovieImageSearchActivity.DATA_KEY_IMAGE_PATH, imagePath);
                startActivity(search);

                /*File imageFile = new File(imagePath);

                HashMap<String, RequestBody> data_movieImageSearch = new HashMap<String, RequestBody>();
                ArrayList<MultipartBody.Part> file_movieImageSearch = new ArrayList<MultipartBody.Part>(); // 서버에 전송할 파일을 담는 리스트
                if(imageFile.exists()){
                    RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
                    file_movieImageSearch.add(MultipartBody.Part.createFormData("movieImage", imageFile.getName(), reqFile));
                }else{
                    Log.e(TAG, imageFile.getPath()+" 파일이 존재하지 않습니다.");
                }
                Call<Movie> call_movieImageSearch = movieService.getMovieByImageSearch("movieImageSearch", data_movieImageSearch, file_movieImageSearch);
                call_movieImageSearch.enqueue(new Callback<Movie>() {
                    @Override
                    public void onResponse(Call<Movie> call, Response<Movie> response) {
                        if(response.isSuccessful()){
                            photoToMovie = response.body();
                            if(photoToMovie.getMovieCode() != 0){
                                Toast.makeText(getContext(), photoToMovie.getMovieTitle(), Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        Log.e(TAG, "영화 이미지 검색");
                        Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        //camera_view.onResume();
                    }

                    @Override
                    public void onFailure(Call<Movie> call, Throwable t) {
                        Log.e(TAG, "영화 이미지 검색 (onFailure)");
                        Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        //camera_view.onResume();
                    }
                });
*/
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 카메라 촬영 버튼 클릭 시, 촬영한 사진을 서버에 보내 어떤 영화인지 판별하도록 한다.
     */
    @OnClick(R.id.img_camera_shot)
    public void cameraShot(){
        mIsTakePictureRequested = true;
    }

    @OnClick(R.id.img_face_filter_camera)
    public void openGallery(){
        Intent gallery = new Intent(getContext(), GalleryType2Activity.class);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT_MAX, 1);
        gallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, true);
        gallery.putExtra(GalleryFunction.OPTION_FILTER, false);
        startActivityForResult(gallery, REQ_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_GALLERY:
                if (resultCode == RESULT_OK) {
                    ArrayList<Gallery> selectedProfileImages = (ArrayList<Gallery>) data.getSerializableExtra(GalleryType2Activity.DATA_KEY_SELECT_LIST);
                    // 이미지 검색작업이므로 리스트 size 가 항상 1로 넘어온다.
                    String imagePath = selectedProfileImages.get(0).getMedia().get(GalleryFunction.KEY_PATH);
                    Intent search = new Intent(getContext(), MovieImageSearchActivity.class);
                    search.putExtra(MovieImageSearchActivity.DATA_KEY_IMAGE_PATH, imagePath);
                    startActivity(search);
                }
                break;
        }
    }

/**
     * 촬영한 Bitmap 형태의 사진을 jpeg 형태의 파일로 변환한다.
     * 촬영한 사진을 서버에 전송하기 위해 사용한다.
     * @param bitmap
     * @return
     */
    public File createFileBitmapToJpeg(Bitmap bitmap){

        File storage = getContext().getCacheDir(); // 이 부분이 임시파일 저장 경로
        long currentTime = System.currentTimeMillis();
        String fileName = currentTime + ".jpg";
        File tempFile = new File(storage, fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.close(); // 마무리로 닫아줍니다.

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "createFileBitmapToJpeg() FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "createFileBitmapToJpeg() IOException");
        }

        return tempFile;   // 임시파일 리턴
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case PM_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    resumeCamera();
                }else {
                    Toast.makeText( getContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera_view != null) {
            camera_view.disableView(); // 카메라 종료
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera_view != null) {
            camera_view.disableView(); // 카메라 종료
        }
    }
}
