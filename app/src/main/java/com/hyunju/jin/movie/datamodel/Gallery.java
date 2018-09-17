package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class Gallery implements Serializable{

    @SerializedName("media") private HashMap<String, String> media; // 이름 너무 추상적
    @SerializedName("selectOrder") private int selectOrder;

    @SerializedName("filter") private ImageFilter filter;

    public ImageFilter getFilter() {

        if (filter == null) {
            filter = new ImageFilter();
        }
        return filter;
    }

    public void setFilter(ImageFilter filter) {
        this.filter = filter;
    }

    public Gallery(){}
    public Gallery(HashMap<String, String> media){
        this.media = media;
    }

    public HashMap<String, String> getMedia() {
        return media;
    }

    public void setMedia(HashMap<String, String> media) {
        this.media = media;
    }

    public int getSelectOrder() {
        return selectOrder;
    }

    public void setSelectOrder(int selectOrder) {
        this.selectOrder = selectOrder;
    }

    @Override
    public boolean equals(Object obj) {
        //return super.equals(obj);

        boolean result = false;

        if( obj instanceof Gallery){
            Gallery obj_gallery = (Gallery) obj;
            HashMap<String, String> obj_media = obj_gallery.getMedia();
            result = this.media.equals(obj_media);
        }
        return result;
    }


}
