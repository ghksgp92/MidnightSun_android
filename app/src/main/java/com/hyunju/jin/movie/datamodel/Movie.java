package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;
import com.hyunju.jin.movie.utils.DateFormatUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 */

public class Movie implements Serializable{

    // 서버 DB 'movie' 테이블에서 가져온 영화 정보를 담는
    @SerializedName("movieCode") private int movieCode; // 몇자리, 어떻게 생성되는지
    @SerializedName("movieTitle") private String movieTitle;
    @SerializedName("movieTitle_en") private String movieTitle_en;
    @SerializedName("poster") private String poster; //
    @SerializedName("productionCountry") private String productionCountry;
    @SerializedName("runningTime") private int runningTime;
    @SerializedName("ageLimit") private String ageLimit;
    @SerializedName("summary") private String summary;
    @SerializedName("streamingFile") private String streamingFile;
    @SerializedName("genre") private String genre;
    @SerializedName("releaseDate") private String releaseDate;

    @SerializedName("ranking") private int ranking;

    // 서버 DB의 director 테이블에서 가져온 감독 정보를 담는 객체
    @SerializedName("director") private Director director;

    // 특정 영화배우가 출연한 영화를 조회할 때, 영화배우의 배역정보를 담는 객체
    @SerializedName("actorCasting") private Casting actorCasting;

    // 서버 DB의 casting 테이블에서 가져온 배역 정보를 담는 객체
    @SerializedName("castingList") private ArrayList<Casting> castingList;

    // 영화 이어보기와 관련된 정보
    @SerializedName("playTime") private long playTime;  // 스트리밍 이어보기 시 시청한 시간을 저장한다. (밀리초)
    @SerializedName("duration") private long duration; // 영상 전체 시간 (밀리초)
    @SerializedName("lastWatchingTime") private String lastWatchingTime; // 마지막 시청 시간

    @SerializedName("ratingValue") private float ratingValue;   // 영화 예상 평점

    @SerializedName("pagination") private Pagination pagination;

    @SerializedName("existWantToWatch") private boolean existWantToWatch;   // 보고싶어요 컬렉션에 있는지 나타낸다.

    @SerializedName("watchedCount") private int watchedCount; // 사용자가 이 영화를 본 횟수를 나타낸다. 현재는 평점을 준 횟수 = 영화를 본 횟수라고 생각한다.

    public Movie(){}
    public Movie(String movieTitle){
        this.movieTitle = movieTitle;
    }
    public Movie(int movieCode){
        this.movieCode = movieCode;
    }
    public Movie(int movieCode, String movieTitle) {
        this.movieCode = movieCode;
        this.movieTitle = movieTitle;
    }

    public Movie(int movieCode, String movieTitle, String movieTitle_en, String poster, String streamingFile, long playTime, long duration) {    // 영화 스트리밍 이어보기 정보를 저장할 때 사용함
        this.movieCode = movieCode;
        this.movieTitle = movieTitle;
        this.movieTitle_en = movieTitle_en;
        this.poster = poster;
        this.streamingFile = streamingFile;
        this.playTime = playTime;
        this.duration = duration;
        this.lastWatchingTime = DateFormatUtils.getyyyyMMDDHHmm(); // 이어보기가 저장된 시각을 구한다.
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getProductionCountry() {
        return productionCountry;
    }

    public void setProductionCountry(String productionCountry) {
        this.productionCountry = productionCountry;
    }

    public String getLastWatchingTime() {
        return lastWatchingTime;
    }

    public void setLastWatchingTime(String lastWatchingTime) {
        this.lastWatchingTime = lastWatchingTime;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public String getStreamingFile() {
        return streamingFile;
    }

    public void setStreamingFile(String streamingFile) {
        this.streamingFile = streamingFile;
    }

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Director getDirector() {
        return director;
    }

    public void setDirector(Director director) {
        this.director = director;
    }

    public ArrayList<Casting> getCastingList() {
        return castingList;
    }

    public void setCastingList(ArrayList<Casting> castingList) {
        this.castingList = castingList;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public float getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(float ratingValue) {
        this.ratingValue = ratingValue;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public boolean isExistWantToWatch() {
        return existWantToWatch;
    }

    public void setExistWantToWatch(boolean existWantToWatch) {
        this.existWantToWatch = existWantToWatch;
    }

    public Casting getActorCasting() {
        return actorCasting;
    }

    public void setActorCasting(Casting actorCasting) {
        this.actorCasting = actorCasting;
    }

    public int getWatchedCount() {
        return watchedCount;
    }

    public void setWatchedCount(int watchedCount) {
        this.watchedCount = watchedCount;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
}
