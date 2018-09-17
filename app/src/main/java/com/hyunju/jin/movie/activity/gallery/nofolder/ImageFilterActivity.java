package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.ImageFilter;

import org.wysaid.view.ImageGLSurfaceView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 사용자가 선택한 이미지에 필터를 적용하는 화면
 * 이미지는 여러장을 받을 수 있으므로 ViewPager 로 나타낸다.
 *
 * *재선택 처리 필요
 */
public class ImageFilterActivity extends SuperActivity implements ImageFilterListenter{

    @BindView(R.id.layout_multi_select) LinearLayout layout_multi_select;

    @BindView(R.id.vpager_media) ViewPager vpager_media; // 필터를 적용할 사진/동영상 목록을 보여주는 View
    ArrayList<Gallery> selectedList;
    MediaViewPagerAdapter mediaViewPagerAdapter;

    @BindView(R.id.recycler_filter_list) RecyclerView recycler_filter_list; // 사용 가능한 필터 목록을 보여주는 View
    ArrayList<ImageFilter> imageFilters;
    ImageFilterAdapter imageFilterAdapter;

    int currentShowMediaPosition;   // 현재 보고있는 사진/동영상의 selectedList 인덱스

    public static final String DATA_KEY_FILTER_IMG_LIST = "select_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_filter);
        ButterKnife.bind(this);

        // 필터 목록을 불러온다.
        imageFilters = ImageFilter.getGalleryFilterList();
        imageFilterAdapter = new ImageFilterAdapter(getContext(), imageFilters, this);
        recycler_filter_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycler_filter_list.setAdapter(imageFilterAdapter);

        // 선택한 이미지 목록을 가져온다.
        Bundle bundle = getIntent().getExtras();
        if( bundle != null ){
            selectedList = (ArrayList<Gallery>) bundle.getSerializable(DATA_KEY_FILTER_IMG_LIST);
            if( selectedList == null || selectedList.size() == 0){
                Toast.makeText(getContext(), "사진이나 동영상을 선택해주세요.", Toast.LENGTH_SHORT).show();
                finish();
            }

            // 선택한 이미지 목록을 Viewpager Adpater 와 연결한다.
            mediaViewPagerAdapter = new MediaViewPagerAdapter(getContext(), selectedList, this);
            vpager_media.setAdapter(mediaViewPagerAdapter);
            vpager_media.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                /**
                 * 이미지를 변경하면 호출된다.
                 * @param position
                 */
                @Override
                public void onPageSelected(int position) {
                    currentShowMediaPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

        }
    }

    /**
     * 이미지 필터를 선택하면 현재 선택된 이미지에 필터를 변경한다.
     * @param filterCode
     */
    @Override
    public void chagneFilter(int filterCode, String filterConfig) {
        selectedList.get(currentShowMediaPosition).getFilter().setCode(filterCode);
        vpager_media.getAdapter().notifyDataSetChanged();
    }

    /**
     * 현재 화면에 나타난 이미지의 인덱스를 리턴한다.
     * @return
     */
    @Override
    public int currentPosition() {
        return currentShowMediaPosition;
    }

    /**
     * 완료 버튼 클릭 시 호출됨.
     * 필터를 적용한 이미지에 대한 처리를 한 후 리턴한다.
     */
    @OnClick(R.id.tv_select_complete)
    public void filterApplyComplete(){

        Intent returnIntent = new Intent();
        returnIntent.putExtra(DATA_KEY_FILTER_IMG_LIST, selectedList);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }
}
