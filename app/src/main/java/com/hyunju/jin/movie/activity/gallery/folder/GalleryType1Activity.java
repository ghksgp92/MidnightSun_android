package com.hyunju.jin.movie.activity.gallery.folder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.ItemDecorationGalleryColumns;
import com.hyunju.jin.movie.activity.gallery.MapComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 *
 */
public class GalleryType1Activity extends SuperActivity implements GalleryType1Listener {

    // 갤러리 폴더 목록을 나타내기 위해 사용되는 View, 객체 선언
    @BindView(R.id.recycler_gallery_folder_list) RecyclerView recycler_gallery_folder_list;
    GalleryFolderLoadTask galleryFolderLoadTask;    // 비동기로 사진/동영상 폴더를 로드하기 위해 생성
    ArrayList<HashMap<String, String>> galleryFolderList; // 사진/동영상 폴더 목록을 저장하는 객체 생성
    GalleryType1FolderAdapter galleryType1FolderAdapter;

    // 선택한 폴더에 있는 사진/동영상을 나타내기 위해 사용되는 View, 객체 선언
    @BindView(R.id.recycler_gallery_list) RecyclerView recycler_gallery_list;
    GalleryLoadTask galleryLoadTask;    // 비동기로 선택한 폴더의 사진/동영상을 로드하기 위해 생성
    ArrayList<HashMap<String, String>> galleryList;   // 선택한 폴더 안의 사진/동영상 목록
    GalleryType1Adapter galleryAdapter;
    private String selected_albumName;      // 현재 선택한 폴더 이름

    ArrayList<HashMap<String, String>> selectedList; // 사용자가 선택한 사진/비디오 목록

    @BindView(R.id.tv_selected_complete) TextView tv_selected_complete;

