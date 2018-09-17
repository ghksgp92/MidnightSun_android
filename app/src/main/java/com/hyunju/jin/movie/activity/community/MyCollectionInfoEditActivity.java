package com.hyunju.jin.movie.activity.community;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.activity.gallery.folder.GalleryType1Activity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyCollectionInfoEditActivity extends SuperActivity {

    @BindView(R.id.ed_collection_title) EditText ed_collection_title;

    @BindView(R.id.toggle_collection_public_state) ToggleSwitch toggle_collection_public_state;

    public final int REQ_GALLERY_SELECT = 1000; // 갤러리에서 컬렉션 표지 이미지를 선택하기 위한 요청 코드

    private MovieService movieService;  // 서버에 컬렉션 수정 작업을 요청하기 위한 객체

    private ArrayList<Movie> collectionList;
    private MovieCollection movieCollection;
    private int flag;

    public static final String KEY_COLLECTION_LIST = "collectionList";
    public static final String KEY_FLAG = "flag";
    public static final int FLAG_ADD = 1;       // 컬렉션에 영화를 추가한다.
    public static final int FLAG_MODIFY = 2;    // 컬레음..?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection_info_edit);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            flag = bundle.getInt(KEY_FLAG, 1);

            if(flag == FLAG_ADD){
                //img_collection_delete.setVisibility(View.INVISIBLE);
                movieCollection = new MovieCollection();
                collectionList = (ArrayList<Movie>) bundle.getSerializable(KEY_COLLECTION_LIST);
            }else{
                //img_collection_delete.setVisibility(View.VISIBLE);
            }
        }

        initialize();
    }

    private void initialize(){
        movieService = RetrofitClient.getMovieService();

    }

    @OnClick(R.id.btn_collection_edit_ok)
    public void collectionEditOK(){

        if(StringUtils.isEmpty(ed_collection_title.getText())){
            Toast.makeText(getContext(), "컬렉션 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(flag == FLAG_ADD) {

            Map<String, String> data_movieCollectionADD = new HashMap<String, String>();
            data_movieCollectionADD.put("userCode", loginUser.getUserCode() + "");
            data_movieCollectionADD.put("collectionTitle", ed_collection_title.getText().toString());
            data_movieCollectionADD.put("publicState", (toggle_collection_public_state.getCheckedTogglePosition() == 0 ? "Y" : "N"));

            ArrayList<Movie> tmpCollectionList = new ArrayList<Movie>();
            for (Movie collectionItem: collectionList) {
                tmpCollectionList.add( new Movie(collectionItem.getMovieCode()) );
            }
            data_movieCollectionADD.put("collectionList", gson.toJson(tmpCollectionList));

            Call<ResponseData> call_movieCollectionADD = movieService.post("movieCollectionADD", data_movieCollectionADD);
            call_movieCollectionADD.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "컬렉션에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Log.e(TAG, " ");
                        Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseData> call, Throwable t) {
                    Log.e(TAG, "' (onFailure)");
                    Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            });

        }else{

        }

    }


    public void clickCollectionProfile(){
        Intent openGallery = new Intent(getContext(), GalleryType1Activity.class);
        openGallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT, false);
        openGallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, true);
        startActivityForResult(openGallery, REQ_GALLERY_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        switch (requestCode){

            case REQ_GALLERY_SELECT: // resultCode 0 은 리턴 값 없다?
                if(resultCode == Activity.RESULT_OK){
                    collectionProfileFileInfo = (HashMap<String, String>)data.getExtras().getSerializable("select");
                    Glide.with(getContext()).load(new File(collectionProfileFileInfo.get(GalleryFunction.KEY_PATH))).into(img_collection_profile);
                }
                break;
        }
        */
    }

    @OnClick(R.id.icon_back)
    public void back(){
        finish();
    }


}
