package com.hyunju.jin.movie.activity.posting;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 페이징 적용해보기
 */
public class RatingCommentActivity extends SuperActivity {

    public static final String DATA_KEY_MOVIE = "commentForMovie";

    @BindView(R.id.tv_movie_title) TextView tv_movie_title;
    @BindView(R.id.ed_comment) EditText ed_comment;
    @BindView(R.id.rating_bar) RatingBar rating_bar;

    private Movie commentForMovie;  // 현재 후기를 작성하고 있는 영화 정보
    private MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_comment);
        ButterKnife.bind(this);
        initialize();


    }

    private void initialize() {
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            commentForMovie = (Movie) bundle.getSerializable(DATA_KEY_MOVIE);
            getIntent().getSerializableExtra(DATA_KEY_MOVIE);

            if (commentForMovie == null || commentForMovie.getMovieCode() == 0) {
                Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

        }else{
            Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        movieService = RetrofitClient.getMovieService();
        tv_movie_title.setText(commentForMovie.getMovieTitle());
        addListenerOnView();
    }

    private void addListenerOnView(){

    }

    @OnClick(R.id.btn_complete)
    public void addRatingComment(){

        HashMap<String, String> data_ratingCommentUPDATE = new HashMap<String, String>();

        Call<ResponseData> call_ratingCommentUPDATE = movieService.post("", data_ratingCommentUPDATE);
        call_ratingCommentUPDATE.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){

                }else{

                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {

            }
        });

    }

    @OnClick(R.id.icon_back)
    public void back(){
        finish();
    }
}
