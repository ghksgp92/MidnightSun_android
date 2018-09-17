package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.ItemDecorationGalleryColumns;
import com.hyunju.jin.movie.activity.gallery.MapComparator;
import com.hyunju.jin.movie.datamodel.Gallery;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * 갤러리 Type 2
 * - 폴더 구분 없이 모든 사진/동영상을 시간순서대로 나열한다.
 * - 이미지만 보는 갤러리로 설정할 수 있다. (관련 변수 imageOnly)
 * - 다중선택 갤러리로 설정할 수 있다. (관련 변수 multiSelect, multiSelectMaxCount)
 */
public class GalleryType2Activity extends SuperActivity implements GalleryType2Listener {

    // 다중선택 갤러리일 때, 선택 이미지 수, 최대 선택가능 이미지 수를 표시하는 View 참조
    @BindView(R.id.layout_multi_select) LinearLayout layout_multi_select;
    @BindView(R.id.tv_select_count) TextView tv_select_count;
    @BindView(R.id.tv_max_count) TextView tv_max_count;
    @BindView(R.id.tv_msg) TextView tv_msg; // 갤러리 사용 목적에 따른 메시지를 보여주는 View, 예를들어 '프로필 사진을 선택하세요.' 같은?
    @BindView(R.id.layout_menu_filer) RelativeLayout layout_menu_filer; // 화면 하단에 필터 메뉴

    // 사용자 기기의 사진/동영상을 보여주기 위한 View, 객체 선언
    @BindView(R.id.recycler_gallery_list)  RecyclerView recycler_gallery_list;
    ArrayList<Gallery> galleryList; // 선택한 폴더 안의 사진/동영상 목록Gallery
    GalleryType2Adapter galleryAdapter;

    private final int PM_EXTERNAL_STORAGE_READ_AND_WRITE = 1000; // 사용자 기기 SD 카드에 접근 권한 요청 코드
    public final int REQ_FILTER = 1;
    private boolean multiSelect;        // 다중선택 갤러리인지 구분하는 변수. true이면 다중선택 갤러리다. (사실 이게 필요한가 싶다. 선택가능한 수가 1이면 단일선택 갤러리가 되는건데)
    private int multiSelectMaxCount;    // 다중선택 시 선택가능한 최대 갯수.
    private boolean imageOnly;          // 이미지만 보여주는 갤러리인지 구분하는 변수. true 일때 이미지만 조회한다.
    private boolean filterOptionUse;          // 필터 사용 여부. true면 사용한다.

    ArrayList<Gallery> selectedList; // 사용자가 선택한 사진/비디오 목록

