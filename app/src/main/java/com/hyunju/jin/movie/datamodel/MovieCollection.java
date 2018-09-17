package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MovieCollection implements Serializable{

    // 서버 DB 'movieCollection' 테이블에서 가져온 정보를 담는 객체들
    @SerializedName("collectionCode") private int collectionCode;
    @SerializedName("collectionTitle") private String collectionTitle;
    @SerializedName("userCode") private int userCode;
    @SerializedName("publicState") private String publicState;

    @SerializedName("movieContainCheck") boolean movieContainCheck; // 영화 상세정보 화면 - 컬렉션 추가 시, 해당 영화가 컬렉션에 포함되어 있는 경우 true 값을 가진다.

    @SerializedName("collectionItemCount") private int collectionItemCount;
    @SerializedName("collectionItemTop1") private Movie collectionItemTop1;

    public Movie getCollectionItemTop1() {
        return collectionItemTop1;
    }

    public void setCollectionItemTop1(Movie collectionItemTop1) {
        this.collectionItemTop1 = collectionItemTop1;
    }

    public boolean isMovieContainCheck() {
        return movieContainCheck;
    }

    public void setMovieContainCheck(boolean movieContainCheck) {
        this.movieContainCheck = movieContainCheck;
    }

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public int getCollectionItemCount() {
        return collectionItemCount;
    }

    public void setCollectionItemCount(int collectionItemCount) {
        this.collectionItemCount = collectionItemCount;
    }

    public int getCollectionCode() {
        return collectionCode;
    }

    public void setCollectionCode(int collectionCode) {
        this.collectionCode = collectionCode;
    }

    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    public String getPublicState() {
        return publicState;
    }

    public void setPublicState(String publicState) {
        this.publicState = publicState;
    }
}
