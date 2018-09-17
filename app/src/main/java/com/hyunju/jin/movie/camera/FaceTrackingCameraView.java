package com.hyunju.jin.movie.camera;

import android.content.Context;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class FaceTrackingCameraView extends JavaCameraView{
    public FaceTrackingCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public FaceTrackingCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int switchCamera() {
        if (mCameraIndex != CAMERA_ID_FRONT) {
            disableView();
            mCameraIndex = CAMERA_ID_FRONT;
            enableView();
            return mCameraIndex;
        }else {
            disableView();
            mCameraIndex = CAMERA_ID_BACK;
            enableView();
            return mCameraIndex;
        }
    }
}
