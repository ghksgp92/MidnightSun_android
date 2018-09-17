package com.hyunju.jin.movie.activity.gallery.nofolder;

/**
 * ImageFilterActivity 내부의 Adapter에서 화면 변경작업이 필요한 경우를 처리하기 위해 정의함.
 */

public interface ImageFilterListenter {

    public void chagneFilter(int filterCode, String filterConfig);
    public int currentPosition();
}
