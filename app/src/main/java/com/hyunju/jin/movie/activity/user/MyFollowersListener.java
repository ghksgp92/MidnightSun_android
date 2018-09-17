package com.hyunju.jin.movie.activity.user;

import com.hyunju.jin.movie.datamodel.User;

public interface MyFollowersListener {

    /*
    이 인터페이스는 MyFollowersActivity 에서만 구현하므로, 메소드별 설명은 MyFollowersActivity.java 에 작성함.
     */
    boolean getCurrentSelectMode();
    void selectUserForVideoCall(int userCode, User user);
    boolean containsVideoCallUserList(int userCode);
}
