package com.hyunju.jin.movie.activity.posting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.github.irshulx.Editor;
import com.github.irshulx.EditorListener;
import com.github.irshulx.custom.MovieTag;
import com.github.irshulx.custom.MovieTagListener;
import com.github.irshulx.custom.ServerUploadImageListener;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.nofolder.GalleryType2Activity;
import com.hyunju.jin.movie.activity.movie.MovieSearchResultListActivity;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.network.PostingService;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostingEditActivity extends SuperActivity {

    public static final String DATA_KEY_POSTING = "posting";
    public static final int REQ_SEARCH_MOVIE = 1;       // 영화 태그를 넣기 위해 영화 검색 요청

    @BindView(R.id.ed_posting_title) EditText ed_posting_title;
    @BindView(R.id.ed_posting_contents) Editor ed_posting_contents;

    Posting posting;                    // 현재 작성중인 포스팅 정보를 관리하는 객체
    PostingService postingService;      // 서버 통신 객체



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting_edit);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            posting = (Posting) bundle.getSerializable(DATA_KEY_POSTING);
            if( posting == null || StringUtils.isEmpty(posting.getRequestType()) ){
                Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

        }else{
            Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
        postingService = RetrofitClient.getPostingService();
        initializeEditor();
    }

    /**
     * 서식 지정, 이미지 추가가 가능한 Editor를 설정한다.
     */
    private void initializeEditor(){

        Map<Integer, String> headingTypeface = getHeadingTypeface();
        Map<Integer, String> contentTypeface = getContentface();
        ed_posting_contents.setHeadingTypeface(headingTypeface); // H1, H2, H3 에 적용되는 폰트 설정
        ed_posting_contents.setH1TextSize(28);   // 기본값 23
        ed_posting_contents.setH2TextSize(24);   // 기본값 18
        ed_posting_contents.setContentTypeface(contentTypeface); // 폰트설정? 꼭 필요한가 싶다.
        ed_posting_contents.setDividerLayout(R.layout.tmpl_divider_layout);
        ed_posting_contents.setEditorImageLayout(R.layout.tmpl_image_view);
        ed_posting_contents.setListItemLayout(R.layout.tmpl_list_item);

        ed_posting_contents.setEditorListener(new EditorListener() {

            /**
             * ed_posting_contents 안의 내용이 변경될때마다 호출됨.
             * @param editText
             * @param text
             */
            @Override
            public void onTextChanged(EditText editText, Editable text) {
                Log.e(TAG, "onTextChanged --- "+editText.toString());
            }

            /**
             * 이미지를 추가할때마다 호출됨
             * @param image
             * @param uuid
             */
            @Override
            public void onUpload(Bitmap image, String uuid) {
                // 이 메소드는 사용되지 않고 ServerUploadImageListener 가 사용됨.
            }
        });

        ed_posting_contents.setServerUploadImageListener(new ServerUploadImageListener() {
            @Override
            public void onUpload(HashMap<String, String> image, String uuid) {
                ed_posting_contents.onImageUploadForServerComplete(image, uuid);
                // 자동으로 커서위치로 내려가게 못하나요?

            }
        });
        ed_posting_contents.setMovieTagListener(new MovieTagListener() {
            @Override
            public void onUpload(MovieTag movieTag, String uuid) {
                ed_posting_contents.onMovieTagComplete(movieTag, uuid);
            }

            @Override
            public void showMovie(MovieTag movieTag) {
                // 포스팅 작성시에는 사용하지 않음. 메소드 이름 바꾸는게 좋겠음.
            }
        });
        ed_posting_contents.render();    // Editro 를 사용하기 위해 반드시 호출되어야함.
    }

    @OnClick(R.id.img_write)
    public void write(){

        String postingContents = ed_posting_contents.getContentAsSerialized();

        // 포스팅 제목
        if( StringUtils.isEmpty(ed_posting_title.getText().toString()) ){
            Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 포스팅 내용
        if( StringUtils.isEmpty(postingContents) ){
            Toast.makeText(getContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String postingContentsJson = ed_posting_contents.getContentAsSerialized();
        Log.e(TAG, postingContentsJson);
        posting.setPostingContents(postingContentsJson);
        posting.setPostingTitle(ed_posting_title.getText().toString());

        HashMap<String, RequestBody> map_postingUPDATE = new HashMap<String, RequestBody>();    // 서버에 전송할 데이터르 담는 리스트
        map_postingUPDATE.put("posting", RequestBody.create(MediaType.parse("text/plain"), gson.toJson(posting)));

        ArrayList<MovieTag> movieTags = new ArrayList<MovieTag>(ed_posting_contents.getMovieTagExtensions().getMovieTags().values());
        map_postingUPDATE.put("movieTags", RequestBody.create(MediaType.parse("text/plain"), gson.toJson(movieTags)));
        // 첨부된 영화데이터도 넣을 것.

        ArrayList<MultipartBody.Part> file_postingUPDATE = new ArrayList<MultipartBody.Part>(); // 서버에 전송할 파일을 담는 리스트
        HashMap<String, HashMap<String, String>> images = ed_posting_contents.getServerUploadImageExtensions().getImages();
        // key 값이 시간을 기반으로 만들어서 정렬 상태로 반복문을 도는 것 같다. 별도의 정렬 작업은 안해도 될듯.

        File photoToFile = null;
        Iterator<String> keys = images.keySet().iterator();
        while (keys.hasNext()){
            HashMap<String, String> image = images.get(keys.next());
            photoToFile = new File( image.get(GalleryFunction.KEY_PATH) );
            if(photoToFile.exists()){
                RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), photoToFile);
                file_postingUPDATE.add(MultipartBody.Part.createFormData("contentsImages[]", photoToFile.getName(), reqFile));
            }else{
                Log.e(TAG, photoToFile.getPath()+" 파일이 존재하지 않습니다.");
            }
        }

        Call<ResponseData> call_postingUPDATE = postingService.postMultiPart("postingUPDATE", map_postingUPDATE, file_postingUPDATE);
        call_postingUPDATE.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                if(response.isSuccessful()){
                    ResponseData responseData = response.body();
                    if(responseData.getCode() == ResponseData.RESPONSE_OK){
                        Log.e(TAG, "포스팅 업데이트 성공");
                        Intent posting = new Intent(getContext(), PostingDetailActivity.class);
                        Posting uploadPosting = new Posting();
                        uploadPosting.setPostingCode( Integer.parseInt(responseData.getMsg()));
                        posting.putExtra(PostingDetailActivity.DATA_KEY_POSTING, uploadPosting);
                        startActivity(posting);
                        finish();
                    }else{
                        Log.e(TAG, "포스팅 업데이트");
                        Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Log.e(TAG, "포스팅 업데이트");
                    Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseData> call, Throwable t) {
                Log.e(TAG, "포스팅 업데이트 (onFailure)");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private Map<Integer, String> getHeadingTypeface() {
        Map<Integer, String> typefaceMap = new HashMap<>();
        typefaceMap.put(Typeface.NORMAL, "fonts/geycliffcf_bold.ttf");
        typefaceMap.put(Typeface.BOLD, "fonts/greycliffcf_heavy.ttf");
        typefaceMap.put(Typeface.ITALIC, "fonts/greycliffcf_heavy.ttf");
        typefaceMap.put(Typeface.BOLD_ITALIC, "fonts/geycliffcf_bold.ttf");
        return typefaceMap;
    }

    private Map<Integer, String> getContentface() {
        Map<Integer, String> typefaceMap = new HashMap<>();
        typefaceMap.put(Typeface.NORMAL, "fonts/lato_medium.ttf");
        typefaceMap.put(Typeface.BOLD, "fonts/lato_bold.ttf");
        typefaceMap.put(Typeface.ITALIC, "fonts/lato_mediumitalic.ttf");
        typefaceMap.put(Typeface.BOLD_ITALIC, "fonts/lato_bolditalic.ttf");
        return typefaceMap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ed_posting_contents.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ed_posting_contents.insertImage(bitmap);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else if (requestCode == ed_posting_contents.PICK_IMAGE_REQUEST_SERVER_UPLOAD && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<Gallery> selectImages = (ArrayList<Gallery>) data.getSerializableExtra(GalleryType2Activity.DATA_KEY_SELECT_LIST);

            for (Gallery image : selectImages){
                ed_posting_contents.insertImageForServerUpload(image.getMedia());
            }
        }else if(requestCode == REQ_SEARCH_MOVIE && resultCode == RESULT_OK && data != null){

            Movie movie = (Movie) data.getSerializableExtra(MovieSearchResultListActivity.DATA_KEY_SELECT);

            MovieTag movieTag = new MovieTag();
            movieTag.setMovieCode(movie.getMovieCode());
            movieTag.setMovieTitle(movie.getMovieTitle());
            movieTag.setMovieTitle_en(movie.getMovieTitle_en());
            movieTag.setPoster(movie.getPoster());

            ed_posting_contents.insertMovieTag(movieTag);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.action_hr)
    public void actionHR(){
        ed_posting_contents.insertDivider();
    }

    @OnClick(R.id.action_insert_image)
    public void actionInsertImage(){
        Intent gallery = new Intent(getContext(), GalleryType2Activity.class);
        gallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, true);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT, true);
        gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT_MAX, 3);
        startActivityForResult(gallery, ed_posting_contents.PICK_IMAGE_REQUEST_SERVER_UPLOAD);
    }

    @OnClick(R.id.img_add_movie)
    public void actionInsertMovie(){
        Intent movieSearch = new Intent(getContext(), MovieSearchResultListActivity.class);
        movieSearch.putExtra(MovieSearchResultListActivity.DATA_KEY_CALL, "posting");
        startActivityForResult(movieSearch, REQ_SEARCH_MOVIE);
    }

}
