package com.hyunju.jin.movie.activity.gallery;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 *
 */

public class GalleryFunction {

    // 인텐트를 통해 갤러리 옵션을 전달할 때 key 값
    public static final String OPTION_MULTI_SELECT = "multiSelect";
    public static final String OPTION_MULTI_SELECT_MAX = "MaxNumberOfMultiSelect";
    public static final String OPTION_IMAGE_ONLY = "imageOnly";
    public static final String OPTION_MSG = "msg";
    public static final String OPTION_FILTER = "filter";

    // 미디어 조회 결과에서 필요한 값을 관리하기 위한 key
    public static final String KEY_ALBUM = "album_name";
    public static final String KEY_PATH = "path";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_TIME = "date";
    public static final String KEY_COUNT = "date";
    public static final String KEY_MIME_TYPE = "mime_type";
    public static final String KEY_DURATION = "duration";

    public static final String KEY_FILTER_PATH = "filter_path";  // 필터 적용된 사진 경로

    public static  boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static HashMap<String, String> mappingInbox(String album, String path, String timestamp, String time, String count)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_COUNT, count);
        return map;
    }


    /**
     * 이름 바ㅜ꺼줘.. 사진/동영상 생성용
     */
    public static HashMap<String, String> mappingImage(String album, String path, String mime_type, String timestamp, String time, String count)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_COUNT, count);
        map.put(KEY_MIME_TYPE, mime_type);
        return map;
    }

    public static HashMap<String, String> mappingVideo(String album, String path, String mime_type, String video_duration, String timestamp, String time, String count)
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_COUNT, count);
        map.put(KEY_MIME_TYPE, mime_type);
        map.put(KEY_DURATION, video_duration);
        return map;
    }


    /**
     * 해당 폴더에 있는 사진/동영상 총 합을 구해서 10 Photos 와 같이 리턴한다.
     * @param c
     * @param album_name
     * @return
     */
    public static String getCount(Context c, String album_name)
    {
        Uri imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri videoUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA
                , MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                , MediaStore.MediaColumns.DATE_MODIFIED
        };

        Cursor cursorExternal = c.getContentResolver().query(imageUri, projection, "bucket_display_name = \""+album_name+"\"", null, null);
        Cursor cursorInternal = c.getContentResolver().query(videoUri, projection, "bucket_display_name = \""+album_name+"\"", null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

        return cursor.getCount()+" Photos";
    }

    public static String converToTime(String timestamp)
    {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm");
        return formatter.format(date);
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

}
