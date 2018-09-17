package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Actor implements Serializable {

    @SerializedName("actorCode") private int actorCode;
    @SerializedName("actorName") private String actorName;
    @SerializedName("actorName_en") private String actorName_en;
    @SerializedName("actorProfile") private String actorProfile;

    @SerializedName("count") private int count;

    public int getActorCode() {
        return actorCode;
    }

    public void setActorCode(int actorCode) {
        this.actorCode = actorCode;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorName_en() {
        return actorName_en;
    }

    public void setActorName_en(String actorName_en) {
        this.actorName_en = actorName_en;
    }

    public String getActorProfile() {
        return actorProfile;
    }

    public void setActorProfile(String actorProfile) {
        this.actorProfile = actorProfile;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
