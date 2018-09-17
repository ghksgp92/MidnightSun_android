package com.hyunju.jin.movie.adapter.user;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.fragment.user.MovieCollectionFragment;
import com.hyunju.jin.movie.fragment.user.PostingByUserFragment;
import com.hyunju.jin.movie.fragment.user.WatchedMovieFragment;
import com.hyunju.jin.movie.fragment.user.WatchingMovieFragment;

/**
 * 마이페이지의 탭을 관리한다.
 * 사용자가 탭을 이동하더라도 전에 보고있던 화면을 유지하도록 하기 위해 FragmentPagerAdapter 을 상속받았다.
 * (예를들어, 현재 내가 보고있던 포스팅 목록의 위치가 다른 탭을 보고 와도 유지가 된다던지? 이게 사용자에겐 더 편할거라 생각하는데 아닌가?)
 *
 * 다만 이 경우 메모리가 부족할 정도로 페이지를 많이 보게될 경우 문제가 생길 수 있겠다. 현재는 그런 경우가 거의 없다고 보기때문에
 * 그럴 경우가 생기면 코드를 수정하는게 맞다고 본다. (내 생각에는 이 처리가 좀 복잡해서)
 */
public class MyPageViewPagerAdapter extends FragmentPagerAdapter {

   // private movie movie;
    private String[] mypageTabTitles;

    // 탭으로 추가될 수 있는 항목들
    public static final String TAB_WATCHING_MOVIE = "이어보기";
    public static final String TAB_POSTING = "포스팅";
    public static final String TAB_WATCHED_MOVIE = "다본영화";
    public static final String TAB_MOVIE_COLLETION = "컬렉션";


    public MyPageViewPagerAdapter(FragmentManager fm, String[] mypageTabTitles) {
        super(fm);
        this.mypageTabTitles = mypageTabTitles;
    }

    @Override
    public Fragment getItem(int position) {

        // 포지션말고 포지션에 들어간 실제 값으로 비교해야함.

        String tabTitle = mypageTabTitles[position];

        switch (tabTitle){
            case TAB_WATCHING_MOVIE:
                return WatchingMovieFragment.getInstance();
            case TAB_WATCHED_MOVIE:
                return WatchedMovieFragment.getInstance();
            case TAB_POSTING:
                return PostingByUserFragment.getInstance();
            case TAB_MOVIE_COLLETION:
                return MovieCollectionFragment.getInstance();
            default:
                return PostingByUserFragment.getInstance();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mypageTabTitles[position];
    }

    @Override
    public int getCount() {
        if(mypageTabTitles == null){
            return 0;
        }
        return mypageTabTitles.length;
    }
}