    public static final String DATA_KEY_SELECT_LIST = "selectedList";   // 다중선택 갤러리일때, 선택 목록에 대한 데이터 키 (주석 뭔말인지 모르겠다. 의도전달이 안됨)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_type2);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            multiSelect = bundle.getBoolean(GalleryFunction.OPTION_MULTI_SELECT, false);
            imageOnly = bundle.getBoolean(GalleryFunction.OPTION_IMAGE_ONLY, false);
            multiSelectMaxCount = bundle.getInt(GalleryFunction.OPTION_MULTI_SELECT_MAX, 3);
            filterOptionUse = bundle.getBoolean(GalleryFunction.OPTION_FILTER, false);
            tv_max_count.setText(multiSelectMaxCount+"");
            String msg = bundle.getString(GalleryFunction.OPTION_MSG, "");
            if(StringUtils.isNotEmpty(msg)){
                tv_msg.setText(msg);
                tv_msg.setVisibility(View.VISIBLE);
            }else{
                tv_msg.setVisibility(View.GONE);
            }

        }

        if(1 >= multiSelectMaxCount){   // 다중선택이 아니면 이미지 선택정보 창을 감춘다.
            layout_multi_select.setVisibility(View.INVISIBLE);
        }
        if(!filterOptionUse){ // 필터 사용하지 않으면 필터 메뉴를 감춘다.
            layout_menu_filer.setVisibility(View.GONE);
        }

        galleryList = new ArrayList<Gallery>();
        galleryAdapter = new GalleryType2Adapter(getContext(), this, galleryList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        recycler_gallery_list.setLayoutManager(gridLayoutManager);
        recycler_gallery_list.addItemDecoration(new ItemDecorationGalleryColumns(2, gridLayoutManager.getSpanCount()));
        recycler_gallery_list.setAdapter(galleryAdapter);
        selectedList = new ArrayList<Gallery>();    // 사용자가 선택한 항목을 관리하는 리스트

    }

    @Override
    protected void onResume() {
        super.onResume();

        // 사용자 앨범에 접근할 수 있는 권한을 얻는다.
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if ( !EasyPermissions.hasPermissions(this, perms)) {    // 권한이 없다면
            EasyPermissions.requestPermissions(this, "권한이 필요합니다.", PM_EXTERNAL_STORAGE_READ_AND_WRITE, perms);
        }else {
            (new GalleryLoadTask()).execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        switch (requestCode) {
            case PM_EXTERNAL_STORAGE_READ_AND_WRITE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    (new GalleryLoadTask()).execute();
                }else {
                    Toast.makeText( getContext(), "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @OnClick(R.id.tv_select_complete)
    public void selectComplete(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra(DATA_KEY_SELECT_LIST, selectedList);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    /**
     * 이미지 필터를 적용 화면으로 이동한다.
     */
    @OnClick(R.id.img_photo_editor)
    public void photoEdit(){
        Intent filter = new Intent(getContext(), ImageFilterActivity.class);
        filter.putExtra(ImageFilterActivity.DATA_KEY_FILTER_IMG_LIST, selectedList);
        startActivityForResult(filter, REQ_FILTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode){
           case REQ_FILTER:

               if(resultCode == RESULT_OK){
                   ArrayList<Gallery> filterImageList = (ArrayList<Gallery>) data.getSerializableExtra(ImageFilterActivity.DATA_KEY_FILTER_IMG_LIST);
                   Intent returnIntent = new Intent();
                   returnIntent.putExtra(DATA_KEY_SELECT_LIST, filterImageList);
                   setResult(Activity.RESULT_OK, returnIntent);
                   finish();
               }
               break;
       }
    }

    @Override
    public void loadAlbum(String albumName) {
        // 현재 이 갤러리의 경우 앨범 구분 없이 모두 가져와서 시간순서대로 나열하므로 이 메소드는 구현할 필요가 없다.
    }

    @Override
    public void selectMedia(Gallery gallery) {

        if(multiSelect){    // 다중선택 갤러리일 경우

            if( selectedList.contains(gallery) ){
                gallery.setSelectOrder(0);
                selectedList.remove(gallery);
            }else{

                if(selectedList.size() >= multiSelectMaxCount){
                    Toast.makeText(getContext(), "최대 "+multiSelectMaxCount+"까지만 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedList.add(gallery);
            }

            for (Gallery item: selectedList) {
                item.setSelectOrder( (selectedList.indexOf(item) + 1) ); // 인덱스 값은 0부터 시작하므로 1을 더해야한다.
            }
            tv_select_count.setText(selectedList.size()+"");
            galleryAdapter.notifyDataSetChanged();

        }else{ // 단일선택 갤러리일 경우는 현재 프로필 사진 설정밖에 없다.
            // 자동으로 필터 적용 화면으로 넘어가도록 한다.
            /*Intent returnIntent = new Intent();
            returnIntent.putExtra(DATA_KEY_SELECT_LIST, selectedList);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();*/
            selectedList.add(gallery);
            if(filterOptionUse) {
                photoEdit();
            }else{
                Intent returnIntent = new Intent();
                returnIntent.putExtra(DATA_KEY_SELECT_LIST, selectedList);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

        }
    }

    @Override
    public int checkSelectedList(Gallery gallery) {

        int order = 0;
        if(selectedList.contains(gallery)){
            order = selectedList.get(selectedList.indexOf(gallery)).getSelectOrder();
        }
        return order;
    }

    /**
     * 사용자가 기기에 있는 사진/동영상 목록을 가져와 화면에 나타낸다.
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
            String mime_Type = null;
            String video_duration = null;

            String[] imageProjection = {
                    MediaStore.MediaColumns.DATA                    // 디스크 상의 파일 경로
                    , MediaStore.MediaColumns.DATE_MODIFIED         // 마지막 수정 시간. 초단위
                    , MediaStore.Images.Media.BUCKET_DISPLAY_NAME   // 이미지의 버킷(앨범) 이름
                    , MediaStore.Files.FileColumns.MIME_TYPE        // 타입, 예를들어 images/png 형태
            };

            Uri imageUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor cursorImage = getContentResolver().query(imageUri, imageProjection, null, null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");

            while (cursorImage.moveToNext()) {
                path = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                mime_Type = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                galleryList.add( new Gallery(GalleryFunction.mappingImage(album, path, mime_Type, timestamp, GalleryFunction.converToTime(timestamp), null)) );
            }
            cursorImage.close();

            if(imageOnly == false){  // 사진과 동영상을 모두 보여주는 View일 경우

                String[] videoProjection = {
                        MediaStore.MediaColumns.DATA                    // 디스크 상의 파일 경로
                        , MediaStore.MediaColumns.DATE_MODIFIED         // 마지막 수정 시간. 초단위
                        , MediaStore.Video.Media.BUCKET_DISPLAY_NAME   // 이미지의 버킷(앨범) 이름
                        , MediaStore.Files.FileColumns.MIME_TYPE        // 파일 타입, 예를들어 video/mp4 형태
                        , MediaStore.Video.VideoColumns.DURATION
                };

                Uri videoUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                Cursor cursorVideo = getContentResolver().query(videoUri, videoProjection, null, null, MediaStore.MediaColumns.DATE_MODIFIED+" DESC");

                while (cursorVideo.moveToNext()) {
                    path = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    mime_Type = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
                    video_duration = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION));
                    galleryList.add( new Gallery(GalleryFunction.mappingVideo(album, path, mime_Type, video_duration, timestamp, GalleryFunction.converToTime(timestamp), null)) );
                }
                cursorVideo.close();
            }
            //Collections.sort(galleryList, new MapComparator(GalleryFunction.KEY_TIMESTAMP, "dsc")); // 최근 저장된 순으로 정렬함.
            return "";
        }

        @Override
        protected void onPostExecute(String xml) {
            galleryAdapter.notifyDataSetChanged();
        }

    }
}

