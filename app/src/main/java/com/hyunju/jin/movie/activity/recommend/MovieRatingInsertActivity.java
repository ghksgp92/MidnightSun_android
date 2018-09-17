package com.hyunju.jin.movie.activity.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.movie.UserMainActivity;
import com.hyunju.jin.movie.adapter.RecyclerViewPaginationListener;
import com.hyunju.jin.movie.adapter.recommend.MovieRatingAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.Pagination;
import com.hyunju.jin.movie.network.RecommendService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 한 화면에서 여러개의 영화 평가를 등록할 수 있도록 해주는 액티비티.
 * 이 화면을 만든 목적은 사용자가 빠르게 영화 평가 정보를 늘려서 추천영화를 더 정확하게 받게 하기 위해서다.
 * 영화 목록은 사용자의 선호장르 정보를 기반으로 만들어진다.
 *
 * (수정)
 * 액티비티 이름 좀 더 명시적으로 바꾸면 좋겠어.
 */
public class MovieRatingInsertActivity extends SuperActivity implements MovieRatingInsertListener, LoadingListener{

    public static final String KEY_CALL_ACTIVITY = "callActivityName";  // 이 액티비티를 호출한 액티비티 이름을 꺼내기 위한 Key
    public static final int DATA_CALL_ACTIVITY_SIGN_UP = 1;   // 이전 화면이 회원가입인 경우
    public static final String KEY_TASTE_GENRE = "tasteGenres";         // 추가 정보 입력 화면에서 선택한 선호 장르를 꺼내기 위한 Key
    int callActivity;

    // 에러/예외 발생 시 메시지를 보여주는 View 참조
    @BindView(R.id.layout_error) LinearLayout layout_error;
    @BindView(R.id.tv_error_msg) TextView tv_error_msg;
    @BindView(R.id.tv_error_msg_detail) TextView tv_error_msg_detail;
    @BindView(R.id.btn_retry) Button btn_retry;

    // 평점 수, 완료 버튼 등이 있는 상단메뉴의 View 참조
    @BindView(R.id.tv_skip) TextView tv_skip;                   // [나중에 하기] 버튼
    @BindView(R.id.tv_rating_count_msg) TextView tv_rating_count_msg;   // 평점 텍스트 View
    @BindView(R.id.tv_rating_count) TextView tv_rating_count;   // 사용자가 평점을 입력한 수를 나타낸다.
    @BindView(R.id.tv_complete) TextView tv_complete;           // [완료] 버튼

    @BindView(R.id.recycler_movies) RecyclerView recycler_movies; // 화면에 보이는 영화 목록
    MovieRatingAdapter movieRatingAdapter;

    @BindView(R.id.layout_top_menu) RelativeLayout layout_top_menu; // 상단메뉴
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator; // 로딩 프로그래스바

    private boolean isLoading = false; // 다음 페이지가 로딩중이라면, true
    private boolean isLastPage = false; // 현재 페이지가 마지막 페이지라면, true

    Pagination pagination;

    RecommendService recommendService; // 영화 목록을 불러오고 평점 정보를 저장하기 위한 서버 통신 객체

    HashMap<Integer, Movie> ratingMovies; // 사용자가 평점을 입력한 영화 목록을 관리다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_rating_insert);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){

        callActivity = getIntent().getIntExtra(KEY_CALL_ACTIVITY, 0);   // 호출한 액티비티 이름 저장

        // 서버 HTTP 요청을 처리하는 객체 생성
        recommendService = RetrofitClient.getRecommendService();

        // 사용자가 평점을 입력한 영화를 저장하는 HashMap 생성
        ratingMovies = new HashMap<Integer, Movie>();

