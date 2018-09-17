package com.hyunju.jin.movie.datamodel;

import org.webrtc.VideoRenderer;

import java.math.BigInteger;

import computician.janusclientapi.IJanusPluginCallbacks;

/**
 * Janus 를 이용한 그룹 영상통화에서 사용하는 데이터 클래스.
 * 영상통화는 각 사용자가 자신의 Feed 를 생성하고 다른 사용자가 Feed 를 받아보는 형식으로 이루어진다.
 * 이 클래스는 다른 사용자의 Feed 정보를 저장하기 위해 만들었다.
 */
public class JanusFeed {

    private BigInteger feedID;  // Feed 구분 ID. 서버에서 자동으로 생성하기 때문에 이 코드로 사용자를 인식할 수는 없다.
    private String displayName; // 여기에선 사용자 아이디를 말한다.
    private User publisherUser; // Feed 를 생성한 사용자 정보. displayName 을 통해

    private VideoRenderer videoRenderer;    // 사용자의 영상통화 화면을 렌더링하는 객체

    private IJanusPluginCallbacks pluginCallbacks;  // Feed 와 연결 수립, 연결 해제 등의 이벤트를 처리하는 콜백 리스너

    /*
        생성자 정의
     */

    public JanusFeed(){}

    public JanusFeed(BigInteger feedID, String displayName, User publisherUser, VideoRenderer videoRenderer, IJanusPluginCallbacks pluginCallbacks){
        this.feedID = feedID;
        this.displayName = displayName;
        this.publisherUser = publisherUser;
        this.videoRenderer = videoRenderer;
        this.pluginCallbacks = pluginCallbacks;
    }


    /*
    getter, setter 정의
     */

    public BigInteger getFeedID() {
        return feedID;
    }

    public void setFeedID(BigInteger feedID) {
        this.feedID = feedID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public User getPublisherUser() {
        return publisherUser;
    }

    public void setPublisherUser(User publisherUser) {
        this.publisherUser = publisherUser;
    }

    public IJanusPluginCallbacks getPluginCallbacks() {
        return pluginCallbacks;
    }

    public void setPluginCallbacks(IJanusPluginCallbacks pluginCallbacks) {
        this.pluginCallbacks = pluginCallbacks;
    }

    public VideoRenderer getVideoRenderer() {
        return videoRenderer;
    }

    public void setVideoRenderer(VideoRenderer videoRenderer) {
        this.videoRenderer = videoRenderer;
    }
}
