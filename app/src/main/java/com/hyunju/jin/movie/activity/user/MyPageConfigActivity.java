package com.hyunju.jin.movie.activity.user;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.entertainment.CameraFilterActivity;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.activity.gallery.nofolder.GalleryType2Activity;
import com.hyunju.jin.movie.datamodel.Gallery;
import com.hyunju.jin.movie.datamodel.User;
import com.hyunju.jin.movie.fragment.user.ProfileDialogFragment;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageConfigActivity extends SuperActivity implements MypageConfigListener{

    public static final int REQ_CAMERA = 1;
    public static final int REQ_GALLERY = 2;
    @BindView(R.id.img_user_profile) CircularImageView img_user_profile; // 프로필 이미지 뷰
    @BindView(R.id.tv_user_id) TextView tv_user_id;
    @BindView(R.id.tv_user_email) TextView tv_user_email;

    User myInfo;
    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page_config);
        ButterKnife.bind(this);

        initialize();
    }

    private void initialize(){
        userService = RetrofitClient.getUserService();
        loadUserInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 서버에 사용자 정보를 요청한다.
     */
    private void loadUserInfo(){

        HashMap<String, String> data_getUser = new HashMap<String, String>();
        data_getUser.put("userCode", loginUser.getUserCode()+"");

        Call<User> call_getUser = userService.getUser("getUser", data_getUser);
        call_getUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    myInfo = (User) response.body();

                    tv_user_id.setText(myInfo.getId());
                    tv_user_email.setText(myInfo.getEmail());
                    Glide.with(getContext()).load(myInfo.getProfileImg()).apply(new RequestOptions().error(R.drawable.img_user)).into(img_user_profile);
                    return;
                }
                Log.e(TAG, "사용자 정보 로드 실패");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "사용자 정보 로드 실패 (onFailure)");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 프로필 사진 수정 방법을 선택할 수 있는 다이얼로그를 보여준다.
     */
    @OnClick(R.id.tv_profile_edit)
    public void showProfileDialog(){
        String dialogTag = "profileDialog";
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(dialogTag);  // 이미 만들어진 다이얼로그가 있다면
        if (prev != null) {
            ft.remove(prev);    // 삭제후
        }
        ft.addToBackStack(null);

        // 다시 다이얼로그를 만든다.
        DialogFragment profileDialogFragment = new ProfileDialogFragment();
        profileDialogFragment.show(ft, dialogTag);
    }

    @OnClick(R.id.tv_logout)
    public void logout(){
        SharedPreferences.Editor sharedEditor = SharedPreferencesBuilder.getSharedDefaultConfigEditor(this);

        // 자동로그인 관련된 값들을 초기화 시킨다.
        sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_CODE);
        sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_ID);
        sharedEditor.remove(SharedPreferencesBuilder.USR_LOGIN_USER_AUTH);
        sharedEditor.commit();

        // 로그인 화면으로 이동한다.
        Intent login = new Intent(getContext(), LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
    }

    /**
     * 프로필 사진 설정 방법에 따른 처리
     * @param method
     */
    @Override
    public void selectProfileMethod(String method) {
        if(ProfileDialogFragment.METHOD_CAMERA.equals(method)){ // 카메라 촬영일 경우

            Intent camera = new Intent(getContext(), CameraFilterActivity.class);
            startActivityForResult(camera, REQ_CAMERA);

        }else if(ProfileDialogFragment.METHOD_GALLERY.equals(method)){ // 갤러리 사진 선택인 경우
            Intent gallery = new Intent(getContext(), GalleryType2Activity.class);
            gallery.putExtra(GalleryFunction.OPTION_MULTI_SELECT_MAX, 1);
            gallery.putExtra(GalleryFunction.OPTION_IMAGE_ONLY, true);
            gallery.putExtra(GalleryFunction.OPTION_FILTER, true);
            startActivityForResult(gallery, REQ_GALLERY);

        }else if(ProfileDialogFragment.METHOD_DEFAULT.equals(method)){ // 기본이미지인 경우

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){

            case REQ_CAMERA:
                if(resultCode == RESULT_OK) {

                }

                break;
            case REQ_GALLERY:

                if(resultCode == RESULT_OK){
                    ArrayList<Gallery> selectedProfileImages = (ArrayList<Gallery>) data.getSerializableExtra(GalleryType2Activity.DATA_KEY_SELECT_LIST);
                    // 프로필 사진을 설정하는 것이기 때문에 리스트 size 가 항상 1로 넘어온다.

                    String profileImagePath = selectedProfileImages.get(0).getMedia().get(GalleryFunction.KEY_FILTER_PATH);
                    if(StringUtils.isEmpty(profileImagePath)){  // 필터 경로가 없다는 것은 이미지가 원본이라는 뜻이다.
                        profileImagePath = selectedProfileImages.get(0).getMedia().get(GalleryFunction.KEY_PATH);
                    }

                    File photoToFile = new File(profileImagePath); // 선택한 사진의 path 로 파일 객체를 만든다.
                    if( !photoToFile.exists() ){
                        Log.e(TAG, "갤러리 프로필 사진 파일이 없어");
                        return; }  // 파일이 존재하지 않으면 리턴

                    // 프로필 사진을 미리 설정하고
                    Glide.with(getContext()).load(new File(profileImagePath)).into(img_user_profile);

                    // 서버에 프로필 사진 업로드 요청
                    HashMap<String, RequestBody> data_updateUserProfile = new HashMap<String, RequestBody>();
                    data_updateUserProfile.put("userCode", RequestBody.create(MediaType.parse("text/plain"), loginUser.getUserCode()+""));
                    data_updateUserProfile.put("reqType", RequestBody.create(MediaType.parse("text/plain"),"UPDATE"));

                    RequestBody reqFile = RequestBody.create(MediaType.parse("multipart/form-data"), photoToFile);
                    MultipartBody.Part file = MultipartBody.Part.createFormData("profileImage", photoToFile.getName(), reqFile);

                    Call<ResponseData> call_updateUserProfle = userService.updateUserProfile("updateUserProfile", data_updateUserProfile, file);
                    call_updateUserProfle.enqueue(new Callback<ResponseData>() {
                        @Override
                        public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                            if(response.isSuccessful()){
                                if(response.body().getCode() == 1){
                                   // FileUtil.deleteTempDir();   // 임시 폴더 삭제
                                    loadUserInfo();
                                    return;
                                }
                            }

                            Log.e(TAG, "갤러리 프로필 사진 설정 실패");
                            Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<ResponseData> call, Throwable t) {
                            Log.e(TAG, "갤러리 프로필 사진 설정 실패 (onFailure)");
                            Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
        }
    }
}
