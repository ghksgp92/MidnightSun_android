package com.hyunju.jin.movie.activity.movie;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.gson.reflect.TypeToken;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.posting.RatingCommentActivity;
import com.hyunju.jin.movie.adapter.movie.MovieDetailViewPagerAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.fragment.MyCollectionListDialogFragment;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.iconics.view.IconicsImageView;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 영화 상세정보를 보여주는 화면
 *
 * (문제)
 * OOM 에러 -> 배경에 깔리는 포스터 제거한 후 조금 사라졌으나.. 여전한듯? 우선 내가 참조하는 포스터 자체의 크기가 너무 큼. 데이터 업데이트부터 해야함.
 */
public class MovieDetailActivity extends SuperActivity implements LoadingListener {

    @BindView(R.id.layout_container) CoordinatorLayout layouat_container;  // 화면의 최상위 레이아웃
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator; // 로딩중임을 나타내는 뷰

    @BindView(R.id.vpager_category_view) ViewPager vpager_category_view;        // 영화 정보 카테고리 별 화면을 나타내는 ViewPager
    @BindView(R.id.tab_movie_info_category) TabLayout tab_movie_info_category;  // 영화 정보 카테고리 ViewPager 와 연결된 탭
    @BindView(R.id.img_movie_poster) RoundedImageView img_movie_poster;         // 영화 메인 포스터
    @BindView(R.id.card_view_movie) CardView card_view_movie;
    @BindView(R.id.btn_streaming) FancyButton btn_streaming;

    // 화면 상에서 스크롤을 내릴 시 축소되는 레이아웃 안에 있는 View
    @BindView(R.id.tv_movie_title) TextView tv_movie_title;
    @BindView(R.id.tv_movie_title_en) TextView tv_movie_title_en;

    @BindView(R.id.toolbar_movie_detail) Toolbar toolbar_movie_detail;

    @BindView(R.id.layout_app_bar) AppBarLayout layout_app_bar;
    @BindView(R.id.layout_tool_bar) CollapsingToolbarLayout layout_tool_bar;

    // 보고싶어요 버튼
    @BindView(R.id.layout_want_to_watch) LinearLayout layout_want_to_watch;
    @BindView(R.id.icon_want_to_watch) IconicsImageView icon_want_to_watch;
    @BindView(R.id.tv_want_to_watch) TextView tv_want_to_watch;

    // 컬렉션 추가 버튼
    @BindView(R.id.layout_add_collection) LinearLayout layout_add_collection;

    private Movie movie;    // 현재 보고있는 영화정보를 담는 객체
    private MovieCollection wantToWatchCollection;
    //private boolean wantToWatch; // 현재 보고있는 영화가 '보고싶어요'에 추가된 영화인지 나타내는 변수

    private int mAppBarMaxScrollSize;   // layout_app_bar 의 최대 사이즈를 저장한다.

    private boolean mIsAvatarShown = true;

    private MovieService movieService;  // 영화 정보를 서버에 요청하기 위한 객체

    private final int REQ_WRITE_COMMENT = 1000;     // 코멘트 작성 요청코드. 작성을 완료하면 자동으로 코멘트 탭으로 이동시키기 위해 추가함.
    public static final String DATA_KEY_MOVIE = "movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        // 영화 정보가 로딩될때까지 화면을 감추고 로딩중임을 표시한다.
        showLoading();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            movie = (Movie) bundle.getSerializable(DATA_KEY_MOVIE);
            if(movie == null || StringUtils.isEmpty(movie.getMovieCode()+"") ){
                Log.e(TAG, "movie is null 혹은 movieCode is null");
                Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        initialize();
    }

