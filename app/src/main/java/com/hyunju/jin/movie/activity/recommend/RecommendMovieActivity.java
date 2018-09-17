package com.hyunju.jin.movie.activity.recommend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.BottomMenuActivity;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.adapter.RecyclerViewPaginationListener;
import com.hyunju.jin.movie.adapter.recommend.RecommendMovieAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.Pagination;
import com.hyunju.jin.movie.network.MovieService;
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
 * 추천 영화 목록을 보여주는 액티비티.
 * [평점추가] 버튼을 눌러 영화 평점을 입력하는 화면으로 이동할 수 있다.
 *
 * 생명주기 좀....
 */
public class RecommendMovieActivity extends BottomMenuActivity implements LoadingListener, RecommendMovieListener{

    // 화면 하단 메뉴 View
    @BindView(R.id.img_bottom_menu_recommend) ImageView img_bottom_menu_recommend;
    @BindView(R.id.tv_bottom_menu_label_recommend) TextView tv_bottom_menu_label_recommend;

    // 에러/예외 발생 시 메시지를 보여주는 레이아웃
    @BindView(R.id.layout_error) LinearLayout layout_error;
    @BindView(R.id.tv_error_msg) TextView tv_error_msg;
    @BindView(R.id.tv_error_msg_detail) TextView tv_error_msg_detail;
    @BindView(R.id.btn_retry) Button btn_retry;

    // 로딩 중임을 나타내는 View
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator;

    @BindView(R.id.recycler_recommend_movies) RecyclerView recycler_recommend_movies;
    RecommendMovieAdapter recommendMovieAdapter;
    LinearLayoutManager linearLayoutManager;

    @BindView(R.id.tv_show_my_report) TextView tv_show_my_report;   // [취향보고서] 버튼

    // 페이징 관련 변수
    Pagination pagination;
    private boolean isLoading = false; // 다음 페이지를 로드중이라면, true
    private boolean isLastPage = false; // 현재 페이지가 마지막 페이지라면, true

