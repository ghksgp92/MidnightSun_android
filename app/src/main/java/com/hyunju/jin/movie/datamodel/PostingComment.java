package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 포스팅 댓글 정보를 관리하는 클래스.
 */
public class PostingComment implements Serializable {

    // 서버 DB 의 postingComment 테이블 컬럼
    @SerializedName("postingCommentCode") private int postingCommentCode;
    @SerializedName("writeUserCode") private int writeUserCode;
    @SerializedName("postingCode") private int postingCode;
    @SerializedName("registerDate") private String registerDate;
    @SerializedName("postingCommentContent") private String postingCommentContent;

    // 댓글을 쓴 포스팅 정보
    @SerializedName("posting") private Posting posting;

    // 댓글을 쓴 사용자 정보
    @SerializedName("writer") private User writer;


    public PostingComment(){}
    public PostingComment(Posting posting){
        this.posting = posting;
    }

    public int getPostingCommentCode() {
        return postingCommentCode;
    }

    public void setPostingCommentCode(int postingCommentCode) {
        this.postingCommentCode = postingCommentCode;
    }

    public int getWriteUserCode() {
        return writeUserCode;
    }

    public void setWriteUserCode(int writeUserCode) {
        this.writeUserCode = writeUserCode;
    }

    public User getWriter() {
        return writer;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public int getPostingCode() {
        return postingCode;
    }

    public void setPostingCode(int postingCode) {
        this.postingCode = postingCode;
    }

    public String getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getPostingCommentContent() {
        return postingCommentContent;
    }

    public void setPostingCommentContent(String postingCommentContent) {
        this.postingCommentContent = postingCommentContent;
    }

    public Posting getPosting() {
        return posting;
    }

    public void setPosting(Posting posting) {
        this.posting = posting;
    }
}
