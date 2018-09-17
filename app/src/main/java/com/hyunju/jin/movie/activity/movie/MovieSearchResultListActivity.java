package com.hyunju.jin.movie.activity.movie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.nofolder.GalleryType2Activity;
import com.hyunju.jin.movie.activity.tensorflow.ClassifierActivity;
import com.hyunju.jin.movie.adapter.movie.MovieSearchResultItemAdapter;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**

 */
public class MovieSearchResultListActivity extends SuperActivity implements MovieSearchListener{

    public static final int REQ_GALLERY = 2;
    public static final String DATA_KEY_SELECT = "selected";
    public static final String DATA_KEY_CALL = "activity";   // 호출한 액티비티 태그
    private String callActivity;

    @BindView(R.id.msg_result_empty) TextView msg_result_empty;
    @BindView(R.id.ed_search_text) EditText ed_search_text;

    // 영화 검색 결과 데이터
    @BindView(R.id.recycler_movie_search_result_list) RecyclerView recycler_movie_search_result_list;
    ArrayList<Movie> movieSearchResultList;
    MovieSearchResultItemAdapter movieSearchResultItemAdapter;

    MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search_result_list);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            callActivity = bundle.getString(DATA_KEY_CALL, "");
        }

        movieService = RetrofitClient.getMovieService();

        movieSearchResultList = new ArrayList<Movie>();
        movieSearchResultItemAdapter = new MovieSearchResultItemAdapter(getContext(), movieSearchResultList, this);
        recycler_movie_search_result_list.setAdapter(movieSearchResultItemAdapter);
        recycler_movie_search_result_list.setLayoutManager(new LinearLayoutManager(getContext()));
        //recycler_movie_search_result_list.setHasFixedSize(true);

        msg_result_empty.setVisibility(View.GONE);
        recycler_movie_search_result_list.setVisibility(View.VISIBLE);

        ed_search_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                switch (actionId){
                    case EditorInfo.IME_ACTION_SEARCH:
                        movieSearch();
                        break;
                }
                return true;
            }
        });

    }

    @OnClick(R.id.btn_search)
    public void movieSearch(){
        // 검색어가 있다면

        if(StringUtils.isEmpty(ed_search_text.getText().toString())){
            return;
        }

        // 서버에서 검색
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("searchText", ed_search_text.getText().toString());
        Call<ArrayList<Movie>> resCall = movieService.getMovieList("movieListGETbySearch", data); // req도 다 코드로 정리해두면 편할텐데
        resCall.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    // 새로운 검색이므로 리스트 초기화
                    movieSearchResultList.clear();
                    movieSearchResultList.addAll(response.body());
                    movieSearchResultItemAdapter.notifyDataSetChanged();
                }else{

                }
            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {

            }
        });

        // 개발 테스트 코드
    }


    /**
     * 영화 검색 결과 목록에서 영화 하나를 선택한 경우
     * @param movie
     */
    @Override
    public void selectMovie(Movie movie) {
        if(callActivity.equals("posting")){ // 영화 검색 화면을 호출한 이유가 포스팅 작성 시 영화 태그를 추가하기 위한 경우엔
            Intent returnIntent = new Intent();
            returnIntent.putExtra(DATA_KEY_SELECT, movie);  // 선택한 영화 정보를 담아서
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }else {
            // 그외의 경우는 사용자가 영화 정보를 보기 위해 검색을 한 경우이므로
            Intent intent = new Intent(getContext(), MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, movie);
            startActivity(intent);  // 선택한 영화의 상세정보 화면으로 이동한다.
        }
    }


    /**
     * 카메라를 통해 영화를 검색하는 화면으로 이동한다.
     */
    @OnClick(R.id.btn_search_camera)
    public void searchByCamera(){
        /*Intent movieSearch = new Intent(getContext(), ClassifierActivity.class);
        startActivity(movieSearch);*/

        // 카메라로 찍은 사진으로 검색
       /* Intent movieSearch = new Intent(getContext(), MovieImageSearchCameraActivity.class);
        startActivity(movieSearch);*/

        // 갤러리에서 선택한 사진만 검색
        Intent gallery = new Intent(getContext(), GalleryType2Activity.class);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT_MAX, 1);
        gallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, true);
        gallery.putExtra(GalleryFunction.OPTION_FILTER, false);
        startActivityForResult(gallery, REQ_GALLERY);
        Toast.makeText(getContext(), "검색할 사진을 선택하세요.", Toast.LENGTH_SHORT).show();
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
}
