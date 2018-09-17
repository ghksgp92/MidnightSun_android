package com.hyunju.jin.movie.adapter.movie;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.fragment.MovieBasicInfoFragment;
import com.hyunju.jin.movie.fragment.movie.MovieCastingFragment;
import com.hyunju.jin.movie.fragment.MovieImageListFragment;
import com.hyunju.jin.movie.fragment.movie.MovieReportFragment;


/**
 * 영화 상세정보 페이지에서 영화 정보를 나타내는 Tab을 관리한다.
 */
public class MovieDetailViewPagerAdapter extends FragmentPagerAdapter {

    private Movie movie;
    private String[] categoryTitles = {"INFO", "CASTING", "COMMENT"};

    public MovieDetailViewPagerAdapter(FragmentManager fm, Movie movie) {
        super(fm);
        this.movie = movie;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 1: // 영화 캐스팅 정보. 감독, 배우 등등..
                return MovieCastingFragment.getInstance(movie);

            case 2: // 영화 이미지 목록 화면
                return MovieImageListFragment.getInstance(movie);

            case 3: // 영화 후기
                return MovieReportFragment.getInstance(movie);

                default: // 영화 기본정보 화면. 제목, 장르, 줄거리 등등..
                    return MovieBasicInfoFragment.getInstance(movie);
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return categoryTitles[position];
    }

    @Override
    public int getCount() {
        if(categoryTitles == null){
            return 0;
        }
        return categoryTitles.length;
    }


}
