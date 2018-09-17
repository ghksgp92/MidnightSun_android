package com.hyunju.jin.movie.activity.entertainment;

import android.support.annotation.NonNull;

import com.hyunju.jin.movie.activity.entertainment.ARData;

import java.io.Serializable;
import java.util.List;

/**
 * assets/wikitude 하위에 작성된 AR 이벤트 목록을 담기위한 클래스
 */
public class ARCategory implements Serializable {

    private final List<ARData> samples;
    private final String name;

    public ARCategory(@NonNull List<ARData> samples, @NonNull String name) {
        this.samples = samples;
        this.name = name;
    }

    public List<ARData> getSamples() {
        return samples;
    }

    public String getName() {
        return name;
    }
}
