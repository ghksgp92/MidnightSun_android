package com.hyunju.jin.movie.fragment.user;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.user.MypageConfigListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileDialogFragment extends DialogFragment {

    // 사용자 선택 가능한 프로필 변경 방법
    public static final String METHOD_CAMERA = "camera";
    public static final String METHOD_GALLERY = "gallery";
    public static final String METHOD_DEFAULT = "default";

    public static final String DATA_KEY_LISTENER = "listener";

    @BindView(R.id.tv_camera) TextView tv_camera;
    @BindView(R.id.tv_gallery) TextView tv_gallery;
    @BindView(R.id.tv_default) TextView tv_default;

    MypageConfigListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_profile_method, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        getDialog().getWindow().setLayout((width-100), ViewGroup.LayoutParams.MATCH_PARENT);
        ButterKnife.bind(this, view);

        listener = (MypageConfigListener) getActivity();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @OnClick(R.id.tv_camera)
    public void selectCamera(){
        listener.selectProfileMethod(METHOD_CAMERA);
        dismiss();
    }

    @OnClick(R.id.tv_gallery)
    public void selectGallery(){
        listener.selectProfileMethod(METHOD_GALLERY);
        dismiss();
    }

    @OnClick(R.id.tv_default)
    public void selectDefault(){
        listener.selectProfileMethod(METHOD_DEFAULT);
        dismiss();
    }
}
