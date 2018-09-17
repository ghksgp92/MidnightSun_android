/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyunju.jin.movie.activity.tensorflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.tensorflow.env.BorderedText;
import com.hyunju.jin.movie.activity.tensorflow.env.ImageUtils;
import com.hyunju.jin.movie.activity.tensorflow.env.Logger;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 텐서플로우 Inception V3 을 사용하여 학습한 영화 포스터를 인식하는 카메라 액티비티.
 */
public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener, LoadingListener {

    private static final Logger LOGGER = new Logger();

    private Classifier classifier;  // 텐서플로우 인식 인터페이스
    // 인터페이스를 생성하기 위해 들어갈 설정 값. 학습 파일을 생성할 때 사용한 옵션과 동일해야한다. Inception V3 의 설정과 맞춰줘야 제대로 실행된다.
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";
    private static final String MODEL_FILE = "file:///android_asset/graph.pb";  // 학습된 그래프 파일 위치
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";  // 학습된 labels 파일 위치

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final boolean MAINTAIN_ASPECT = true;
    //private static Size DESIRED_PREVIEW_SIZE; // 기기 사이즈에 따라 사이즈를 설정하기 위해 final 한정자 제거
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);  // 이게 카메라 사이즈인거 같긴한데.. 전체화면으로 설정을 못하네.

    private Integer sensorOrientation;

    private int previewWidth = 0;
    private int previewHeight = 0;
    private byte[][] yuvBytes;
    private int[] rgbBytes = null;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;  // 텐서플로우가 추론을 수행할 이미지
    private Bitmap cropCopyBitmap;

    private boolean computing = false;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    // getLayoutId() 에서 사용하는 레이아웃에 정의된 View
    //private ResultsView resultsView;
    private BorderedText borderedText;

    private long lastProcessingTimeMs;

    private boolean isSuccessMovieSearch;   // 영화를 인식해서 찾기에 성공했다면 true

    public Context getContext(){ return this; }

    /**
    * 실질적인 레이아웃이 설정됨.
    * @return
    */
    @Override
    protected int getLayoutId() {
        isSuccessMovieSearch = false;
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
      Log.e("텐서플로우", "getDesiredPreviewFrameSize");

      /*if(DESIRED_PREVIEW_SIZE == null){

      int displayWidth = 0;
      int displayHeight = 0;

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
      DESIRED_PREVIEW_SIZE = new Size(displayWidth, displayHeight);
    }
    */
    return DESIRED_PREVIEW_SIZE;
    }

  private static final float TEXT_SIZE_DIP = 10;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {

      Log.e("텐서플로우", "onPreviewSizeChosen");

    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    classifier =
        TensorFlowImageClassifier.create(
            getAssets(),
            MODEL_FILE,
            LABEL_FILE,
            INPUT_SIZE,
            IMAGE_MEAN,
            IMAGE_STD,
            INPUT_NAME,
            OUTPUT_NAME);

    // View 는 getLayoutId() 에서 사용하는 레이아웃에 정의되어있다.
    //resultsView = (ResultsView) findViewById(R.id.results);
    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    // 센서를 왜 ?
    final Display display = getWindowManager().getDefaultDisplay();
    final int screenOrientation = display.getRotation();
    LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);
    sensorOrientation = rotation + screenOrientation;

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbBytes = new int[previewWidth * previewHeight];
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            INPUT_SIZE, INPUT_SIZE,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    yuvBytes = new byte[3][];

    /* addCallback(
        new DrawCallback() {    // OverlayView
          @Override
          public void drawCallback(final Canvas canvas) {
              Log.e("호출", "renderDebug");
            //renderDebug(canvas);    // 왜 이런식으로 호출해야하지? 컨버스 가져올려고?
          }
        });*/
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {

        Image image = null;

        if( isSuccessMovieSearch){
          return;  // true 일 경우 이미지 표시를 멈춘다.
        }

        try {
          image = reader.acquireLatestImage();
          if (image == null) {
            return;
          }

          if (computing) {
              image.close();
              return;
          }

          computing = true;

          Trace.beginSection("imageAvailable");

          final Plane[] planes = image.getPlanes();
          fillBytes(planes, yuvBytes);

          final int yRowStride = planes[0].getRowStride();
          final int uvRowStride = planes[1].getRowStride();
          final int uvPixelStride = planes[1].getPixelStride();
          ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0], yuvBytes[1], yuvBytes[2], previewWidth, previewHeight
                  , yRowStride, uvRowStride, uvPixelStride, rgbBytes);

        image.close();

        }catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            LOGGER.e(e, "Exception!");
            Trace.endSection();
            return;
        }

        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
          ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
            final long startTime = SystemClock.uptimeMillis();
            final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

            for(int i=0; i<results.size(); i++){
                Classifier.Recognition recog = results.get(i);
                Log.e("runInBackground", "("+(i+1)+") 결과: "+recog.getTitle()+" , "+recog.getConfidence());
            }

            Classifier.Recognition recog = results.get(0);
            if( !isSuccessMovieSearch && recog.getConfidence() > CONFIDENCE_LEVEL){
                isSuccessMovieSearch = true;
                requestMovieInfo(results.get(0).getTitle());

            }else{
                isSuccessMovieSearch = false;
            }
            requestRender();
            computing = false;
        }
    });
    Trace.endSection();
    }

    @Override
    public void onSetDebug(boolean debug) {
        classifier.enableStatLogging(debug);
    }

    private void requestMovieInfo(String movieCode){

        showLoading();

        Movie movie = new Movie();
        movie.setMovieCode( Integer.parseInt(movieCode) );  // 라벨은 문자열. 영화코드를 나타내도록 했으므로 정수로 변환후 영화코드로 설정한다.

        HashMap<String, String> data_getMovie = new HashMap<String, String>();
        data_getMovie.put("movieCode", movie.getMovieCode()+"");
        Call<Movie> request_getMovie = movieService.getMovie("getMovie", data_getMovie);
        request_getMovie.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if(response.isSuccessful()){
                    Movie result = response.body();
                    tv_search_result.setText(result.getMovieTitle());
                    hideLoading();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {

            }
        });
    }

    @BindView(R.id.tv_search_before_msg) TextView tv_search_before_msg;         // 이미지 인식 전 보여줄 메세지
    @BindView(R.id.layout_searching) RelativeLayout layout_searching;           // 이미지 인식중임을 나타내는 레이아웃
    @BindView(R.id.indicator_image_search) AVLoadingIndicatorView indicator_image_search;   // 이미지 인식중임을 나타내는 indicator
    @BindView(R.id.layout_search_result) RelativeLayout layout_search_result;   // 이미지 인식 결과 (영화정보) 레이아웃
    @BindView(R.id.tv_search_result) TextView tv_search_result;

    MovieService movieService;

    final Float CONFIDENCE_LEVEL = 0.88f;
    Boolean showLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        tv_search_before_msg.setVisibility(View.VISIBLE);
        layout_searching.setVisibility(View.GONE);
        layout_search_result.setVisibility(View.GONE);

        movieService = RetrofitClient.getMovieService();
    }


    @Override
    public void showLoading() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading = true;
                tv_search_before_msg.setVisibility(View.GONE);
                layout_searching.setVisibility(View.VISIBLE);
                indicator_image_search.smoothToShow();
                layout_search_result.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void hideLoading() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading = false;
                tv_search_before_msg.setVisibility(View.GONE);
                layout_searching.setVisibility(View.GONE);
                indicator_image_search.smoothToHide();
                layout_search_result.setVisibility(View.VISIBLE);
            }
        });

    }
}
