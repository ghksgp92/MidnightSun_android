package com.github.irshulx.custom;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.irshulx.Components.CustomEditText;
import com.github.irshulx.EditorCore;
import com.github.irshulx.R;
import com.github.irshulx.models.EditorControl;
import com.github.irshulx.models.EditorType;
import com.github.irshulx.models.RenderType;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MovieTagExtensions {

    private EditorCore editorCore;
    private int editorMovieTagLayout = R.layout.custom_movie_tag;       // 포스팅 내용에 추가된 영화 레이아웃 파일.
    private HashMap<String, MovieTag> movieTags;        // 포스팅에 추가된 이미지 파일 정보. key,

    public MovieTagExtensions(EditorCore editorCore) {
        this.editorCore = editorCore;
        this.movieTags = new HashMap<String, MovieTag>();
    }

    public HashMap<String, MovieTag> getMovieTags() {
        return movieTags;
    }

    public void setEditorMovieTagLayout(int editorMovieTagLayout) {
        this.editorMovieTagLayout = editorMovieTagLayout;
    }

    public void executeDownloadImageTask(String url, int index, String desc) {
        new MovieTagExtensions.DownloadImageTask(index).execute(url, desc);
    }

    /**
     * Editor 에 영화 태그를 추가한다.
     * (주의) 레이아웃이 바뀌면 이 메서드 구현을 수정해야함.
     *
     * @param movieTag
     */
    public void insertMovieTag(MovieTag movieTag, int index) {

        // 설정된 레이아웃으로 View 를 만든다.
        final View childLayout = ((Activity) editorCore.getContext()).getLayoutInflater().inflate(this.editorMovieTagLayout, null);

        // View 참조 선언
        ImageView img_movie_poster = (ImageView) childLayout.findViewById(R.id.img_movie_poster);     // 영화 포스터
        final TextView tv_movie_title = (TextView) childLayout.findViewById(R.id.tv_movie_title);
        final TextView tv_movie_title_en = (TextView) childLayout.findViewById(R.id.tv_movie_title_en);
        final TextView tv_productionCountry = (TextView) childLayout.findViewById(R.id.tv_productionCountry);
        final TextView tv_msg = (TextView) childLayout.findViewById(R.id.tv_msg);     // 상세페이지 안내 메시지
        tv_msg.setVisibility(View.GONE);

        Picasso.with(editorCore.getContext()).load(movieTag.getPoster()).into(img_movie_poster);    // 피카소 이미지 라이브러리를 이용해 이미지를 표시한다.
        tv_movie_title.setText(movieTag.getMovieTitle());
        tv_movie_title_en.setText(movieTag.getMovieTitle_en());
        tv_productionCountry.setText(movieTag.getProductionCountry());

        final String uuid = generateUUID(); // 이미지를 구분하기 위한 key 값을 만든다.
        if (index == -1) {  // 항상 -1이 넘어오는데, 그 외 값이 넘어오는 경우는 아직 파악 못함.
            index = editorCore.determineIndex(EditorType.img);
        }
        showNextInputHint(index);
        editorCore.getParentView().addView(childLayout, index);

        if (editorCore.isLastRow(childLayout)) {
            editorCore.getInputExtensions().insertEditText(index + 1, null, null);
        }
        EditorControl control = editorCore.createTag(EditorType.movieTag);
        control.path = uuid; // set the imageId,so we can recognize later after upload
        childLayout.setTag(control);
        if (editorCore.getRenderType() == RenderType.Editor) {
            BindEvents(childLayout);    // View 이벤트 설정
            editorCore.getMovieTagListener().onUpload(movieTag, uuid);  // 업로드 완료 이벤트 리스너 호출
        } else {

        }
    }

    /**
     * 뭔지 모르겠는데
     *
     * @param index
     */
    private void showNextInputHint(int index) {
        View view = editorCore.getParentView().getChildAt(index);
        EditorType type = editorCore.getControlType(view);
        if (type != EditorType.INPUT)
            return;
        TextView tv = (TextView) view;
        tv.setHint(editorCore.placeHolder);
    }

    private void hideInputHint(int index) {
        View view = editorCore.getParentView().getChildAt(index);
        EditorType type = editorCore.getControlType(view);
        if (type != EditorType.INPUT)
            return;

        String hint = editorCore.placeHolder;
        if (index > 0) {
            View prevView = editorCore.getParentView().getChildAt(index - 1);
            EditorType prevType = editorCore.getControlType(prevView);
            if (prevType == EditorType.INPUT)
                hint = null;
        }
        TextView tv = (TextView) view;
        tv.setHint(hint);
    }

    /**
     * yyyyMMddHHmmss 형식으로 key 값을 만든다. 이미지를 구분하는 키가 된다.
     *
     * @return
     */
    public String generateUUID() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String sdt = df.format(new Date(System.currentTimeMillis()));
        UUID x = UUID.randomUUID();
        String[] y = x.toString().split("-");
        return y[y.length - 1] + sdt;
    }

    /**
     * 저장된 데이터를 불러와 다시 영화 태그로 만든다.
     * @param movieTag
     */
    public void loadMovieTag(final MovieTag movieTag) {

        // 설정된 레이아웃으로 View 를 만든다.
        final View childLayout = ((Activity) editorCore.getContext()).getLayoutInflater().inflate(this.editorMovieTagLayout, null);

        // View 참조 선언
        ImageView img_movie_poster = (ImageView) childLayout.findViewById(R.id.img_movie_poster);     // 영화 포스터
        final TextView tv_movie_title = (TextView) childLayout.findViewById(R.id.tv_movie_title);
        final TextView tv_movie_title_en = (TextView) childLayout.findViewById(R.id.tv_movie_title_en);
        final TextView tv_productionCountry = (TextView) childLayout.findViewById(R.id.tv_productionCountry);
        final TextView tv_remove = (TextView) childLayout.findViewById(R.id.tv_remove);     // 영화태그 삭제 버튼
        final TextView tv_msg = (TextView) childLayout.findViewById(R.id.tv_msg);     // 상세페이지 안내 메시지

        final RelativeLayout layout_container = (RelativeLayout) childLayout.findViewById(R.id.layout_container);   // 영화태그 View 의 최상위 레이아웃

        Picasso.with(editorCore.getContext()).load(movieTag.getPoster()).into(img_movie_poster);    // 피카소 이미지 라이브러리를 이용해 이미지를 표시한다.
        tv_movie_title.setText(movieTag.getMovieTitle());
        tv_movie_title_en.setText(movieTag.getMovieTitle_en());
        tv_productionCountry.setText(movieTag.getProductionCountry());
        tv_remove.setVisibility(View.GONE);
        tv_msg.setVisibility(View.VISIBLE);
        layout_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editorCore.getMovieTagListener().showMovie( movieTag);
            }
        });

        editorCore.getParentView().addView(childLayout);
    }

    public View findImageById(String imageId) {
        for (int i = 0; i < editorCore.getParentChildCount(); i++) {
            View view = editorCore.getParentView().getChildAt(i);
            EditorControl control = editorCore.getControlTag(view);
            if (!TextUtils.isEmpty(control.path) && control.path.equals(imageId))
                return view;
        }
        return null;
    }

    /**
     * 영화 태그 추가가 성공할 경우 호출된다.
     *
     * @param movieTag
     * @param movieTagId
     */
    public void onPostUpload(MovieTag movieTag, String movieTagId) {

        View view = findImageById(movieTagId);
        EditorControl control = editorCore.createTag(EditorType.movieTag);
        control.path = movieTagId;
        view.setTag(control);

        movieTags.put(movieTagId, movieTag);
        // 영화 태그 목록에 태그 추가!
    }

    /*
      /used to fetch an image from internet and return a Bitmap equivalent
    */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private int InsertIndex;
        private String subTitle;

        public DownloadImageTask(int index) {
            this.InsertIndex = index;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            this.subTitle = urls[1];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            //insertMovieTag(result, this.InsertIndex,subTitle);
        }
    }

    /**
     * 포스팅 작성시에 영화 태그 내부의 이벤트 리스너 추가
     * 레이아웃이 바뀌면 수정 해야함.
     *
     * @param layout
     */
    private void BindEvents(final View layout) {
        final RelativeLayout layout_container = (RelativeLayout) layout.findViewById(R.id.layout_container);
        final TextView tv_remove = layout.findViewById(R.id.tv_remove);
        tv_remove.setVisibility(View.VISIBLE);

        // 이미지 삭제 이벤트
        tv_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = editorCore.getParentView().indexOfChild(layout);
                editorCore.getParentView().removeView(layout);
                hideInputHint(index);
                editorCore.getInputExtensions().setFocusToPrevious(index);

                EditorControl control = (EditorControl) layout.getTag();  // 삭제되는 View 의 구분코드를 구해서
                movieTags.remove(control.path); // 영화 태그 목록에서도 지운다.
            }
        });
    }
}
