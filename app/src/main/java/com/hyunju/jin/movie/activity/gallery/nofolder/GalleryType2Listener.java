package com.hyunju.jin.movie.activity.gallery.nofolder;

import com.hyunju.jin.movie.datamodel.Gallery;

import java.util.HashMap;

public interface GalleryType2Listener {

    /**
     * albumName 폴더 안에있는 사진/동영상 목록을 불러온다.
     * @param albumName
     */
    public void loadAlbum(String albumName);

    public void selectMedia(Gallery gallery);

    public int checkSelectedList(Gallery gallery);
}