    @OnClick({R.id.icon_comment_write, R.id.tv_comment_write})
    public void writeComment(){

        Intent ratingComment = new Intent(getContext(), RatingCommentActivity.class);
        ratingComment.putExtra(RatingCommentActivity.DATA_KEY_MOVIE, movie);
        startActivity(ratingComment);   // 리턴해서 자동으로 코멘트 탭으로 이동하게 만들어주기

        /*
        Intent write = new Intent(getContext(), CommentEditActivity.class);
        write.putExtra(CommentEditActivity.DATA_KEY_MOVIE, movie;
        startActivityForResult(write, REQ_WRITE_COMMENT);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
           /* case REQ_WRITE_COMMENT:
                if(requestCode == RESULT_OK){
                    if(tab_movie_info_category.getSelectedTabPosition() !=  3){
                        vpager_category_view.setCurrentItem(3);
                    }
                }
                break;*/
        }
    }

    /**
     * 영화를 컬렉션에 추가할 수 있도록 내 컬렉션 목록을 다이얼로그로 띄운다.
     */
    @OnClick({R.id.icon_add_collection, R.id.tv_add_collection})
    public void addCollection(){

        String dialogTag = "myCollectionList";
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // 이미 만들어진 다이얼로그가 있다면
        Fragment prev = getFragmentManager().findFragmentByTag(dialogTag);
        if (prev != null) {
            ft.remove(prev); // 삭제 후
        }
        ft.addToBackStack(null);

        // 새롭게 다이얼로그를 생성한다.
        DialogFragment myCollectionListDialogFragment = new MyCollectionListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("movie", movie); // 내 컬렉션에 영화가 포함되어 있는지 확인해야하기 때문에 영화 정보를 전달한다.
        myCollectionListDialogFragment.setArguments(bundle);
        myCollectionListDialogFragment.show(ft, dialogTag);

    }

    /**
     * 보고싶어요를 누를때 호출됨.
     */
    @OnClick({R.id.icon_want_to_watch, R.id.tv_want_to_watch})
    public void clickLike(){

        HashMap<String, String> data_wantToWatchMovieUPDATE = new HashMap<String, String>();
        // 사용자 코드로 '보고싶어요' 컬렉션을 찾을 수 있기 때문에 컬렉션 코드를 보내지 않도록 함.
        //data_wantToWatchMovieUPDATE.put("collectionCode", wantToWatchCollection.getCollectionCode()+"");
        data_wantToWatchMovieUPDATE.put("movieCode", movie.getMovieCode()+"");
        data_wantToWatchMovieUPDATE.put("userCode", loginUser.getUserCode()+"");
        data_wantToWatchMovieUPDATE.put("flag", ( wantToWatchCollection.isMovieContainCheck() ? "DELETE" : "ADD"));  // 현재 '보고싶어요' 상태라면 다시 클릭할땐 삭제를 해야함.

        Call<ResponseData> call_wantToWatchMovieUPDATE = movieService.post("wantToWatchMovieUPDATE", data_wantToWatchMovieUPDATE);

        call_wantToWatchMovieUPDATE.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    wantToWatchCollection.setMovieContainCheck( !wantToWatchCollection.isMovieContainCheck() );
                    if(wantToWatchCollection.isMovieContainCheck()) {
                        icon_want_to_watch.getIcon().color(getResources().getColor(R.color.mainHotPink));
                        tv_want_to_watch.setTextColor(getResources().getColor(R.color.mainHotPink));
                        Toast.makeText(getContext(), "보고싶은 영화에 추가됬습니다.", Toast.LENGTH_SHORT).show();
                    }else{
                        icon_want_to_watch.getIcon().color(getResources().getColor(R.color.defaultGrey));
                        tv_want_to_watch.setTextColor(getResources().getColor(R.color.defaultGrey));

                        Toast.makeText(getContext(), "보고싶은 영화에서 제외됬습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initialize(){

        // 변수 생성 작업
        movieService = RetrofitClient.getMovieService();

        // 보고싶은 영화 컬렉션 정보와 보고싶은 영화 목록에 추가된 영화인지 서버에 요청함.
        HashMap<String, String> data_wantToWatchMovieCHECK = new HashMap<String, String>();
        data_wantToWatchMovieCHECK.put("userCode", loginUser.getUserCode()+"");
        data_wantToWatchMovieCHECK.put("movieCode", movie.getMovieCode()+"");

        Call<MovieCollection> call_wantToWatchMovieCHECK = movieService.getMovieCollection("wantToWatchMovieCHECK", data_wantToWatchMovieCHECK);
        call_wantToWatchMovieCHECK.enqueue(new Callback<MovieCollection>() {
            @Override
            public void onResponse(Call<MovieCollection> call, Response<MovieCollection> response) {
                if(response.isSuccessful()){
                    wantToWatchCollection = response.body();
                    if(wantToWatchCollection.isMovieContainCheck()){
                        tv_want_to_watch.setTextColor(getResources().getColor(R.color.mainHotPink));
                        icon_want_to_watch.getIcon().color(getResources().getColor(R.color.mainHotPink));
                    }else{

                    }
                }
            }

            @Override
            public void onFailure(Call<MovieCollection> call, Throwable t) {

            }
        });

        // 영화 정보를 서버에 요청한다.
        HashMap<String, String> data_getMovie = new HashMap<String, String>();
        data_getMovie.put("movieCode", movie.getMovieCode()+"");
        Call<Movie> request_getMovie = movieService.getMovie("getMovie", data_getMovie);
        request_getMovie.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                if(response.isSuccessful()){
                    movie = response.body();

                    if(movie == null || StringUtils.isEmpty(movie.getMovieCode()+"") ){
                        Log.e(TAG, "서버 DB에 존재하지 않는 영화 요청");
                        Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    vpager_category_view.setAdapter(new MovieDetailViewPagerAdapter(getSupportFragmentManager(), movie));
                    tab_movie_info_category.setupWithViewPager(vpager_category_view);

                    if(StringUtils.isNotEmpty(movie.getPoster())) {
                        Glide.with(getActivity()).load(movie.getPoster()).into(img_movie_poster);
                    }else{

                    }

                    toolbar_movie_detail.setVisibility(View.VISIBLE);

                    // 스트리밍이 가능한 경우, 재생버튼을 보여준다.
                    if(StringUtils.isNotEmpty(movie.getStreamingFile())){
                        btn_streaming.setVisibility(View.VISIBLE);
                    }else{
                        btn_streaming.setVisibility(View.INVISIBLE);
                    }

                    layout_tool_bar.setTitle(" ");  // 초기에는
                    tv_movie_title.setText(movie.getMovieTitle());
                    tv_movie_title_en.setText(movie.getMovieTitle_en());

                    mAppBarMaxScrollSize = layout_app_bar.getTotalScrollRange();

                    hideLoading();

                    addListenerOnView();

                }else{
                    Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {

            }
        });
    }

    /**
     * 영화를 스트리밍 한다. 이전에 보던 기록이 있다면 이어보기를 할 것인지 물어본다.
     * (참고) 이 코드는 WatchingMovieFragment 에서도 완전히 동일하게 사용하고 있음.
     */
    @OnClick(R.id.btn_streaming)
    public void startStreaming(){

        // 이전에 시청 기록이 있는지 확인한다.
        boolean checkWatchingMovie = false;

        SharedPreferences sharedPreferencesBuilder = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferencesBuilder.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);  // 현재 로그인한 사용자 userCode 를 구하고
        String watchingMovieDataKey = loginUserCode+SharedPreferencesBuilder.KEY_WATCHING_MOVIE;
        String watchingMovieValue = sharedPreferencesBuilder.getString(watchingMovieDataKey, "");

        // 이어보기 기록에 해당 영화가 있는지 확인한다.
        if(StringUtils.isNotEmpty(watchingMovieValue)){

            HashMap<Integer, Movie> watchingMovieList = new HashMap<Integer, Movie>();

            try {
                watchingMovieList.putAll( (HashMap<Integer, Movie>) gson.fromJson(watchingMovieValue, new TypeToken<HashMap<Integer, Movie>>() {}.getType()));
                checkWatchingMovie = ( watchingMovieList.get(movie.getMovieCode()) != null ) ;

            }catch (Exception e){
                Log.e(TAG, "이어보기 기록 조회 실패");
            }

            // 시청기록이 있다면 이어보기를 할 것인지 묻는다.
            if(checkWatchingMovie){

                final long restorePlayPosition = watchingMovieList.get(movie.getMovieCode()).getPlayTime();

                // 버튼 텍스트 색상지정은 제공하지 않아서 디폴트 흰색임. 알아서 잘 보이게 버튼 색 지정해야함.
                new FancyAlertDialog.Builder(this).setTitle("이어보기 기록이 있어요.")
                        .setMessage("이어보시겠어요?") // 이 글자색은 앱의 기본 글자색으로 지정됨. 테마가 어두워서 기본 글자색이 밝은 회색으로 지정되어있어 잘 안보임.
                        .setBackgroundColor(Color.parseColor("#595c63"))   // 색상 지정 시, R.color.colorvalue 전달이 안된다고 함. 다이얼로그에서 아이콘이 보이는 부분의 배경색 지정
                        .setNegativeBtnText("처음부터") // 왼쪽 버튼
                        .setNegativeBtnBackground(Color.parseColor("#595c63")) // 왼쪽버튼 색상 지정
                        .OnNegativeClicked(new FancyAlertDialogListener() {
                            @Override
                            public void OnClick() {

                                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
                                // 처음부터 재생하기 때문에 영화 정보만 전달한다.
                                vod.putExtra("playingVideo", movie);
                                startActivity(vod);
                                // 이 메소드가 실행되고 자동으로 다이얼로그가 사라진다.

                            }
                        })
                        .setPositiveBtnBackground(Color.parseColor("#d02c83"))
                        .setPositiveBtnText("이어보기") // 오른쪽 버튼
                        .setAnimation(Animation.SIDE)
                        .isCancellable(true)
                        .setIcon(R.drawable.ic_star_border_black_24dp, Icon.Visible)
                        .OnPositiveClicked(new FancyAlertDialogListener() {
                            @Override
                            public void OnClick() {

                                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
                                // 이어보기를 한다면 해당 위치에서부터 재생할 수 있도록, 파라미터로 영화정보와 재생시간을 보낸다.
                                vod.putExtra(VideoPlayerActivity.DATA_KEY_PLAY_MOVIE, movie);
                                vod.putExtra(VideoPlayerActivity.DATA_KEY_PLAY_POSITION, restorePlayPosition);
                                startActivity(vod);
                                // 이 메소드가 실행되고 자동으로 다이얼로그가 사라진다.

                            }
                        })
                        .build();


            }else{ // 처음 보는 영화라면 바로 재생한다.
                Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
                vod.putExtra("playingVideo", movie);
                startActivity(vod);
            }

        }else{ // 이어보기 기록이 아예 없을 경우 바로 재생한다.

            Intent vod = new Intent(getContext(), VideoPlayerActivity.class);
            vod.putExtra("playingVideo", movie);
            startActivity(vod);
        }
    }


    private void addListenerOnView(){

        // toolbar의 back 버튼 클릭 시 액티비티를 종료한다.
        toolbar_movie_detail.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        // 사용자 스크롤 동작에 의해 layout_app_bar 사이즈가 변경 될 때 필요한 화면 작업을 한다.
        layout_app_bar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 65;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                // 사용자가 스크롤을 얼마나 내렸는지 구한다. 0%일 경우 하나도 내리지 않은 상태, 100%일 경우 화면 가장 아래까지 내린 상태.
                if (mAppBarMaxScrollSize == 0)
                    mAppBarMaxScrollSize = appBarLayout.getTotalScrollRange();
                int percentage = (Math.abs(verticalOffset)) * 100 / mAppBarMaxScrollSize;
                //Log.d(TAG, percentage+"%, verticalOffset: "+verticalOffset);

                // 화면을 65퍼센트 정도 내린 시점에
                if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
                    // 내부에 있는 View들을 감춘다.
                    mIsAvatarShown = false;
                    img_movie_poster.animate().alpha(0).setDuration(200).start();
                    card_view_movie.animate().alpha(0).start();

                    // 아래 작업은 사용자 스크롤에 맞춰 자연스럽게 보이게 하기 위해 일정시간 딜레이 후 나타낸다.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toolbar_movie_detail.setNavigationIcon(R.drawable.ic_back_grey);
                            layout_tool_bar.setTitle(movie.getMovieTitle());  // Title에 영화 제목이 나타나도록 한다.

                        }
                    }, 200);

                }

                // 화면이 다시 올라가면
                if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
                    // 내부에 있는 View들을 나타낸다.
                    mIsAvatarShown = true;
                    toolbar_movie_detail.setNavigationIcon(R.drawable.ic_back_white);
                    layout_tool_bar.setTitle(" "); // Title 을 공백으로 설정해서 영화 제목을 사라지게한다.
                    img_movie_poster.animate().alpha(1).setDuration(200).start();
                    card_view_movie.animate().alpha(1).setDuration(200).start();

                }
            }
        });

    }

    @Override
    public void showLoading() {
        layouat_container.setVisibility(View.INVISIBLE);
        loading_indicator.smoothToShow();

    }

    @Override
    public void hideLoading() {
        loading_indicator.smoothToHide();
        layouat_container.setVisibility(View.VISIBLE);
    }
}
