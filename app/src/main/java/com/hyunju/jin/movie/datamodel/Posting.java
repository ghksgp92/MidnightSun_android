package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Posting implements Serializable {

    public static final String DATA_VAL_REQ_TYPE_ADD = "ADD";
    public static final String DATA_VAL_REQ_TYPE_UPDATE = "UPDATE";

    // 서버 DB posting 테이블에 있는 값을 나타내는 변수
    @SerializedName("postingCode") private int postingCode;
    @SerializedName("postingTitle") private String postingTitle;
    @SerializedName("postingContents") private String postingContents;
    @SerializedName("writeDate") private String writeDate;

    // 서버와 통신할때 리턴받는 값
    @SerializedName("canBeModify") private boolean canBeModify; // 해당 포스팅이 자신의 글인지 확인한다. true 면 자신의 글이다.

    // 포스팅 작성/수정/삭제 등의 서버 작업 요청을 나타내는 변수
    @SerializedName("requestType") private String requestType;  // ADD : 포스팅 작성, UPDATE : 수정, DELETE : 삭제

    @SerializedName("writer") private User writer;      // 작성자 정보
    @SerializedName("postingMovieTagList") private ArrayList<Movie> postingMovieTagList;        // 포스팅에 태그된 영화 정보
    @SerializedName("postingMediaList") private ArrayList<Movie> postingMediaList;  // 포스팅에 첨부된 이미지 정보
    @SerializedName("postingContentsText") private String postingContentsText;

    public Posting(){
    }

    /**
     * Posting 작성, 수정 요청 시 사용하기 위해 만든 생성자
     * @param requestType
     * @param writer
     */
    public Posting(String requestType, User writer){
        this.requestType = requestType;
        this.writer = writer;
    }

    public String getPostingContentsText() {
        return postingContentsText;
    }

    public void setPostingContentsText(String postingContentsText) {
        this.postingContentsText = postingContentsText;
    }

    public boolean isCanBeModify() {
        return canBeModify;
    }

    public void setCanBeModify(boolean canBeModify) {
        this.canBeModify = canBeModify;
    }

    public ArrayList<Movie> getPostingMediaList() {
        return postingMediaList;
    }

    public void setPostingMediaList(ArrayList<Movie> postingMediaList) {
        this.postingMediaList = postingMediaList;
    }

    public int getPostingCode() {
        return postingCode;
    }

    public void setPostingCode(int postingCode) {
        this.postingCode = postingCode;
    }

    public String getPostingTitle() {
        return postingTitle;
    }

    public void setPostingTitle(String postingTitle) {
        this.postingTitle = postingTitle;
    }

    public String getPostingContents() {
        return postingContents;
    }

    public void setPostingContents(String postingContents) {
        this.postingContents = postingContents;
    }

    public String getWriteDate() {
        return writeDate;
    }

    public void setWriteDate(String writeDate) {
        this.writeDate = writeDate;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public User getWriter() {
        return writer;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public ArrayList<Movie> getPostingMovieTagList() {
        return postingMovieTagList;
    }

    public void setPostingMovieTagList(ArrayList<Movie> postingMovieTagList) {
        this.postingMovieTagList = postingMovieTagList;
    }
}
