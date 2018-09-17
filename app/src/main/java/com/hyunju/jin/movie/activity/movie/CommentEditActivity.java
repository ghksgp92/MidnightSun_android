package com.hyunju.jin.movie.activity.movie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.ItemDecorationGalleryColumns;
import com.hyunju.jin.movie.activity.gallery.nofolder.GalleryType2Activity;
import com.hyunju.jin.movie.adapter.posting.CommentEditMediaAdapter;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class CommentEditActivity extends SuperActivity {

    public static final String DATA_KEY_MOVIE = "commentForMovie";
    public static final int REQ_SELECT_PHOTOS = 1000;

    @BindView(R.id.tv_movie_title) TextView tv_movie_title; // 현재 후기를 작성하고 있는 영화의 제목 ex) #오션스8
    @BindView(R.id.ed_posting_contents) EditText ed_comment_text;   // 후기 내용 입력 칸

    // 사용자가 첨부한 사진/동영상 목록
    @BindView(R.id.recycler_photos) RecyclerView recycler_photos;
    ArrayList<Gallery> selectedList;
    CommentEditMediaAdapter commentEditMediaAdapter;

    MovieService movieService;

    private Movie commentForMovie;  // 현재 후기를 작성하고 있는 영화 정보

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_edit);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            commentForMovie = (Movie) bundle.getSerializable(DATA_KEY_MOVIE);
            getIntent().getSerializableExtra(DATA_KEY_MOVIE);

            if(commentForMovie == null || commentForMovie.getMovieCode() == 0){
                Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            movieService = RetrofitClient.getMovieService();

            tv_movie_title.setText("#"+commentForMovie.getMovieTitle());

            selectedList = new ArrayList<Gallery>();
            commentEditMediaAdapter = new CommentEditMediaAdapter(getContext(), selectedList);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 5);
            recycler_photos.setLayoutManager(gridLayoutManager);
            recycler_photos.addItemDecoration(new ItemDecorationGalleryColumns(1, gridLayoutManager.getSpanCount()));
            recycler_photos.setAdapter(commentEditMediaAdapter);

        }
    }

    @OnClick(R.id.btn_ok)
    public void ok(){

        if(StringUtils.isEmpty(ed_comment_text.getText().toString())){
            Toast.makeText(getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, RequestBody> data_commentUPDATE = new HashMap<String, RequestBody>();
        data_commentUPDATE.put("userCode", RequestBody.create(MediaType.parse("text/plain"), loginUser.getUserCode()+""));
        data_commentUPDATE.put("movieCode", RequestBody.create(MediaType.parse("text/plain"), commentForMovie.getMovieCode()+""));
        data_commentUPDATE.put("contents", RequestBody.create(MediaType.parse("text/plain"), ed_comment_text.getText().toString()));

        List<MultipartBody.Part> file_commentUPDATE = new ArrayList<MultipartBody.Part>();
        for (Gallery file : selectedList ) {
            HashMap<String, String> media = file.getMedia();
            File f = new File(media.get(GalleryFunction.KEY_PATH));


        }

        Call<ResponseData> call_commentUPDATE = movieService.postMultiPart("commentUPDATE", data_commentUPDATE, file_commentUPDATE);

    }

    @OnClick(R.id.tv_add_photos)
    public void openGallery(){

        Intent gallery = new Intent(getContext(), GalleryType2Activity.class);
        gallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, false);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT, true);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT_MAX, 10);
        startActivityForResult(gallery, REQ_SELECT_PHOTOS);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_SELECT_PHOTOS:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();

                    selectedList.clear();
                    selectedList.addAll((ArrayList<Gallery>) data.getSerializableExtra(GalleryType2Activity.DATA_KEY_SELECT_LIST) );

                    commentEditMediaAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

}
