package com.hyunju.jin.movie.datamodel;

import android.util.Log;

import com.hyunju.jin.movie.R;

import java.io.Serializable;
import java.util.ArrayList;

public class ImageFilter implements Serializable {

    public static ArrayList<ImageFilter> getGalleryFilterList(){

        ArrayList<ImageFilter> filters = new ArrayList<ImageFilter>();
        // new GalleryFilter(필터사진 drawable, 필터코드, 필터목록에서 보여질 이름)
        // 필터 코드는 별도의 엑셀파일로 정리해둠.
        filters.add(new ImageFilter(R.drawable.image_filter_0, 0, "원본"));
        filters.add(new ImageFilter(R.drawable.image_filter_36, 36, "1"));
        filters.add(new ImageFilter(R.drawable.image_filter_90, 90, "2"));
        filters.add(new ImageFilter(R.drawable.image_filter_57, 57, "3"));
        filters.add(new ImageFilter(R.drawable.image_filter_44, 44, "4"));
        filters.add(new ImageFilter(R.drawable.image_filter_63, 63, "5"));
        filters.add(new ImageFilter(R.drawable.image_filter_101, 101, "6"));
        filters.add(new ImageFilter(R.drawable.image_filter_79, 79, "7"));
        filters.add(new ImageFilter(R.drawable.image_filter_104, 104, "8"));
        filters.add(new ImageFilter(R.drawable.image_filter_13, 13, "9"));
        return filters;
    }


    private int filter_preview_drawable;
    private int code;
    private String filter_name;
    private String filter_config;

    public ImageFilter(){}

    public ImageFilter(int filter_preview_drawable, int code, String filter_name){
        this.filter_preview_drawable = filter_preview_drawable;
        this.code = code;
        this.filter_name = filter_name;
    }

    public int getFilter_preview_drawable() {
        return filter_preview_drawable;
    }

    public void setFilter_preview_drawable(int filter_preview_drawable) {
        this.filter_preview_drawable = filter_preview_drawable;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getFilter_name() {
        return filter_name;
    }

    public void setFilter_name(String filter_name) {
        this.filter_name = filter_name;
    }

    /**
     * 필터 인덱스에 따른 구체적인 필터 효과를 리턴한다.
     * 외부 모듈 imageFilter 가 필터를 처리한다.
     * @return
     */
    public String getFilter_config() {

        switch (code){

            case 0:
                filter_config = ""; // 원본
                break;
            case 13:
                filter_config = "#unpack @style sketch 0.9";
                break;
            case 36:
                filter_config = "@curve R(0, 0)(71, 74)(164, 165)(255, 255) @pixblend screen 0.94118 0.29 0.29 1 20";
                break;
            case 44:
                filter_config = "@curve B(0, 0)(70, 87)(140, 191)(255, 255) @pixblend pinlight 0.247 0.49 0.894 1 20";
                break;
            case 57:
                filter_config = "@adjust saturation 0.7 @pixblend screen 1 0.243 0.69 1 30";
                break;
            case 63:
                filter_config = "@curve R(0, 4)(255, 244)G(0, 0)(255, 255)B(0, 84)(255, 194)";
                break;
            case 79:
                filter_config = "@colormul mat 0.34 0.48 0.22 0.34 0.48 0.22 0.34 0.48 0.22 @curve R(0, 29)(20, 48)(83, 103)(164, 166)(255, 239)G(0, 30)(30, 61)(66, 94)(151, 160)(255, 241)B(2, 48)(82, 93)(166, 143)(255, 199)";
                break;
            case 90:
                filter_config = "@curve R(0, 0)(69, 93)(126, 160)(210, 232)(255, 255)G(0, 0)(36, 47)(135, 169)(250, 254)B(0, 0)(28, 30)(107, 137)(147, 206)(255, 255)";
                break;
            case 101:
                filter_config = "@vigblend mix 10 10 30 255 91 0 1.0 0.5 0.5 3 @curve R(0, 31)(35, 75)(81, 139)(109, 174)(148, 207)(255, 255)G(0, 24)(59, 88)(105, 146)(130, 171)(145, 187)(180, 214)(255, 255)B(0, 96)(63, 130)(103, 157)(169, 194)(255, 255)";
                break;
            case 104:
                filter_config = "@adjust hsl 0.02 -0.31 -0.17 @curve R(0, 28)(23, 45)(117, 148)(135, 162)G(0, 8)(131, 152)(255, 255)B(0, 17)(58, 80)(132, 131)(127, 131)(255, 225)";
                break;
        }

        Log.e("선택필터 ", code+"번 입니다.");
        return filter_config;
    }

}
