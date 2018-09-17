package com.github.irshulx.custom;

import java.util.HashMap;

public interface MovieTagListener {
    void onUpload(MovieTag movieTag, String uuid);
    void showMovie(MovieTag movieTag);
}
