package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Casting implements Serializable {

    @SerializedName("movieCode") private int movieCode;
    @SerializedName("actorCode") private String actorCode;
    @SerializedName("seq") private int seq;
    @SerializedName("castingType") private String castingType;
    @SerializedName("castingName") private String castingName;

    @SerializedName("actor") private Actor actor;

    public int getMovieCode() {
        return movieCode;
    }

    public void setMovieCode(int movieCode) {
        this.movieCode = movieCode;
    }

    public String getActorCode() {
        return actorCode;
    }

    public void setActorCode(String actorCode) {
        this.actorCode = actorCode;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getCastingType() {
        return castingType;
    }

    public void setCastingType(String castingType) {
        this.castingType = castingType;
    }

    public String getCastingName() {
        return castingName;
    }

    public void setCastingName(String castingName) {
        this.castingName = castingName;
    }

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }
}
