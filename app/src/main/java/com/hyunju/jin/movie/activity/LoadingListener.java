package com.hyunju.jin.movie.activity;

/**
 * AVLoadingIndicatorView 를 사용하는 액티비티에서
 * 로딩 시작과 로딩 종료 시 해야하는 동작을 정의하게 하기 위한 인터페이스.
 * 필수로 구현해야하는 것은 아니다.
 */
public interface LoadingListener {
    void showLoading();
    void hideLoading();
}