    private final int PM_EXTERNAL_STORAGE_READ_AND_WRITE = 1000; // 사용자 기기 SD 카드에 접근 권한 요청 코드
    private boolean multiSelect;        // 다중선택 갤러리인지 구분하는 변수
    private int multiSelectMaxCount;    // 다중선택 시 선택가능한 최대 갯수
    private boolean imageOnly;          // 이미지만 보여주는 갤러리인지 구분하는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            multiSelect = bundle.getBoolean(GalleryFunction.OPTION_MULTI_SELECT, false);
            imageOnly = bundle.getBoolean(GalleryFunction.OPTION_IMAGE_ONLY, false);
            multiSelectMaxCount = bundle.getInt(GalleryFunction.OPTION_MULTI_SELECT_MAX, 3);
        }
        
        galleryFolderList = new ArrayList<HashMap<String, String>>();
        galleryType1FolderAdapter = new GalleryType1FolderAdapter(getContext(), this, galleryFolderList);
        //recycler_gallery_folder_list.setLayoutManager(new GridLayoutManager(getContext(), 3));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //recycler_gallery_folder_list.setHasFixedSize(false);
        recycler_gallery_folder_list.setLayoutManager(layoutManager);
        recycler_gallery_folder_list.setAdapter(galleryType1FolderAdapter);

        galleryList = new ArrayList<HashMap<String, String>>();
        galleryAdapter = new GalleryType1Adapter(getContext(), this, galleryList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recycler_gallery_list.setLayoutManager(gridLayoutManager);
        recycler_gallery_list.addItemDecoration(new ItemDecorationGalleryColumns(1, gridLayoutManager.getSpanCount()));
        recycler_gallery_list.setAdapter(galleryAdapter);

        selectedList = new ArrayList<HashMap<String, String>>();

        // 사용자 앨범에 접근할 수 있는 권한을 얻는다.
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if ( !EasyPermissions.hasPermissions(this, perms)) {    // 권한이 없다면
            EasyPermissions.requestPermissions(this, "권한이 필요합니다.", PM_EXTERNAL_STORAGE_READ_AND_WRITE, perms);
        }

    }

    @OnClick(R.id.tv_selected_complete)
    public void complete(){

    }

    @Override
    protected void onResume() {
        super.onResume();

        (new GalleryFolderLoadTask()).execute();

    }

    /**
     * 권한 요청 결과에 대한 콜백함수.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case PM_EXTERNAL_STORAGE_READ_AND_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    galleryFolderLoadTask = new GalleryFolderLoadTask();
                    galleryFolderLoadTask.execute();

                }else {
                    Toast.makeText( getContext(), "You must accept permissions.", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void loadAlbum(String albumName) {
        selected_albumName = albumName;
        (new GalleryLoadTask()).execute();
    }

    @Override
    public void selectMedia(HashMap<String, String> select) {

        if(multiSelect){    // 다중선택 갤러리일 경우

            if(selectedList.size() >= multiSelectMaxCount){
                Toast.makeText(getContext(), "최대 "+multiSelectMaxCount+"까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                return;
            }


        }else{
            Intent returnIntent = new Intent();
            returnIntent.putExtra("select", select);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    /**
     * 사용자 기기의 외부저장소에서 사진/동영상이 있는 폴더 목록을 가져와 화면에 나타낸다.
     */
    private class GalleryFolderLoadTask extends AsyncTask<String, Void, String> {

        // 백그라운드 작업 전 호출
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            galleryFolderList.clear();
        }

        // 백그라운드 작업
        protected String doInBackground(String... args) {

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;

            // 사용자 기기의 외부 저장소에서만 탐색한다.
            Uri imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri videoUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.MediaColumns.DATA                    // 디스크 상의 파일 경로
                    , MediaStore.MediaColumns.DATE_MODIFIED         // 마지막 수정 시간. 초단위
                    , MediaStore.Images.Media.BUCKET_DISPLAY_NAME   // 이미지의 버킷(앨범) 이름
            };

            // 사용자 기기의 외부 저장소에서 사진이나 동영상이 있는 폴더 목록을 가져온다.
            Cursor cursorExternal = getContentResolver().query(imageUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",null, null);
            Cursor cursorInternal = getContentResolver().query(videoUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

            while (cursor.moveToNext()) {   // 왜 이미지가 1장만 가져오게 되는지 모르겠군..?

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                countPhoto = GalleryFunction.getCount(getApplicationContext(), album);

                galleryFolderList.add(GalleryFunction.mappingInbox(album, path, timestamp, GalleryFunction.converToTime(timestamp), countPhoto));
            }
            cursor.close();
            Collections.sort(galleryFolderList, new MapComparator(GalleryFunction.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return "";
        }

        // doInBackground 완료 후 호출
        @Override
        protected void onPostExecute(String xml) {
            galleryType1FolderAdapter.notifyDataSetChanged();
        }

        // doInBackground 에서 주기적으로 호출 가능한데.. 음..
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    /**
     * 사용자가 선택한 폴더에 있는 사진/동영상 목록을 가져와 화면에 나타낸다.
     */
    class GalleryLoadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            galleryList.clear();
        }

        protected String doInBackground(String... args) {

            String path = null;
            String album = null;
            String timestamp = null;

            String[] projection = {
                    MediaStore.MediaColumns.DATA                    // 디스크 상의 파일 경로
                    , MediaStore.MediaColumns.DATE_MODIFIED         // 마지막 수정 시간. 초단위
                    , MediaStore.Images.Media.BUCKET_DISPLAY_NAME   // 이미지의 버킷(앨범) 이름
            };

            Uri imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursorImage = getContentResolver().query(imageUri, projection, "bucket_display_name = \""+selected_albumName+"\"", null, null);
            Cursor cursor = cursorImage;

            if(imageOnly == false){  // 사진과 동영상을 모두 보여주는 View일 경우
                Uri videoUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Cursor cursorVideo = getContentResolver().query(videoUri, projection, "bucket_display_name = \""+selected_albumName+"\"", null, null);
                cursor = new MergeCursor(new Cursor[]{cursorImage, cursorVideo});
            }

            while (cursor.moveToNext()) {

                path = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                galleryList.add(GalleryFunction.mappingImage(album, path, null, timestamp, GalleryFunction.converToTime(timestamp), null));
            }

            cursorImage.close();
            Collections.sort(galleryList, new MapComparator(GalleryFunction.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending

            return "";
        }

        @Override
        protected void onPostExecute(String xml) {

            galleryAdapter.notifyDataSetChanged();

        }
    }






}
