/*
 * Copyright (C) 2016 Muhammed Irshad
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.irshulx;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import com.github.irshulx.Components.CustomEditText;
import com.github.irshulx.custom.MovieTag;
import com.github.irshulx.models.EditorTextStyle;
import com.github.irshulx.models.EditorContent;
import com.github.irshulx.models.RenderType;

import java.util.HashMap;
import java.util.Map;

public class Editor extends EditorCore {
    public Editor(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setEditorListener(null);
        //  initialize(context,parentView,renderType,_PlaceHolderText);
    }

    public void setEditorListener(EditorListener _listener) {
        super.setEditorListener(_listener);
    }

    public EditorContent getContent() {
        return super.getContent();
    }

    public String getContentAsSerialized() {
        return super.getContentAsSerialized();
    }

    public String getContentAsSerialized(EditorContent state) {
        return super.getContentAsSerialized(state);
    }

    public EditorContent getContentDeserialized(String EditorContentSerialized) {
        return super.getContentDeserialized(EditorContentSerialized);
    }

    public String getContentAsHTML() {
        return getHtmlExtensions().getContentAsHTML();
    }

    public String getContentAsHTML(EditorContent content) {
        return getHtmlExtensions().getContentAsHTML(content);
    }

    public String getContentAsHTML(String editorContentAsSerialized) {
        return getHtmlExtensions().getContentAsHTML(editorContentAsSerialized);
    }

    public void render(EditorContent _state) {
        super.renderEditor(_state);
    }

    public void render(String HtmlString) {
        renderEditorFromHtml(HtmlString);
    }

    public void render() {
        if (getRenderType() == RenderType.Editor) {
            getInputExtensions().insertEditText(0, this.placeHolder, null);
        }
    }

    private void restoreState() {
        EditorContent state = getStateFromString(null);
        render(state);
    }

    public void clearAllContents() {
        super.clearAllContents();
        if (getRenderType() == RenderType.Editor) {
            getInputExtensions().insertEditText(0, this.placeHolder, null);
        }
    }
//
//    @Override
//    public int removeParent(View view) {
//        int indexOfDeleteItem = super.removeParent(view);
//        if (indexOfDeleteItem < 0)
//            render();
//        return indexOfDeleteItem;
//    }

    //region Miscellanious getters and setters

    /*
     *
     * Input extension
     *
     */
    public int getH1TextSize() {
        return getInputExtensions().getH1TextSize();
    }

    /**
     * size in sp
     * @param size
     */
    public void setH1TextSize(int size) {
        getInputExtensions().setH1TextSize(size);
    }

    public int getH2TextSize() {
        return getInputExtensions().getH2TextSize();
    }

    /**
     * size in sp
     * @param size
     */
    public void setH2TextSize(int size) {
        getInputExtensions().setH2TextSize(size);
    }

    public int getH3TextSize() {
        return getInputExtensions().getH3TextSize();
    }

    /**
     * size in sp
     * @param size
     */
    public void setH3TextSize(int size) {
        getInputExtensions().setH3TextSize(size);
    }

    /**
     * size in sp
     * @param size
     */
    public void setNormalTextSize(int size){
        getInputExtensions().setNormalTextSize(size);
    }

    /**
     * Set the fontface for the editor
     *
     * @deprecated use {@link #setContentTypeface(Map)} and {@link #setHeadingTypeface(Map)} (Map)} ()} instead.
     */
    @Deprecated
    public void setFontFace(int StringResource) {
        getInputExtensions().setFontFace(StringResource);
    }


    public void updateTextStyle(EditorTextStyle style) {
        getInputExtensions().UpdateTextStyle(style, null);
    }

    public void insertLink() {
        getInputExtensions().insertLink();
    }

    public void insertLink(String link) {
        getInputExtensions().insertLink(link);
    }


    /**
     * setup the fontfaces for editor content
     * For eg:
     * Map<Integer, String> typefaceMap = new HashMap<>();
     * typefaceMap.put(Typeface.NORMAL,"fonts/greycliffcf-medium.ttf");
     * typefaceMap.put(Typeface.BOLD,"fonts/geycliffcf-bold.ttf");
     * typefaceMap.put(Typeface.ITALIC,"fonts/greycliffcf-medium.ttf");
     * typefaceMap.put(Typeface.BOLD_ITALIC,"fonts/greycliffcf-medium.ttf");
     *
     * @param map
     */

    public void setContentTypeface(Map<Integer, String> map) {
        getInputExtensions().setContentTypeface(map);
    }

    public Map<Integer, String> getContentTypeface() {
        return getInputExtensions().getContentTypeface();
    }

    /**
     * setup the fontfaces for editor heding tags (h1,h2,h3)
     * for Eg:
     * Map<Integer, String> typefaceMap = new HashMap<>();
     * typefaceMap.put(Typeface.NORMAL,"fonts/greycliffcf-medium.ttf");
     * typefaceMap.put(Typeface.BOLD,"fonts/geycliffcf-bold.ttf");
     * typefaceMap.put(Typeface.ITALIC,"fonts/greycliffcf-medium.ttf");
     * typefaceMap.put(Typeface.BOLD_ITALIC,"fonts/greycliffcf-medium.ttf");
     *
     * @param map
     */
    public void setHeadingTypeface(Map<Integer, String> map) {
        getInputExtensions().setHeadingTypeface(map);
    }

    public Map<Integer, String> getHeadingTypeface() {
        return getInputExtensions().getHeadingTypeface();
    }


        /*
         *
         * Divider extension
         *
         */

    public void setDividerLayout(int layout) {
        this.getDividerExtensions().setDividerLayout(layout);
    }

    public void insertDivider() {
        getDividerExtensions().insertDivider();
    }

        /*
         *
         * Image Extension
         *
         */

    public void setEditorImageLayout(int layout) {
        this.getImageExtensions().setEditorImageLayout(layout);
    }

    public void openImagePicker() {
        getImageExtensions().openImageGallery();
    }

    /**
     * 이미지를 1장 받아서 Editor에 추가한다.
     * 이 메서드를 사용할 경우, 추가한 이미지 파일 정보는 관리하지 않는다.
     * @param bitmap
     */
    public void insertImage(Bitmap bitmap) {
        getImageExtensions().insertImage(bitmap, -1,null);
    }

    public void onImageUploadComplete(String url, String imageId) {
        getImageExtensions().onPostUpload(url, imageId);
    }

    public void onImageUploadFailed(String imageId) {
        getImageExtensions().onPostUpload(null, imageId);
    }
    /*
     *
     *List Item extension
     *
     */
    public void setListItemLayout(int layout) {
        this.getListItemExtensions().setListItemTemplate(layout);
    }

    public void insertList(boolean isOrdered) {
        this.getListItemExtensions().insertlist(isOrdered);
    }


        /*
         *
         * Map Extension
         *
         */

    public void setMapViewLayout(int layout) {
        this.getMapExtensions().setMapViewTemplate(layout);
    }

    public void insertMap() {
        getMapExtensions().loadMapActivity();
    }

    public void insertMap(String Cords) {
        getMapExtensions().insertMap(Cords, null, true);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event, CustomEditText editText) {
        boolean onKey = super.onKey(v, keyCode, event, editText);
        if (getParentChildCount() == 0)
            render();
        return onKey;
    }

    /* ***************************************************************** */
    /* 기본 모듈에서 커스텀한 코드 영역
    /* ***************************************************************** */

    /* ***************************************************************** */
    /* 서버에 이미지를 올릴 수 있도록 커스텀한 부분과 관련된 코드
    /* ***************************************************************** */

    /**
     * Editor 에 이미지를 추가할 때 호출한다.
     * 이 메소드를 호출해서 이미지를 추가할 경우, 추가한 이미지들을 Extensions 내부에서 HashMap 으로 관리한다.
     * @param media
     */
    public void insertImageForServerUpload(HashMap<String, String> media){
        getServerUploadImageExtensions().insertImage(media, -1);
    }

    /**
     * insertImageForServerUpload 로 이미지 추가를 요청했을 때 이미지 추가가 완료되면 호출한다.
     * @param image     추가하는 이미지 파일 정보. key 값 path 로 파일 경로를 사용할 수 있다.
     * @param uuid      이미지 구분 코드. 추가한 시간으로 생성하기 때문에 같은 이미지를 올려도 문제 없음.
     */
    public void onImageUploadForServerComplete(HashMap<String, String> image, String uuid) {
        getServerUploadImageExtensions().onPostUpload(image, uuid);
    }

    /* ***************************************************************** */
    /* 영화 태그 코드
    /* ***************************************************************** */


    public void insertMovieTag(MovieTag movieTag){
        getMovieTagExtensions().insertMovieTag(movieTag, -1);
    }

    public void onMovieTagComplete(MovieTag movieTag, String uuid){
        getMovieTagExtensions().onPostUpload(movieTag, uuid);
    }
}
