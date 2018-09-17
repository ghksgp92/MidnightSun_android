package com.hyunju.jin.movie.activity.posting;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.LoadingListener;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.adapter.posting.PostingCommentAdapter;
import com.hyunju.jin.movie.datamodel.Posting;
import com.hyunju.jin.movie.datamodel.PostingComment;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.network.PostingService;
import com.hyunju.jin.movie.network.RetrofitClient;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostingCommentActivity extends SuperActivity implements LoadingListener, PostingCommentListener {

    public static final String DATE_KEY_POSTING = "posting";

    PostingComment postingComment;
    PostingService postingService;

    @BindView(R.id.layout_container) LinearLayout layout_container;

    @BindView(R.id.tv_posting_comment_count) TextView tv_posting_comment_count; // 댓글 수를 나타내는 TextView 참조
    @BindView(R.id.ed_posting_comment_input) EditText ed_posting_comment_input; // 댓글 내용을 입력하는 EditText 참조

    // 댓글을 보여주는 뷰, 관련 객체
    @BindView(R.id.recycler_posting_comment) RecyclerView recycler_posting_comment;
    PostingCommentAdapter postingCommentAdapter;
    ArrayList<PostingComment> postingCommentList;
    @BindView(R.id.layout_posting_comment_empty) LinearLayout layout_posting_comment_empty;

    SpannableString commentContentSpannable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting_comment);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            postingComment = new PostingComment( (Posting) bundle.getSerializable(DATE_KEY_POSTING));
            if(postingComment.getPosting() == null || postingComment.getPosting().getPostingCode() == 0){
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "포스팅 정보 없음");
                finish();
            }

        }else{
            Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "포스팅 정보 없음");
            finish();
        }

        initialize();
    }

    /**
     * 서버에 포스팅 댓글을 요청해 화면에 표시한다.
     */
    private void loadPostingCommet(){
        // 서버에서 댓글 불러오기
        HashMap<String, String> data_postingCommentListOrderBy= new HashMap<String, String>();
        data_postingCommentListOrderBy.put("postingCode", postingComment.getPosting().getPostingCode()+"");
        //data_postingCommentListOrderBy.put("userCode", loginUser.getUserCode()+"");

        Call<ArrayList<PostingComment>> call_postingCommentListOrderBy = postingService.getPostingCommentList("postingCommentListOrderBy", data_postingCommentListOrderBy);
        call_postingCommentListOrderBy.enqueue(new Callback<ArrayList<PostingComment>>() {
            @Override
            public void onResponse(Call<ArrayList<PostingComment>> call, Response<ArrayList<PostingComment>> response) {
                if(response.isSuccessful()){

                    postingCommentList.addAll(response.body());
                    tv_posting_comment_count.setText(postingCommentList.size()+"");
                    postingCommentAdapter.notifyDataSetChanged();

                    if(postingCommentList.size() == 0){
                        layout_posting_comment_empty.setVisibility(View.VISIBLE);
                    }else{
                        layout_posting_comment_empty.setVisibility(View.GONE);
                    }

                    return;
                }

                // 서버 요청 결과가 실패한 경우
                Log.e(TAG, "포스팅 댓글로드 실패");
            }

            @Override
            public void onFailure(Call<ArrayList<PostingComment>> call, Throwable t) {
                Log.e(TAG, "포스팅 댓글로드 실패 (onFailure)");
            }
        });

    }

    private void initialize(){

        postingService = RetrofitClient.getPostingService();
        postingComment.setWriteUserCode(loginUser.getUserCode());
        commentContentSpannable = new SpannableString(ed_posting_comment_input.getText().toString());

        // 댓글을 보여주는 리사이클러 뷰 준비
        postingCommentList = new ArrayList<PostingComment>();
        postingCommentAdapter = new PostingCommentAdapter(getContext(), postingCommentList, this);
        recycler_posting_comment.setAdapter(postingCommentAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //layoutManager.setStackFromEnd(true);
        recycler_posting_comment.setHasFixedSize(false);
        recycler_posting_comment.setLayoutManager(layoutManager);

        loadPostingCommet();

        addListenerOnView();
    }

    private void addListenerOnView(){

        ed_posting_comment_input.setFilters( new InputFilter[] { new InputFilter(){

            /**
             * 댓글입력창에 @ 문자가 들어갈 수 없도록 필터링 조건을 만든다.
             * @param source
             * @param start
             * @param end
             * @param dest
             * @param dstart
             * @param dend
             * @return
             */
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile(".*[^@].*$");
                if (!ps.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        }});

        ed_posting_comment_input.addTextChangedListener(new TextWatcher() {

            ArrayList<ForegroundColorSpan> spansToRemove = new ArrayList<>();

            /**
             * 텍스트가 변경되기 직전 호출된다.
             * @param s      입력 전 텍스트
             * @param start  입력 커서 위치
             * @param count  커서 위치로부터 count 수 만큼의 글자가 대체된다. 0이라면 글자만 추가된다.
             * @param after  추가되는 글자 수. 글자를 지울 경우엔 0이 넘어온다.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                /*// count가 0보다 크다는 것은, 글자가 지워진다는 뜻
                if (count > 0 && ed_posting_comment_input.getText() != null) {
                    Editable text = ed_posting_comment_input.getText();
                    int end = start + count;

                    //If we're deleting a space, we want spans from 1 character before this start
                    if ((text.length() >= (start + 1)) && text.charAt(start) == ' ') {
                        start -= 1;
                    }

                    ForegroundColorSpan[] foregroundColorSpans = text.getSpans(start, end, ForegroundColorSpan.class);

                    //NOTE: I'm not completely sure this won't cause problems if we get stuck in a text changed loop
                    //but it appears to work fine. Spans will stop getting removed if this breaks.
                    ArrayList<ForegroundColorSpan> spansToRemove = new ArrayList<>();
                    for (ForegroundColorSpan span : foregroundColorSpans) {
                        if (text.getSpanStart(span) < end && start < text.getSpanEnd(span)) {
                            spansToRemove.add(span);
                        }
                    }
                    this.spansToRemove = spansToRemove;
                }*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 입력한 내용이 바뀔때마다 Spannable 객체를 업데이트 한다.

               /* ArrayList<ForegroundColorSpan> spansCopy = new ArrayList<>(spansToRemove);
                spansToRemove.clear();
                for (ForegroundColorSpan span : spansCopy) {
                    int spanStart = s.getSpanStart(span);
                    int spanEnd = s.getSpanEnd(span);

                    s.removeSpan(span);

                    //The end of the span is the character index after it
                    spanEnd--;

                    s.delete(spanStart, spanEnd);

                }*/

                commentContentSpannable = new SpannableString(s);

            }

            private boolean isSplitChar(char c) {
                char[] splitChar = {'@'};
                for (char split : splitChar) {
                    if (c == split) return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.btn_add_posting_comment)
    public void completePostingComment(){
        if(StringUtils.isEmpty(ed_posting_comment_input.getText().toString()) ){

            return;
        }

        postingComment.setPostingCommentContent(ed_posting_comment_input.getText().toString());

        HashMap<String, String> data_postingCommentUPDATE = new HashMap<String, String>();
        data_postingCommentUPDATE.put("postingComment", gson.toJson(postingComment));
        data_postingCommentUPDATE.put("userCode", loginUser.getUserCode()+"");

        Call<PostingComment> call_postingCommentUPDATE = postingService.postingCommentUPDATE("postingCommentUPDATE", data_postingCommentUPDATE);
        call_postingCommentUPDATE.enqueue(new Callback<PostingComment>() {
            @Override
            public void onResponse(Call<PostingComment> call, Response<PostingComment> response) {
                if (response.isSuccessful()) {
                    PostingComment responseData = response.body();

                    loadPostingCommet();

                    // 이렇게 방금 등록한 애만 받아서 업데이트하면, 내가 댓글쓰는동안 달린 다른 새로운 댓글은 못봐. 그냥 서버에 한번 새로 요청하는게 맞긴한데..
                    // 페이징 적용했을때를 처리할 생각이 안떠올라서.. 우선 서버 요청을 하자.


                   /* if(responseData.getCode() > 0){ // 댓글 등록에 성공하면 code 값으로 댓글 코드가 리턴된다.
                        postingComment.setPostingCommentCode( responseData.getCode() );
                        postingComment.setUser
                        postingCommentList.add(postingComment);

                    } // 댓글 등록 실패 시 code == -1 이 리턴된다.*/
                    postingComment = new PostingComment(postingComment.getPosting());   // 댓글 정보 초기화
                    ed_posting_comment_input.setText("");
                    ed_posting_comment_input.setSelection(ed_posting_comment_input.length());
                    postingCommentAdapter.notifyDataSetChanged();
                    return;
                }
                // 서버 요청 결과가 실패한 경우
                Log.e(TAG, "포스팅 댓글 작성 실패");
            }

            @Override
            public void onFailure(Call<PostingComment> call, Throwable t) {
                Log.e(TAG, "포스팅 댓글로드 작성 (onFailure)");
            }
        });

    }

    @OnClick(R.id.icon_menu_close)
    public void back(){
        //Intent returnIntent = new Intent();
        //setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    /**
     * 댓글을 클릭할 경우, 해당 댓글의 작성자를 태그하도록 한다.
     * @param addUser
     */
    @Override
    public void addUserTag(User addUser) {

        // 작성자 정보가 올바른지 확인한다.
        if(addUser == null || addUser.getUserCode() == 0){ return; }

        // 클릭한 댓글이 자신의 댓글인지 확인한다. 자신의 댓글이라면, 태그로 추가하지 않는다.
        if(loginUser.getUserCode() == addUser.getUserCode()){ return; }

        // 이미 태그에 추가된 사용자인지 확인한다.
        if(userHashTags.get(loginUser.getUserCode()) != null) { return; }
        /*StyleSpan[] styleSpans = commentContentSpannable.getSpans(0, ed_posting_comment_input.length(), StyleSpan.class);

        // 선택한 텍스트에 굵게, 기울게 스타일이 적용됬는지 확인한다.
        for (int i = 0; i < styleSpans.length; i++) {
            if (styleSpans[i].getStyle() == Typeface.BOLD) {
              *//*  if( loginUser.getId().equals(styleSpans[i].toString()) ){
                    return;
                }*//*
            }
        }*/

        // 작성자 ID를 '@작성자ID' 형태로 댓글입력창에 추가한다.
        String userTag = "@"+addUser.getId();
        commentContentSpannable = new SpannableString(commentContentSpannable + userTag);
        String tmp = commentContentSpannable.toString();

        //commentContentSpannable.setSpan((new StyleSpan(Typeface.BOLD)),  tmp.indexOf(userTag), userTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        commentContentSpannable.setSpan( (new ForegroundColorSpan(Color.rgb(31, 158, 180))),  tmp.indexOf(userTag), tmp.indexOf(userTag) + userTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ed_posting_comment_input.setText(commentContentSpannable);
        ed_posting_comment_input.setSelection(ed_posting_comment_input.length());
        ed_posting_comment_input.findFocus();
        // 추가한 ID가 사용자 태그인것을 표시한다.
        userHashTags.put(loginUser.getUserCode(), loginUser);

    }

    HashMap<Integer, User> userHashTags = new HashMap<Integer, User>();

}
