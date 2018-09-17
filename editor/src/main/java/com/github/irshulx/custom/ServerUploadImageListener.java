package com.github.irshulx.custom;

import java.util.HashMap;

public interface ServerUploadImageListener {
    void onUpload(HashMap<String, String> image, String uuid);
}
