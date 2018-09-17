package com.hyunju.jin.movie.activity.posting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

/**
 * Glide 이미지 라이브러리를 이용하여 HTML 의 <img> 태그로 된 이미지들을 TextView 에 불러온다.
 * 포스팅 글을 조회할 때 사용함.
 *
 * 현재 프로젝트 Glide 버전 4.7.1
 * 참고함. https://gist.github.com/extralam/017900f6eb616e9ae97eec9904dd90a6 > Glide 버전 3.x 대에서 작성한 코드.
 *
 * 나중에 공부할만한 주제. Glide 버전 4에서 변화한 점 : https://bumptech.github.io/glide/doc/migrating.html
 */
public class GlideImageGetter implements Html.ImageGetter {

    Context context;
    ArrayList<Target> imageTagList;
    final TextView targetTextView;

    public GlideImageGetter(Context context, TextView targetTextView){
        this.context = context;
        this.targetTextView = targetTextView;
        this.imageTagList = new ArrayList<Target>();
    }

    @Override
    public Drawable getDrawable(String url) {
        // Glide.with(context) 를 하면, 해당 context (Activity)가 사라질때 자동으로 메모리 해제.

        final UrlGlideDrawable urlDrawable = new UrlGlideDrawable();
        RequestBuilder<Bitmap> bitmapRequestBuilder = Glide.with(context).asBitmap().load(url);
        final Target target = new BitmapTarget(urlDrawable);
        imageTagList.add(target);
        bitmapRequestBuilder.into(target);

        return urlDrawable;
    }

    private class BitmapTarget extends SimpleTarget<Bitmap> {

        Drawable drawable;
        private final UrlGlideDrawable urlDrawable;

        public BitmapTarget(UrlGlideDrawable urlDrawable) {
            this.urlDrawable = urlDrawable;
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            drawable = new BitmapDrawable(context.getResources(), resource);

            targetTextView.post(new Runnable() {
                @Override
                public void run() {
                    int mTextViewWidth = targetTextView.getWidth();
                    int url_imageWidth = drawable.getIntrinsicWidth() ;
                    int url_imageHeight = drawable.getIntrinsicHeight();
                    int newHeight = url_imageHeight * ( mTextViewWidth ) / url_imageWidth;  // 왜 이렇게해?

                    Rect rect = new Rect( 0 , 0 , mTextViewWidth  , newHeight);
                    drawable.setBounds(rect);
                    urlDrawable.setBounds(rect);
                    urlDrawable.setDrawable(drawable);

                    targetTextView.setText(targetTextView.getText());
                    targetTextView.invalidate();
                }
            });
        }
    }

    class UrlGlideDrawable extends BitmapDrawable {
        private Drawable drawable;

        @SuppressWarnings("deprecation")
        public UrlGlideDrawable() {
        }
        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }
        public Drawable getDrawable() {
            return drawable;
        }
        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }

}
