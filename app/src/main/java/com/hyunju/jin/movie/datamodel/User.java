package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 사용자 정보를 담는 클래스. 서버와 json 형태로 통신하기 위해 생성함.
 * 서버 데이터베이스 정보는 컬럼 이름으로 관리한다.
 */

public class User implements Serializable {

    @SerializedName("userCode") int userCode;
    @SerializedName("email") String email;
    @SerializedName("authType") String authType;
    @SerializedName("id") String id;
    @SerializedName("pwd1") String pwd1;    // 비밀번호
    @SerializedName("pwd2") String pwd2;    // 회원 가입 시 비밀번호 확인
    @SerializedName("profileImg") String profileImg;    // 사용자 프로필 사진 URL 주소

    @SerializedName("mateScore") private float mateScore;   // 취향보고서-추천친구 목록에서 취향 일치도를 나타낸다.

    @SerializedName("followingState") private boolean followingState;   // 현재 로그인한 사용자가 팔로잉하는 사용자라면 true, 아니라면 false
    // [마이페이지]-[팔로워목록]-[유저검색] 화면에서 검색 결과에 팔로잉 버튼 상태를 나타내기 위해 추가함.

    @SerializedName("fcmInstanceId") private String fcmInstanceId;  // FCM 메시징 기능을 사용하기 위한 FCM ID.

    public User(){}

    public User(int userCode, String id, String profileImg, String fcmInstanceId){
        this.userCode = userCode;
        this.id = id;
        this.profileImg = profileImg;
        this.fcmInstanceId = fcmInstanceId;
    }

    public int getUserCode() {
        return userCode;
    }

    public void setUserCode(int userCode) {
        this.userCode = userCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPwd1() {
        return pwd1;
    }

    public void setPwd1(String pwd1) {
        this.pwd1 = pwd1;
    }

    public String getPwd2() {
        return pwd2;
    }

    public void setPwd2(String pwd2) {
        this.pwd2 = pwd2;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public float getMateScore() {
        return mateScore;
    }

    public void setMateScore(float mateScore) {
        this.mateScore = mateScore;
    }

    public boolean isFollowingState() {
        return followingState;
    }

    public void setFollowingState(boolean followingState) {
        this.followingState = followingState;
    }

    public String getFcmInstanceId() {
        return fcmInstanceId;
    }

    public void setFcmInstanceId(String fcmInstanceId) {
        this.fcmInstanceId = fcmInstanceId;
    }
}
