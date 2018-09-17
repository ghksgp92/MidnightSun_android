package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 *
 */
public class Director implements Serializable {

    @SerializedName("directorCode") private int directorCode;
    @SerializedName("directorName") private String directorName;
    @SerializedName("directorName_en") private String directorName_en;
    @SerializedName("directorProfile") private String directorProfile;

    @SerializedName("count") private int count; // 감독으로 참여한 영화 수

    public int getDirectorCode() {
        return directorCode;
    }

    public void setDirectorCode(int directorCode) {
        this.directorCode = directorCode;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }

    public String getDirectorName_en() {
        return directorName_en;
    }

    public void setDirectorName_en(String directorName_en) {
        this.directorName_en = directorName_en;
    }

    public String getDirectorProfile() {
        return directorProfile;
    }

    public void setDirectorProfile(String directorProfile) {
        this.directorProfile = directorProfile;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