        // 영화 목록을 보여주는 RecyclerView 생성
        movieRatingAdapter = new MovieRatingAdapter(getContext(), this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recycler_movies.setLayoutManager(linearLayoutManager);
        recycler_movies.setAdapter(movieRatingAdapter);
        recycler_movies.setItemAnimator(new DefaultItemAnimator());

        // 영화 목록 페이징 처리
        // 1) 페이징 정보를 관리하는 객체 생성 (반드시 먼저!)
        pagination = new Pagination();

        // 2) RecyclerView 에 스크롤 리스너 등록
        // RecyclerViewPaginationListener 는 스크롤 끝까지 내리면 다음 페이지를 로드한다.
        recycler_movies.addOnScrollListener(new RecyclerViewPaginationListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true; // 다음 페이지가 로딩중임을 표시한다.
                pagination.setNextPage();   // 현재 페이지 + 1 하여 다음 페이지 번호로 바꾼다.
                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return pagination.getTotalPages();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        switch (callActivity){
            case DATA_CALL_ACTIVITY_SIGN_UP:
                tv_skip.setVisibility(View.VISIBLE);
                break;
                default:
                    tv_skip.setVisibility(View.INVISIBLE);
                    break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoading();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPage();
            }
        }, 200);
    }

    /**
     * 영화 목록 첫페이지 로드
     * 첫페이지 로드 시 전체 페이지 수를 함께 리턴받는다.
     */
    private void loadFirstPage() {

        // 서버에서 영화 정보 가져오기
        HashMap<String, String> data_movieRatingListGET = new HashMap<String, String>();
        data_movieRatingListGET.put("userCode", loginUser.getUserCode()+"");
        data_movieRatingListGET.put("pagination", gson.toJson(pagination));

        Call<ArrayList<Movie>> call_movieRatingListGET = recommendService.getMovieList("movieRatingListGET", data_movieRatingListGET);
        call_movieRatingListGET.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()) {

                    hideLoading();

                    movieRatingAdapter.clear();  // 첫페이지므로 기존에 있던 리스트 내용을 삭제
                    // 어댑터에 리턴된 데이터 추가
                    movieRatingAdapter.addAll(response.body());
                    if (movieRatingAdapter.getItemCount() > 0) {
                        // 첫 페이지 로드시, 추천 영화가 리턴된다면 첫번째 영화의 Pagination 에 전체 페이지 수가 담겨서 리턴된다.
                        pagination.setTotalPages(movieRatingAdapter.getItem(0).getPagination().getTotalPages());

                        if (pagination.checkNextPage()) {
                            movieRatingAdapter.addLoadingFooter();
                        } else {
                            isLastPage = true;
                        }

                        recycler_movies.setVisibility(View.VISIBLE);        // 영화 목록 보여주고
                        if(callActivity == DATA_CALL_ACTIVITY_SIGN_UP){     // 가입 직후 평점등록으로 넘어온 경우라면
                            tv_skip.setVisibility(View.VISIBLE);            // [나중에하기] 버튼 보여준다.
                        }
                        // 상단 메뉴에 있는 나머지 View 를 보여준다.
                        tv_rating_count_msg.setVisibility(View.VISIBLE);
                        tv_rating_count.setVisibility(View.VISIBLE);
                        tv_complete.setVisibility(View.VISIBLE);

                    }else{
                        // 첫 페이지 로드 시 추천영화가 로드되지 않는 경우
                        // 사용자가 추천 영화를 만들기 위해 필요한 최소한의 평점 데이터를 입력하지 않은 경우일 수 있다.
                        // 나중엔 다양한 이유가 생길 수 있지만 우선은 저 위의 경우만 있다고 생각하고 무조건 아래와 같이 메시지를 나타낸다.
                        hideLoading();
                        tv_error_msg.setText("더이상 평가할 영화가 없어요.");
                        tv_error_msg_detail.setText("새로운 영화가 나올때까지 기다려주세요!");
                        layout_error.setVisibility(View.VISIBLE);
                        if(callActivity == DATA_CALL_ACTIVITY_SIGN_UP){     // 가입 직후 평점등록으로 넘어온 경우라면
                            tv_skip.setVisibility(View.VISIBLE);            // [나중에하기] 버튼 보여준다.
                        }
                    }

                    return;
                }
                // 서버 응답에 실패한 경우
                hideLoading();
                Log.e(TAG, "평가할 영화목록 첫페이지 로드 실패 (onFailure)");
                layout_error.setVisibility(View.VISIBLE);
                if(callActivity == DATA_CALL_ACTIVITY_SIGN_UP){     // 가입 직후 평점등록으로 넘어온 경우라면
                    tv_skip.setVisibility(View.VISIBLE);            // [나중에하기] 버튼 보여준다.
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "평가할 영화목록 첫페이지 로드 실패 (onFailure)");
                t.printStackTrace();
                layout_error.setVisibility(View.VISIBLE);
                if(callActivity == DATA_CALL_ACTIVITY_SIGN_UP){     // 가입 직후 평점등록으로 넘어온 경우라면
                    tv_skip.setVisibility(View.VISIBLE);            // [나중에하기] 버튼 보여준다.
                }
            }
        });

    }


    /**
     * 영화 목록의 다음 페이지를 로드한다.
     */
    private void loadNextPage() {
        Log.e(TAG, "loadNextPage: " + pagination.getCurrentPage());

        // 서버에서 영화 정보 가져오기
        HashMap<String, String> data_movieRatingListGET = new HashMap<String, String>();
        data_movieRatingListGET.put("userCode", loginUser.getUserCode()+"");
        data_movieRatingListGET.put("pagination", gson.toJson(pagination));

        Log.e(TAG, gson.toJson(pagination));
        Call<ArrayList<Movie>> call_movieRatingListGET = recommendService.getMovieList("movieRatingListGET", data_movieRatingListGET);
        call_movieRatingListGET.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    movieRatingAdapter.removeLoadingFooter();
                    isLoading = false;

                    movieRatingAdapter.addAll(response.body());

                    if (pagination.checkNextPage()){
                        movieRatingAdapter.addLoadingFooter();
                    } else {
                        isLastPage = true;
                    }
                    return;
                }

                // 서버 응답에 실패한 경우
                Log.e(TAG, "추천 영화목록 페이지 로드 실패 (onFailure)");
                Toast.makeText(getContext(), "서버에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                isLastPage = true; // 더이상 페이지 로드를 못하도록 마지막 페이지라고 설정한다.
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                Log.e(TAG, "추천 영화목록 페이지 로드 실패 (onFailure)");
                t.printStackTrace();
                Toast.makeText(getContext(), "서버에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                isLastPage = true; // 더이상 페이지 로드를 못하도록 마지막 페이지라고 설정한다.
            }
        });
    }

    @OnClick(R.id.btn_retry)
    public void retry(){
        layout_error.setVisibility(View.GONE);
        showLoading();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPage();
            }
        },1000);
    }

    /**
     * [나중에 하기]를 클릭한 경우를 처리하는 메서드
     * 이 버튼은 이전 화면이 회원가입이었을 경우에만 나타난다.
     */
    @OnClick(R.id.tv_skip)
    public void skipEnterMovieTasteBasicInfo(){

        switch (callActivity){
            case DATA_CALL_ACTIVITY_SIGN_UP:
                Intent intent = new Intent(getContext(), UserMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
        }

        finish();
    }

    /**
     * MovieRatingInsertListener 인터페이스 구현
     * 영화의 평점이 바뀌는 경우 '평점을 입력한 영화 목록'을 업데이트 한다.
     * @param movie
     */
    @Override
    public void onRatingChanged(Movie movie) {
        if(movie.getRatingValue() > 0.0f) { // 평점이 0 이상일 경우엔 리스트에 추가
            ratingMovies.put(movie.getMovieCode(), movie);
        }else{
            ratingMovies.remove(movie.getMovieCode());  // 평점이 0 이 되면 리스트에서 제거
        }
        tv_rating_count.setText(ratingMovies.size()+""); // 평점을 입력한 영화 수를 업데이트
        movieRatingAdapter.notifyDataSetChanged();
    }

    /**
     * 사용자가 매긴 영화 평점을 서버 DB 에 저장한다.
     */
    @OnClick(R.id.tv_complete)
    public void movieRatingSave(){
        if(ratingMovies.size() > 0){

            HashMap<String, String> data_movieRatingUPDATE = new HashMap<String, String>();
            data_movieRatingUPDATE.put("userCode", loginUser.getUserCode()+"");
            // HashMap 형태의 데이터를 ArrayList 로 변경한다.
            ArrayList<Movie> movies = new ArrayList<Movie>(ratingMovies.values());
            // ArrayList 형태로 서버에 전송한다.
            data_movieRatingUPDATE.put("ratingMovies", gson.toJson(movies));

            Call<ResponseData> call_movieRatingUPDATE = recommendService.post("movieRatingUPDATE", data_movieRatingUPDATE);
            call_movieRatingUPDATE.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                    if(response.isSuccessful()){
                        finish();
                        return;
                    }

                    Log.e(TAG, "평점 저장 실패");
                }

                @Override
                public void onFailure(Call<ResponseData> call, Throwable t) {
                    Log.e(TAG, "평점 저장 실패 (onFailure)");
                }
            });

        }
    }

    /**
     * 화면이 처음 로딩될 때, (= 영화 목록이 처음 로딩될때) 호출된다.
     */
    @Override
    public void showLoading() {
        // 로딩이 완료될때까지 감춰야할 View 들 처리
        // 상단에 있는 버튼들 감춤. 부모가 RelativeLayout 이므로 invisible 로 설정한다.
        tv_skip.setVisibility(View.INVISIBLE);
        tv_rating_count_msg.setVisibility(View.INVISIBLE);
        tv_rating_count.setVisibility(View.INVISIBLE);
        tv_complete.setVisibility(View.INVISIBLE);

        // 영화 목록과 에러페이지 감춤
        recycler_movies.setVisibility(View.GONE);
        layout_error.setVisibility(View.GONE);

        // 프로그래스바를 보여준다.
        loading_indicator.smoothToShow();
    }

    @Override
    public void hideLoading() {
        loading_indicator.smoothToHide();
    }
}
