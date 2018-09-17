package com.hyunju.jin.movie.activity.recommend;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.adapter.recommend.FavoriteActorAdapter;
import com.hyunju.jin.movie.adapter.recommend.FavoriteDirectorAdapter;
import com.hyunju.jin.movie.adapter.recommend.RecommendUserAdapter;
import com.hyunju.jin.movie.datamodel.Actor;
import com.hyunju.jin.movie.datamodel.Director;
import com.hyunju.jin.movie.datamodel.MovieReportData;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.extlib.wordcloud.WordCloud;
import com.hyunju.jin.movie.extlib.wordcloud.WordCloudColorTemplate;
import com.hyunju.jin.movie.extlib.wordcloud.WordCloudView;
import com.hyunju.jin.movie.network.RecommendService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.wang.avi.AVLoadingIndicatorView;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.communication.IOnPointFocusedListener;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieTasteReportActivity extends SuperActivity {

    @BindView(R.id.layout_container) LinearLayout layout_container; // 취향 보고서의 부모 레이아웃. 총 영화수를 로드하면 보인다.
    @BindView(R.id.layout_error) LinearLayout layout_error;     // 총 영화수를 가져오는데 실패하면 보이는 레이아웃.
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator; // 총 영화수 로딩중임을 표시하는 프로그래스바

    @BindView(R.id.tv_user_id) TextView tv_user_id; // 사용자 ID 를 보여주는 View
    @BindView(R.id.tv_movie_rating_count) TextView tv_movie_rating_count; // 평점 수를 보여주는 View

    @BindView(R.id.layout_view_report) LinearLayout layout_view_report;    // 별점분포, 선호배우 등의 모든 보고서 내용의 부모레이아웃

    RecommendService recommendService;  // 서버에 취향 분석 결과를 조회하기 위한 HTTP 서비스 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_taste_report);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize(){
        recommendService = RetrofitClient.getRecommendService();
        tv_user_id.setText(loginUser.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 총 평점 수 로드
        loading_indicator.smoothToShow(); // 총 평점수를 로드중임을 화면에 표시한다.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadRatingTotalCount();
                /*
                    총 평점 수 로드 후, 성공하면 보고서를 로드한다.
                    실패할 경우 평점이 없다고 가정하기 때문에 에러 메시지를 띄운다.
                    이 작업은 loadRatingTotalCount() 메소드에서 처리한다.
                 */
            }
        }, 800);
    }

    /**
     * 사용자가 본 총 영화수를 로드한다.
     */
    private void loadRatingTotalCount(){

        loading_indicator.smoothToHide();

        HashMap<String, String> data_getRatingMovieCount = new HashMap<>();
        data_getRatingMovieCount.put("userCode", loginUser.getUserCode()+"");
        Call<ResponseData> call_getRatingMovieCount = recommendService.get("getRatingMovieCount", data_getRatingMovieCount);
        call_getRatingMovieCount.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    ResponseData responseData = response.body();
                    if(responseData.getCode() == 1){
                        tv_movie_rating_count.setText(responseData.getMsg()+"");    // 사용자가 본 총 영화수

                        layout_container.setVisibility(View.VISIBLE);
                        layout_view_report.setVisibility(View.VISIBLE);

                        // 모든 보고서를 로드한다. 각각의 메소드는 내부적으로 비동기 처리된다.
                        loadRatingReport(); // 별점분포 보고서 로드
                        loadTagReport();    // 태그 보고서 로드

                        loadActorReport();  // 선호배우 보고서 로드
                        loadDirectorReport();   // 선호감독 보고서 로드
                        loadMateReport();   // 추천친구 로드
                        return;
                    }
                }

                // 이 코드가 실행된다면 사용자의 총 영화평점 수를 로드하지 못했기 때문이다. 에러메시지를 띄운다.
                Log.e(TAG, "사용자 영화평점 수 조회 실패");
                layout_error.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "사용자 영화평점 수 조회 실패 (onFailure)");
                layout_error.setVisibility(View.VISIBLE);
                t.printStackTrace();
            }
        });
    }

    /* 별점분포 */

    @BindView(R.id.layout_rating_report) LinearLayout layout_rating_report; // 별점분포 정보 로드 성공시 보여지는 레이아웃
    @BindView(R.id.chart_rating) ValueLineChart chart_rating;
    @BindView(R.id.tv_total_rating_count) TextView tv_total_rating_count; // 전체 평점 수
    @BindView(R.id.tv_max_rating_category) TextView tv_max_rating_category; // 많이 준 평점
    @BindView(R.id.tv_rating_average) TextView tv_rating_average; // 평점 평균

    @BindView(R.id.layout_error_rating_report) LinearLayout layout_error_rating_report; // 별점분포 정보 로딩 실패시 보여지는 레이아웃
    @BindView(R.id.tv_error_msg_rating_report) TextView tv_error_msg_rating_report; // 별점분포 정보 로드 실패시 상세 메시지
    @BindView(R.id.loading_indicator_rating_report) AVLoadingIndicatorView loading_indicator_rating_report; // 별점분포 정보 로딩 프로그래스바

    /**
     * '별점분포' 분석 결과를 로드한다.
     */
    private void loadRatingReport(){

        loading_indicator_rating_report.smoothToShow();
        layout_rating_report.setVisibility(View.GONE);
        layout_error_rating_report.setVisibility(View.GONE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data_getRatingChart = new HashMap<>();
                data_getRatingChart.put("userCode", loginUser.getUserCode()+"");

                Call<ArrayList<MovieReportData>> call_getRatingChart = recommendService.getReportDatas("getRatingChart", data_getRatingChart);
                call_getRatingChart.enqueue(new Callback<ArrayList<MovieReportData>>() {
                    @Override
                    public void onResponse(Call<ArrayList<MovieReportData>> call, Response<ArrayList<MovieReportData>> response) {

                        layout_error_rating_report.setVisibility(View.GONE);
                        if(response.isSuccessful()){
                            loading_indicator_rating_report.smoothToHide();
                            layout_rating_report.setVisibility(View.VISIBLE);

                            ArrayList<MovieReportData> ratings = response.body();
                            setRatingReport(ratings);
                            return;
                        }
                        Log.e(TAG, "별점분포 로드 실패");
                        loading_indicator_rating_report.smoothToHide();
                        layout_error_rating_report.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<MovieReportData>> call, Throwable t) {
                        Log.e(TAG, "별점분포 로드 실패 (onFailure)");
                        loading_indicator_rating_report.smoothToHide();
                        layout_error_rating_report.setVisibility(View.VISIBLE);
                    }
                });

            }
        }, 500);
    }

    /**
     * 별점분포에 대한 보고서 정보를 화면에 표시한다.
     * @param ratings
     */
    private void setRatingReport(@Nullable  ArrayList<MovieReportData> ratings){

        // 1) 별점분포 그래프를 만든다.

        // ValueLineChart 에 데이터를 추가한다.
        ValueLineSeries series = new ValueLineSeries();
        //series.setColor(Color.parseColor("#878bb6"));    // 차트 컬러 지정
        series.setColor(0xFF63CBB0);
        series.addPoint(new ValueLinePoint("start", 0)); // 맨 처음 요소가 그래프 라벨로 나타나지 않는 문제가 있어서 임의로 추가함.

        // 가장 많이 준 평점을 구하기 위해 선언함
        String max_rating_category = "0.5"; // 현재 가장 많이 준 평점을 저장한다. 최소평점인 0.5로 지정함.
        int max_rating_count = 0; // 현재 가장 많이 준 평점의 수를 저장한다.

        // 평점 평균을 구하기 위해 선언함
        float sum_rating = 0.0f;
        int sum_rating_count = 0;
        for(MovieReportData data : ratings){
            series.addPoint(new ValueLinePoint(data.getRatingCategory(), data.getCount()));
            if( max_rating_count < data.getCount() ){
                max_rating_category = data.getRatingCategory();
                max_rating_count = data.getCount();
            }
            sum_rating += Float.parseFloat(data.getRatingCategory()) * data.getCount(); // (평점 * 평점 수) 를 구해서 합친다.
            sum_rating_count += data.getCount();    // 전체 평점 수에 평점 수를 더한다.
        }
        series.addPoint(new ValueLinePoint("end", 0)); // 맨 마지막 요소가 그래프 라벨로 나타나지 않는 문제가 있어서 임의로 추가함.

        chart_rating.addSeries(series);
        chart_rating.setOnPointFocusedListener(new IOnPointFocusedListener() {
            @Override
            public void onPointFocused(int _PointPos) {
                Log.e(TAG, "Pos: " + _PointPos);
            }
        });
        chart_rating.startAnimation();

        // 2) 총 평점 수를 표시한다.
        tv_total_rating_count.setText(sum_rating_count+"");

        // 3) 가장 많이 준 평점 수를 표시한다.
        tv_max_rating_category.setText(max_rating_category);

        // 4) 평균 평점을 표시한다.
        float average_rating = sum_rating / sum_rating_count;
        tv_rating_average.setText(( Math.round(average_rating*10)/10.0 )+"");

    }


    /* 선호태그 */

    @BindView(R.id.layout_tag_cloud) LinearLayout layout_tag_cloud;
    @BindView(R.id.word_cloud_movie_tag)
    WordCloudView word_cloud_movie_tag;

    @BindView(R.id.layout_error_tag_cloud) LinearLayout layout_error_tag_cloud;
    @BindView(R.id.loading_indicator_tag_cloud) AVLoadingIndicatorView loading_indicator_tag_cloud;

    /**
     * '선호태그' 분석 결과를 로드한다.
     */
    private void loadTagReport(){

        loading_indicator_tag_cloud.smoothToShow();
        layout_tag_cloud.setVisibility(View.GONE);
        layout_error_tag_cloud.setVisibility(View.GONE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> data_getUserMovieTagCloud = new HashMap<>();
                data_getUserMovieTagCloud.put("userCode", loginUser.getUserCode()+"");

                Call<ArrayList<MovieReportData>> call_getUserMovieTagCloud = recommendService.getReportDatas("getUserMovieTagCloud", data_getUserMovieTagCloud);
                call_getUserMovieTagCloud.enqueue(new Callback<ArrayList<MovieReportData>>() {
                    @Override
                    public void onResponse(Call<ArrayList<MovieReportData>> call, Response<ArrayList<MovieReportData>> response) {
                        if(response.isSuccessful()){

                            ArrayList<MovieReportData> datas = response.body();

                            List<WordCloud> tagCloudDatas = new ArrayList<>();
                            for (MovieReportData data : datas){
                                tagCloudDatas.add(new WordCloud(data.getTag(), data.getCount()));
                            }

                            // 태그 클라우드는 https://github.com/alhazmy13/AndroidWordCloud 라이브러리를 사용했다.
                            word_cloud_movie_tag.setDataSet(tagCloudDatas);

                            // 폰의 화면 사이즈를 구해서 태그 클라우드가 보여질 크기를 정한다.
                            // 가로 크기를 가득 채우도록하고, 세로는 280으로 고정된다.
                            Display display = getWindowManager().getDefaultDisplay();
                            if( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1 ){ // SDK 13 부터
                                Point size = new Point();
                                display.getSize(size);
                                word_cloud_movie_tag.setSize(size.x/5, size.y/7);
                            }else{
                                word_cloud_movie_tag.setSize(display.getWidth()/5, display.getHeight()/7);
                            }
                            // word_cloud_movie_tag.setSize(240, 240);
                            word_cloud_movie_tag.setColors(WordCloudColorTemplate.PASTEL_COLORS);
                            word_cloud_movie_tag.setScale(52, 18);
                            word_cloud_movie_tag.notifyDataSetChanged();

                            loading_indicator_tag_cloud.smoothToHide();
                            layout_tag_cloud.setVisibility(View.VISIBLE);
                            return;
                        }

                        Log.e(TAG, "태그 클라우드 로드 실패");
                        loading_indicator_tag_cloud.smoothToHide();
                        layout_error_tag_cloud.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<MovieReportData>> call, Throwable t) {
                        Log.e(TAG, "태그 클라우드 로드 실패 (onFailure)");
                        loading_indicator_tag_cloud.smoothToHide();
                        layout_error_tag_cloud.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }

    /* 선호배우 */

    @BindView(R.id.layout_favorite_actor) LinearLayout layout_favorite_actor;
    @BindView(R.id.recycler_view_favorite_actors) RecyclerView recycler_view_favorite_actors;
    FavoriteActorAdapter favoriteActorAdapter;

    @BindView(R.id.layout_error_favorite_actor) LinearLayout layout_error_favorite_actor;
    @BindView(R.id.loading_indicator_favorite_actor) AVLoadingIndicatorView loading_indicator_favorite_actor;


    /**
     * '선호배우' 분석 결과를 로드한다.
     */
    private void loadActorReport(){

        loading_indicator_favorite_actor.smoothToShow();
        layout_favorite_actor.setVisibility(View.GONE);
        layout_error_favorite_actor.setVisibility(View.GONE);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> data_getFavoriteActors = new HashMap<>();
                data_getFavoriteActors.put("userCode", loginUser.getUserCode() + "");

                Call<ArrayList<Actor>> call_getFavoriteActors = recommendService.getActorList("getFavoriteActors", data_getFavoriteActors);
                call_getFavoriteActors.enqueue(new Callback<ArrayList<Actor>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Actor>> call, Response<ArrayList<Actor>> response) {
                        if (response.isSuccessful()) {

                            ArrayList<Actor> list = response.body();
                            favoriteActorAdapter = new FavoriteActorAdapter(getContext(), list);
                            recycler_view_favorite_actors.setLayoutManager(new LinearLayoutManager(getContext()));
                            recycler_view_favorite_actors.setAdapter(favoriteActorAdapter);

                            loading_indicator_favorite_actor.smoothToHide();
                            layout_favorite_actor.setVisibility(View.VISIBLE);

                            return;
                        }

                        Log.e(TAG, "선호배우 로드 실패");
                        loading_indicator_favorite_actor.smoothToHide();    // 로딩 표시를 없앤다.
                        layout_error_favorite_actor.setVisibility(View.VISIBLE);   // 로딩 실패 레이아웃을 보여준다.
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Actor>> call, Throwable t) {
                        Log.e(TAG, "선호배우 로드 실패 (onFailure)");
                        loading_indicator_favorite_actor.smoothToHide();    // 로딩 표시를 없앤다.
                        layout_error_favorite_actor.setVisibility(View.VISIBLE);   // 로딩 실패 레이아웃을 보여준다.
                    }
                });

            }
        }, 500);
    }

    /* 선호감독 */

    @BindView(R.id.layout_director_report) LinearLayout layout_director_report; // 선호감독 정보 로드에 성공하면 보여지는 레이아웃
    @BindView(R.id.recycler_view_directors) RecyclerView recycler_view_directors; // 선호감독 목록
    FavoriteDirectorAdapter favoriteDirectorAdapter;

    @BindView(R.id.layout_error_director_report) LinearLayout layout_error_director_report; // 선호감독 정보 로드에 실패할 시 보여지는 레이아웃
    @BindView(R.id.tv_error_msg_favorite_director) TextView tv_error_msg_favorite_director;
    @BindView(R.id.loading_indicator_director_report) AVLoadingIndicatorView loading_indicator_director_report;

    /**
     * '선호감독' 분석 결과를 로드한다.
     */
    private void loadDirectorReport(){

        loading_indicator_director_report.smoothToShow(); // 선호감독 정보를 로드중임을 알린다.
        layout_error_director_report.setVisibility(View.GONE); // 로드 실패시 보여지는 레이아웃을 감춘다.
        layout_director_report.setVisibility(View.GONE); // 선호감독 목록이 있는 레이아웃을 감춘다.

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> data_getFavoriteDirectors = new HashMap<>();
                data_getFavoriteDirectors.put("userCode", loginUser.getUserCode()+"");

                Call<ArrayList<Director>> call_getFavoriteDirectors = recommendService.getDirectorLIst("getFavoriteDirectors", data_getFavoriteDirectors);
                call_getFavoriteDirectors.enqueue(new Callback<ArrayList<Director>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Director>> call, Response<ArrayList<Director>> response) {
                        if(response.isSuccessful()){

                            ArrayList<Director> list = response.body();
                            favoriteDirectorAdapter = new FavoriteDirectorAdapter(getContext(), list);
                            recycler_view_directors.setLayoutManager(new LinearLayoutManager(getContext()));
                            recycler_view_directors.setAdapter(favoriteDirectorAdapter);

                            loading_indicator_director_report.smoothToHide(); // 로딩이 완료됬으므로 프로그래스바를 감춘다.
                            layout_error_director_report.setVisibility(View.GONE); // 로드 실패시 보여지는 레이아웃을 감춘다.
                            layout_director_report.setVisibility(View.VISIBLE); // 선호감독 목록이 있는 레이아웃을 보여준다.
                            return;
                        }

                        Log.e(TAG, "선호감독 로드 실패");
                        loading_indicator_director_report.smoothToHide(); // 로딩이 완료됬으므로 프로그래스바를 감춘다.
                        layout_error_director_report.setVisibility(View.VISIBLE); // 로드 실패 레이아웃을 보여준다.

                    }

                    @Override
                    public void onFailure(Call<ArrayList<Director>> call, Throwable t) {
                        Log.e(TAG, "선호감독 로드 실패 (onFailure)");
                        loading_indicator_director_report.smoothToHide(); // 로딩이 완료됬으므로 프로그래스바를 감춘다.
                        layout_error_director_report.setVisibility(View.VISIBLE); // 로드 실패 레이아웃을 보여준다.
                    }
                });
            }
        },500);

    }


    @BindView(R.id.layout_recommend_user) LinearLayout layout_recommend_user; // 정상적인 보고서 레이아웃
    @BindView(R.id.recycler_view_recommend_users) RecyclerView recycler_view_recommend_users;
    RecommendUserAdapter recommendUserAdapter;

    @BindView(R.id.layout_error_recommend_user) LinearLayout layout_error_recommend_user; // 보고서 로딩중임을 알려주는 레이아웃
    @BindView(R.id.tv_error_msg_recommend_user) TextView tv_error_msg_recommend_user; // 보고서 로딩 실패 메시지
    @BindView(R.id.loading_indicator_recommend_user) AVLoadingIndicatorView loading_indicator_recommend_user; // 추천친구 로딩 프로그래스바

    /**
     * '추천친구' 정보를 로드한다.
     */
    private void loadMateReport(){

        layout_recommend_user.setVisibility(View.GONE);
        layout_error_recommend_user.setVisibility(View.GONE);
        tv_error_msg_recommend_user.setVisibility(View.GONE);
        loading_indicator_recommend_user.smoothToShow();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                HashMap<String, String> data_getRecommendUsers = new HashMap<>();
                data_getRecommendUsers.put("userCode", loginUser.getUserCode() + "");

                Call<ArrayList<User>> call_getRecommendUsers = recommendService.getMates("getRecommendUsers", data_getRecommendUsers);
                call_getRecommendUsers.enqueue(new Callback<ArrayList<User>>() {
                    @Override
                    public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                        layout_error_recommend_user.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            ArrayList<User> mates = response.body();

                            recommendUserAdapter = new RecommendUserAdapter(getContext(), mates);
                            recycler_view_recommend_users.setLayoutManager(new LinearLayoutManager(getContext()));
                            recycler_view_recommend_users.setAdapter(recommendUserAdapter);

                            loading_indicator_recommend_user.smoothToHide();
                            layout_recommend_user.setVisibility(View.VISIBLE);
                            return;
                        }
                        Log.e(TAG, "추천친구 로드 실패");
                        loading_indicator_recommend_user.smoothToHide();
                        layout_error_recommend_user.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<ArrayList<User>> call, Throwable t) {
                        Log.e(TAG, "추천친구 로드 실패 (onFailure)");
                        loading_indicator_recommend_user.smoothToHide();
                        layout_error_recommend_user.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 500);
    }



    @OnClick(R.id.btn_retry)
    public void retryLoadRatingTotalCount(){
        // 총 평점 수 로드
        layout_error.setVisibility(View.GONE);
        loading_indicator.smoothToShow(); // 총 평점수를 로드중임을 화면에 표시한다.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadRatingTotalCount();
                /*
                    총 평점 수 로드 후, 성공하면 보고서를 로드한다.
                    실패할 경우 평점이 없다고 가정하기 때문에 에러 메시지를 띄운다.
                    이 작업은 loadRatingTotalCount() 메소드에서 처리한다.
                 */
            }
        }, 500);
    }


}
