package com.hyunju.jin.movie.activity.gallery.folder;

import java.util.HashMap;

/**
 * Activity 내부에 있는 custom Adapter 에서 화면 변경에 필요한 작업을 요청할 경우 사용하는 인터페이스
 * (뭔가 주석이 모지리 같아 보인다)
 */
public interface GalleryType1Listener {

    /**
     * albumName 폴더 안에있는 사진/동영상 목록을 불러온다.
     * @param albumName
     */
    public void loadAlbum(String albumName);

    public void selectMedia(HashMap<String, String> select);


}
