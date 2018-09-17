package com.hyunju.jin.movie.activity.posting;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.irshulx.Editor;
import com.github.irshulx.custom.MovieTag;
import com.github.irshulx.custom.MovieTagListener;
import com.github.irshulx.models.EditorContent;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.movie.MovieDetailActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.PostingService;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 * 18.06.19 현재 글에 태그된 영화를 확인하고 다시 목록으로 돌아가면 에러 발생.
 * 내가 짠 코드에서 일어나는 일이 아니라 원인을 파악하는게 좀 어렵다. 근데 파악하면 큰 공부가 될 듯.
 */
public class PostingDetailActivity extends SuperActivity implements LoadingListener {

    public static final String DATA_KEY_POSTING = "posting"; // 조회할 포스팅 정보를 꺼내는 키?
    public static final int REQ_POSTING_COMMENT = 1000;      // 포스팅 댓글 화면 요청 코드. 댓글을 새로 입력했을 경우 댓글 수를 업데이트 하기위해 사용한다.

    @BindView(R.id.layout_container) CoordinatorLayout layouat_container;  // 화면의 최상위 레이아웃
    @BindView(R.id.loading_indicator) AVLoadingIndicatorView loading_indicator; // 로딩중임을 나타내는 뷰
    @BindView(R.id.layout_posting_edit_menu) LinearLayout layout_posting_edit_menu; // 현재 포스팅 작성자일 경우 수정/삭제 버튼을 보여준다.

    // 포스팅 글 내용을 나타내는 View 참조
    @BindView(R.id.tv_posting_title) TextView tv_posting_title;             // 제목
    @BindView(R.id.tv_posting_writer_id) TextView tv_posting_writer_id; // 작성자 이름
    @BindView(R.id.tv_posting_write_date) TextView tv_posting_write_date;   // 작성날짜
    @BindView(R.id.img_posting_writer_profile) CircularImageView img_posting_writer_profile;

    @BindView(R.id.ed_posting_contents) Editor ed_posting_contents;

    Posting posting;    // 현재 보고있는 포스팅 정보를 담은 객체
    PostingService postingService;    // 포스팅과 관련된 서버요청을 담당하는 객체 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting_detail);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            posting = (Posting) bundle.getSerializable(DATA_KEY_POSTING);
            if( posting == null || posting.getPostingCode() == 0){
                Toast.makeText(getContext(), "잘못된 요청입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        initialize();
    }

    private void initialize(){

        showLoading();
        postingService = RetrofitClient.getPostingService();

        HashMap<String, String> data_postingGET = new HashMap<String, String>();
        data_postingGET.put("userCode", loginUser.getUserCode()+"");
        data_postingGET.put("postingCode", posting.getPostingCode()+"");

        Call<Posting> call_postingGET = postingService.getPosting("postingGET", data_postingGET);
        call_postingGET.enqueue(new Callback<Posting>() {
            @Override
            public void onResponse(Call<Posting> call, Response<Posting> response) {
                if(response.isSuccessful()){

                    posting = response.body();
                    if(posting.getWriter() != null) {
                        User writer = posting.getWriter();
                        if (StringUtils.isNotEmpty(posting.getWriter().getProfileImg())) {
                            Glide.with(getContext()).load(RetrofitClient.WEB_SERVER + RetrofitClient.WEB_SERVER_PORT + "/images/loginUser/" + writer.getProfileImg()).into(img_posting_writer_profile);
                        }
                        tv_posting_writer_id.setText(writer.getId());
                    }
                    initializePostingInfo();

                }else{
                    Log.e(TAG, "포스팅 조회 실패");

                }

                hideLoading();
            }

            @Override
            public void onFailure(Call<Posting> call, Throwable t) {
                Log.e(TAG, "포스팅 조회 실패 (onFailure)");
                hideLoading();
            }
        });

    }

    /**
     * 서식 지정, 이미지 추가가 가능한 Editor를 설정한다.
     */
    private void initializePostingInfo(){

        tv_posting_title.setText(posting.getPostingTitle());
        tv_posting_writer_id.setText(posting.getWriter().getId());
        // 날짜 처리 어떻게 할래?
        tv_posting_write_date.setText(posting.getWriteDate());

        Map<Integer, String> headingTypeface = getHeadingTypeface();
        Map<Integer, String> contentTypeface = getContentface();
        ed_posting_contents.setHeadingTypeface(headingTypeface); // H1, H2, H3 에 적용되는 폰트 설정
        ed_posting_contents.setH1TextSize(28);   // 기본값 23
        ed_posting_contents.setH2TextSize(24);   // 기본값 18
        ed_posting_contents.setContentTypeface(contentTypeface); // 폰트설정? 꼭 필요한가 싶다.
        ed_posting_contents.setDividerLayout(R.layout.tmpl_divider_layout);
        ed_posting_contents.setEditorImageLayout(R.layout.tmpl_image_view);
        ed_posting_contents.setListItemLayout(R.layout.tmpl_list_item);
        ed_posting_contents.setMovieTagListener(new MovieTagListener() {
            @Override
            public void onUpload(MovieTag movieTag, String uuid) {
                // 포스팅 조회시에는 사용하지 않음.
            }

            @Override
            public void showMovie(MovieTag movieTag) {
                //메소드 이름 바꾸는게 좋겠음.
                Movie movie = new Movie();
                movie.setMovieCode(movieTag.getMovieCode());
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.DATA_KEY_MOVIE, movie);
                startActivity(intent);
            }
        });
        EditorContent Deserialized = ed_posting_contents.getContentDeserialized(posting.getPostingContents());
        ed_posting_contents.render(Deserialized);    // Editro 를 사용하기 위해 반드시 호출되어야함.
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


    @OnClick(R.id.toolbar_menu_comment)
    public void showPostingComment(){
        Intent postingComment = new Intent(getContext(), PostingCommentActivity.class);
        postingComment.putExtra(PostingCommentActivity.DATE_KEY_POSTING, posting);
        //startActivityForResult(postingComment, REQ_POSTING_COMMENT);
        startActivity(postingComment);
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
