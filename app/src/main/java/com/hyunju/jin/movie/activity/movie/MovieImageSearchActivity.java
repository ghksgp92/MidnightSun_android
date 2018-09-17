package com.hyunju.jin.movie.activity.movie;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 영화 이미지 검색 결과 화면
 * 검색 결과가 팝업창으로 나타난다.
 */
public class MovieImageSearchActivity extends SuperActivity implements LoadingListener{

    public static final String DATA_KEY_IMAGE_PATH = "imagePath";   // 사용자가 선택한 이미지 경로를 저장하고 꺼낼때 사용하는 key 값
    String imagePath;   // 사용자가 선택한 이미지 경로

    @BindView(R.id.img_search) ImageView img_search;    // 사용자가 선택한 이미지를 보여주는 View
    @BindView(R.id.indicator_image_search) AVLoadingIndicatorView indicator_image_search;   // 이미지 인식중임을 알리는 indicator
    @BindView(R.id.layout_searching) RelativeLayout layout_searching;   //  영화를 인식중임을 나타내는 레이아웃

    @BindView(R.id.card_view_img_search_result) CardView card_view_img_search_result;   // 이미지 검색 결과를 보여주는 뷰
    @BindView(R.id.tv_movie_title) TextView tv_movie_title; // 검색된 영화 제목
    @BindView(R.id.btn_detail_movie) Button btn_detail_movie;   // 검색 결과에서 [상세정보] 버튼. 분홍색 버튼이다. 결과가 없을 경우엔 버튼이 보이지 않는다.

    @BindView(R.id.tv_searching_msg) TextView tv_searching_msg;

    MovieService movieService;
    Movie photoToMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_image_search);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            imagePath = bundle.getString(DATA_KEY_IMAGE_PATH, "");
        }
        if(StringUtils.isEmpty(imagePath)){
            Toast.makeText(getContext(), "사진을 선택해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }

        initialize();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoading();
                requestImageSearch();
            }
        },1000);

    }

    /**
     * 이미지 검색을 하기 위해 필요한 변수와 객체 초기화 및 화면 준비
     */
    private void initialize(){
        // 서버에 이미지 검색을 요청하기 위한 HTTP 통신 객체를 얻는다.
        movieService = RetrofitClient.getMovieService();

        // 사용자가 선택한 이미지를 보여준다.
        Glide.with(getActivity()).load(new File(imagePath)).into(img_search);
    }

    /**
     * 서버에 이미지 검색을 요청한다.
     */
    private void requestImageSearch(){
        File imageFile = new File(imagePath);
        HashMap<String, RequestBody> data_movieImageSearch = new HashMap<String, RequestBody>();
        ArrayList<MultipartBody.Part> file_movieImageSearch = new ArrayList<MultipartBody.Part>(); // 서버에 전송할 파일을 담는 리스트
        if(imageFile.exists()){
            RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
            file_movieImageSearch.add(MultipartBody.Part.createFormData("movieImage", imageFile.getName(), reqFile));
        }else{
            // 검색할 사진이 없는 경우
            Log.e(TAG, imageFile.getPath()+" 파일이 존재하지 않습니다.");
            Toast.makeText(getContext(),"사진을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish(); // 액티비티를 종료한다.
        }

        Call<Movie> call_movieImageSearch = movieService.getMovieByImageSearch("movieImageSearch", data_movieImageSearch, file_movieImageSearch);
        call_movieImageSearch.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if(response.isSuccessful()){
                    photoToMovie = response.body();
                    if(photoToMovie.getMovieCode() > 0){   // 영화 코드가 양수일 경우, 영화 검색에 성공한 것임.
                        hideLoading();  // 영화 검색중임을 나타내는 로딩바를 감춘다.
                        layout_searching.setVisibility(View.GONE);      // 영화 검색중 레이아웃을 감춘다. (화면이 살짝 어두워지는 레이아웃)
                        createMovieSearchResultDialog(photoToMovie);
                        return;
                    }
                }

                hideLoading();
                layout_searching.setVisibility(View.GONE);      // 영화 검색중 레이아웃을 감춘다.
                createMovieSearchResultDialog(null);
                Log.e(TAG, "영화 이미지 검색");
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                hideLoading();
                layout_searching.setVisibility(View.GONE);      // 영화 검색중 레이아웃을 감춘다.
                createMovieSearchResultDialog(null);
                Log.e(TAG, "영화 이미지 검색 (onFailure)");
            }
        });
    }

    /**
     * 영화 이미지 검색 결과를 보여주는 다이얼로그를 띄운다.
     * 검색 결과에 해당하는 영화 정보를 매개변수로 받는다.
     */
    private void createMovieSearchResultDialog(final Movie movie){

        if(movie == null){  // 검색결과가 없는 경우

            card_view_img_search_result.animate().translationY(card_view_img_search_result.getHeight()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Log.e(TAG, "onAnimationStart");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.e(TAG, "onAnimationEnd");
                    // 검색 결과를 화면에 표시한다.
                    tv_movie_title.setText("검색 결과가 없습니다.");
                    btn_detail_movie.setVisibility(View.GONE);  // [상세정보] 버튼을 감춘다.
                    card_view_img_search_result.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        }else{  // 검색 결과가 있는 경우

            card_view_img_search_result.animate().translationY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    Log.e(TAG, "onAnimationStart");

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.e(TAG, "onAnimationEnd");
                    // 검색 결과를 화면에 표시한다.
                    tv_movie_title.setText(movie.getMovieTitle());  // 영화 제목
                    btn_detail_movie.setVisibility(View.VISIBLE);
                    card_view_img_search_result.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }


    }


    @Override
    public void showLoading() {
        indicator_image_search.smoothToShow();
        layout_searching.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        indicator_image_search.smoothToHide();
    }

    @OnClick(R.id.btn_close)
    public void back(){
        finish();
        // 다시 앨범열도록 하기.
    }


    @OnClick(R.id.btn_detail_movie)
    public void showMovieDetail(){
        Intent intent = new Intent(getContext(), MovieDetailActivity.class);
        intent.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, photoToMovie);
        startActivity(intent);  // 선택한 영화의 상세정보 화면으로 이동한다.
        finish();
    }

}
