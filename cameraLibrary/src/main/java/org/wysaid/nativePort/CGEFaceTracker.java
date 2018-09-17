package org.wysaid.nativePort;

import android.graphics.Bitmap;
import android.graphics.PointF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by wysaid on 16/2/23.
 * Mail: admin@wysaid.org
 * blog: wysaid.org
 */
public class CGEFaceTracker {

    static {
        // 얼굴인식 라이브러리를 로드한다. (libs/armeabi-v7a/libFaceTracker.so)
        // libFaceTracker 에서는 opencv_java3 라이브러리를 정적으로 로드함.
        System.loadLibrary("FaceTracker");
    }

    private CGEFaceTracker() {
        mNativeAddress = nativeCreateFaceTracker();
    }
    public static CGEFaceTracker createFaceTracker() {
        if(!sIsTrackerSetup) {  // 얼굴인식이 설정되지 않았다면
            nativeSetupTracker(null, null, null);
            sIsTrackerSetup = true;
        }
        return new CGEFaceTracker();
    }


    // 얼굴인식 결과를 저장하는 클래스. 가장 기본적인 정보만 정의 되어있다.
    public static class FaceResultSimple {

        public PointF leftEyePos, rightEyepos;  // 왼쪽 눈, 오른쪽 눈 위치
        public PointF nosePos;                  // 코 중앙 위치
        public PointF mouthPos;                 // 입 중앙 위치
        public PointF jawPos;                   // 턱 아래 위치 (정확히 어떤 위치인지 잘 모르겠다)
    }

    protected static boolean sIsTrackerSetup = false;   // 얼굴인식이 설정 되었다면 true
    public static boolean isTrackerSetup() {
        return sIsTrackerSetup;
    }
    protected long mNativeAddress;  // 이게 정확히 뭘 뜻하는 건지 모름

    public void release() {
        if(mNativeAddress != 0) {
            nativeRelease(mNativeAddress);
            mNativeAddress = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * 얼굴인식 결과를 FaceResultSimple 객체에 담아 리턴한다.
     * @param bmp
     * @param drawFeature
     * @return
     */
    public FaceResultSimple detectFaceWithSimpleResult(Bitmap bmp, boolean drawFeature) {
        float[] result = nativeDetectFaceWithSimpleResult(mNativeAddress, bmp, drawFeature);

        if(result == null) {
            return null;
        }

        FaceResultSimple faceResultSimple = new FaceResultSimple();
        faceResultSimple.leftEyePos = new PointF(result[0], result[1]);
        faceResultSimple.rightEyepos = new PointF(result[2], result[3]);
        faceResultSimple.nosePos = new PointF(result[4], result[5]);
        faceResultSimple.mouthPos = new PointF(result[6], result[7]);
        faceResultSimple.jawPos = new PointF(result[8], result[9]);
        // 이 코드를 통해서 눈코입의 위치를 알 수 있지만 result 의 값들이 정확히 어떤 위치를 말하는 건지는 파악이 안됨.

        return faceResultSimple;
    }

    /////////// for video frames
    // TrackingCameraGLSurfaceView.TrackingProc 내부에 선언됨.
    public static class FaceResult {
        // 66 key points.
        public FloatBuffer faceKeyPoints = ByteBuffer.allocateDirect(66 * 8).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    protected FaceResult mFaceResult = new FaceResult();

    public FaceResult getFaceResult() {
        return mFaceResult;
    }


     /*
        native 메소드을 호출한다.
     */
     //recommended
    public boolean detectFaceWithGrayBuffer(ByteBuffer buffer, int width, int height, int bytesPerRow) {
        return nativeDetectFaceWithBuffer(mNativeAddress, buffer, width, height, 1, bytesPerRow, mFaceResult.faceKeyPoints);
    }
    public boolean detectFaceWithBGRABuffer(ByteBuffer buffer, int width, int height, int bytesPerRow) {
        return nativeDetectFaceWithBuffer(mNativeAddress, buffer, width, height, 4, bytesPerRow, mFaceResult.faceKeyPoints);
    }
    public boolean detectFaceWithBGRBuffer(ByteBuffer buffer, int width, int height, int bytesPerRow) {
        return nativeDetectFaceWithBuffer(mNativeAddress, buffer, width, height, 3, bytesPerRow, mFaceResult.faceKeyPoints);
    }


    /*
        native 메소드
     */

    //static
    private static native void nativeSetupTracker(String modelFile, String triFile, String conFile);
    //non-static
    protected native long nativeCreateFaceTracker();
    protected native void nativeRelease(long addr);
    protected native float[] nativeDetectFaceWithSimpleResult(long addr, Bitmap bmp, boolean drawFeature);
    protected native boolean nativeDetectFaceWithBuffer(long addr, ByteBuffer buffer, int w, int h, int channel, int bytesPerRow, FloatBuffer outputBuffer);


}
