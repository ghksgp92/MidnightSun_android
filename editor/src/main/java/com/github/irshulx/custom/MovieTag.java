package com.github.irshulx.custom;

import android.graphics.Movie;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * app 프로젝트 내 com.hyunju.jin.movieTag.datamodel.MovieTag 클래스에서
 * 영화 태그를 만들기 위해 필요한 정보만 가지고 온 클래스
 */

public class MovieTag implements Serializable{

    // 서버 DB 'movieTag' 테이블에서 가져온 영화 정보를 담는 객체들
    @SerializedName("movieCode") private int movieCode;
    @SerializedName("movieTitle") private String movieTitle;
    @SerializedName("movieTitle_en") private String movieTitle_en;
    @SerializedName("poster") private String poster;
    @SerializedName("productionCountry") private String productionCountry;
    @SerializedName("runningTime") private int runningTime;
    @SerializedName("ageLimit") private String ageLimit;

    public MovieTag(){}

    public int getMovieCode() {
        return movieCode;
    }

    public void setMovieCode(int movieCode) {
        this.movieCode = movieCode;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieTitle_en() {
        return movieTitle_en;
    }

    public void setMovieTitle_en(String movieTitle_en) {
        this.movieTitle_en = movieTitle_en;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(int runningTime) {
        this.runningTime = runningTime;
    }

    public String getAgeLimit() {
        return ageLimit;
    }

    public void setAgeLimit(String ageLimit) {
        this.ageLimit = ageLimit;
    }

    public String getProductionCountry() {
        return productionCountry;
    }

    public void setProductionCountry(String productionCountry) {
        this.productionCountry = productionCountry;
    }
}
