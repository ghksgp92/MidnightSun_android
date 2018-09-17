package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 사용자의 영화 취향보고서에서 사용할 데이터 형태를 정의함.
 */
public class MovieReportData implements Serializable {

    // '별점분포' 보고서에서 사용하는 데이터
    @SerializedName("ratingCategory") private String ratingCategory;
    @SerializedName("count") private int count;

    // 태그 클라우드
    @SerializedName("tag") private String tag;

    // '추천친구'
    @SerializedName("mateUser") private User mateUser;

    public String getRatingCategory() {
        return ratingCategory;
    }

    public void setRatingCategory(String ratingCategory) {
        this.ratingCategory = ratingCategory;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public User getMateUser() {
        return mateUser;
    }

    public void setMateUser(User mateUser) {
        this.mateUser = mateUser;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