    RecommendService recommendService;
    MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_movie);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize(){
        // 하단 메뉴에서 [RECOMMEND] 버튼을 활성화 함
        img_bottom_menu_recommend.setImageResource(R.drawable.ic_menu_recommend_on);
        tv_bottom_menu_label_recommend.setTextColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null));

        recommendService = RetrofitClient.getRecommendService();
        movieService = RetrofitClient.getMovieService();

        // 추천 영화 목록 RecyclerView 준비
        recommendMovieAdapter = new RecommendMovieAdapter(getContext(), this);
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recycler_recommend_movies.setLayoutManager(linearLayoutManager);
        recycler_recommend_movies.setAdapter(recommendMovieAdapter);
        //recycler_movies.setItemAnimator(new DefaultItemAnimator());
        recycler_recommend_movies.addOnScrollListener(new RecyclerViewPaginationListener(linearLayoutManager) {
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
        }, 500);
    }

    /**
     * 추천 영화 목록 첫페이지 로드
     * 첫페이지 로드 시 전체 페이지 수를 함께 리턴받는다.
     */
    private void loadFirstPage() {

        // 페이징 정보를 초기화해줘야 한다.
        pagination = new Pagination();
        pagination.setPageSize(50);

        HashMap<String, String> data_recommendMoviesByUser = new HashMap<>();
        data_recommendMoviesByUser.put("userCode", loginUser.getUserCode()+"");
        data_recommendMoviesByUser.put("pagination", gson.toJson(pagination));

        Call<ArrayList<Movie>> call_recommendMoviesByUser = recommendService.getMovieList("recommendMoviesByUser", data_recommendMoviesByUser);
        call_recommendMoviesByUser.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){

                    hideLoading();
                    recommendMovieAdapter.clear(); // 첫페이지므로 기존에 로드했던 페이지 삭제
                    // 어댑터에 리턴된 데이터 추가

                    ArrayList<Movie> list = response.body();
                    if ( list.size() > 0) {
                        // (주의) 첫번째 영화의 코드가 -1 인지 확인한다. 이 경우는 사용자가 매긴 영화 평점 데이터가 15개 이하여서 영화추천을 할 수 없는 경우다.
                        if(list.get(0).getMovieCode() == -1){
                            tv_error_msg.setText("추천 영화가 없어요.");

                            int currentRatedMovieCount = list.get(1).getMovieCode();    // 두번째 영화의 코드에는 사용자가 매긴 평점 수가 있다.
                            tv_error_msg_detail.setText("현재 "+currentRatedMovieCount+"개의 평점이 있습니다.\n영화 평점을 10개 이상 매겨주세요.");
                            layout_error.setVisibility(View.VISIBLE);
                            tv_show_my_report.setVisibility(View.INVISIBLE);
                            btn_retry.setVisibility(View.INVISIBLE);

                        }else{
                            recommendMovieAdapter.addAll(list); // 추천영화를 제대로 로드한 경우에만 어댑터에 리스트 추가.
                            // 첫 페이지 로드시, 추천 영화가 리턴된다면 첫번째 영화의 Pagination 에 전체 페이지 수가 담겨서 리턴된다.
                            pagination.setTotalPages(recommendMovieAdapter.getItem(0).getPagination().getTotalPages());
                            if (pagination.checkNextPage()){
                                recommendMovieAdapter.addLoadingFooter();
                            } else {
                                isLastPage = true;
                            }
                            recycler_recommend_movies.setVisibility(View.VISIBLE);
                            tv_show_my_report.setVisibility(View.VISIBLE);
                        }

                    }else{
                        // 첫 페이지 로드 시 추천영화가 로드되지 않는 경우
                        // 사용자가 추천 영화를 만들기 위해 필요한 최소한의 평점 데이터를 입력하지 않은 경우일 수 있다.
                        // 나중엔 다양한 이유가 생길 수 있지만 우선은 저 위의 경우만 있다고 생각하고 무조건 아래와 같이 메시지를 나타낸다.
                        hideLoading();
                        tv_error_msg.setText("추천 영화를 불러올 수 없어요.");
                        tv_error_msg_detail.setText("다시 시도해주세요.");
                        layout_error.setVisibility(View.VISIBLE);
                        btn_retry.setVisibility(View.VISIBLE);

                    }
                    return;
                }
                // 서버 응답에 실패한 경우
                hideLoading();
                Log.e(TAG, "추천 영화목록 첫페이지 로드 실패");
                tv_error_msg.setText("추천 영화를 불러올 수 없어요.");
                tv_error_msg_detail.setText("다시 시도해주세요.");
                layout_error.setVisibility(View.VISIBLE);
                btn_retry.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "추천 영화목록 첫페이지 로드 실패 (onFailure)");
                t.printStackTrace();
                tv_error_msg.setText("추천 영화를 불러올 수 없어요.");
                tv_error_msg_detail.setText("다시 시도해주세요.");
                layout_error.setVisibility(View.VISIBLE);
                btn_retry.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * 추천 영화 목록의 다음 페이지를 로드한다.
     */
    private void loadNextPage() {

        // 서버에서 추천 영화 다음 페이지 가져오기
        HashMap<String, String> data_recommendMoviesByUser = new HashMap<>();
        data_recommendMoviesByUser.put("userCode", loginUser.getUserCode()+"");
        data_recommendMoviesByUser.put("pagination", gson.toJson(pagination));

        Log.e(TAG, gson.toJson(pagination));
        Call<ArrayList<Movie>> call_recommendMoviesByUser = recommendService.getMovieList("recommendMoviesByUser", data_recommendMoviesByUser);
        call_recommendMoviesByUser.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    recommendMovieAdapter.removeLoadingFooter();
                    isLoading = false;
                    recommendMovieAdapter.addAll(response.body());

                    if (pagination.checkNextPage()){
                        recommendMovieAdapter.addLoadingFooter();
                    } else {
                        isLastPage = true;
                    }
                    return;
                }

                // 서버 응답에 실패한 경우
                Log.e(TAG, "추천 영화목록 페이지 로드 실패");
                //Toast.makeText(getContext(), "서버에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                isLastPage = true; // 더이상 페이지 로드를 못하도록 마지막 페이지라고 설정한다.
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                Log.e(TAG, "추천 영화목록 페이지 로드 실패 (onFailure)");
                t.printStackTrace();
                //Toast.makeText(getContext(), "서버에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                isLastPage = true; // 더이상 페이지 로드를 못하도록 마지막 페이지라고 설정한다.
            }
        });
    }

    /**
     * 오류 발생 시, [RETRY] 버튼을 누를 경우 호출되는 메소드.
     * 추천 영화 목록의 첫번째 페이지를 서버에 요청한다.
     */
    @OnClick(R.id.btn_retry)
    public void retry(){
        layout_error.setVisibility(View.GONE);
        showLoading();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstPage();
            }
        },1000); // 프로그래스바가 잠시동안 보여지게 하기 위해서 딜레이를 줌
    }


    /**
     * [평점추가] 버튼 클릭시 호출되는 메서드.
     * 영화 평점을 한번에 추가할 수 있는 화면으로 이동한다.
     */
    @OnClick(R.id.btn_add_movie_rating)
    public void addMovieRating(){
        Intent intent = new Intent(getContext(), MovieRatingInsertActivity.class);
        startActivity(intent);
    }

    /**
     * [취향보고서] 버튼 클릭 시 호출되는 메서드.
     */
    @OnClick(R.id.tv_show_my_report)
    public void showMyMovieTasteReport(){
        Intent report = new Intent(getContext(), MovieTasteReportActivity.class);
        startActivity(report);
    }

    @Override
    public void showLoading() {
        loading_indicator.smoothToShow();
        recycler_recommend_movies.setVisibility(View.GONE);
        layout_error.setVisibility(View.GONE);
        tv_show_my_report.setVisibility(View.INVISIBLE);    // 부모가 Relative 레이아웃이므로 레이아웃의 변화가 없도록 invisible 로 설정해야함.
    }

    @Override
    public void hideLoading() {
        loading_indicator.hide();
    }

    @Override
    public void addWantToWatch(final int movieIndex) {

        Movie wantToWatch = recommendMovieAdapter.getItem(movieIndex);
        HashMap<String, String> data_wantToWatchMovieUPDATE = new HashMap<String, String>();
        data_wantToWatchMovieUPDATE.put("movieCode", wantToWatch.getMovieCode()+"");
        data_wantToWatchMovieUPDATE.put("userCode", loginUser.getUserCode()+"");
        // 현재 '보고싶어요' 목록에 있다면 삭제하도록 요청하고, '보고싶어요' 목록에 없다면 목록에 추가하도록 한다.
        data_wantToWatchMovieUPDATE.put("flag", ( wantToWatch.isExistWantToWatch() ? "DELETE" : "ADD"));

        Call<ResponseData> call_wantToWatchMovieUPDATE = movieService.post("wantToWatchMovieUPDATE", data_wantToWatchMovieUPDATE);

        call_wantToWatchMovieUPDATE.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){

                    recommendMovieAdapter.getItem(movieIndex).setExistWantToWatch( !recommendMovieAdapter.getItem(movieIndex).isExistWantToWatch() );
                    recommendMovieAdapter.notifyItemChanged(movieIndex);

                    return;
                }

                Log.e(TAG, "보고싶어요 추가 실패");
                Toast.makeText(getContext(), "다시 요청해주세요.", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "보고싶어요 추가 실패 (onFailure)");
                Toast.makeText(getContext(), "다시 요청해주세요.", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


}
