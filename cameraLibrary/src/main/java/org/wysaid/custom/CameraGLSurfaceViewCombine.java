package org.wysaid.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;

import org.wysaid.camera.CameraInstance;
import org.wysaid.common.Common;
import org.wysaid.common.FrameBufferObject;
import org.wysaid.gpuCodec.TextureDrawerNV12ToRGB;
import org.wysaid.nativePort.CGEFrameRecorder;
import org.wysaid.nativePort.CGENativeLibrary;
import org.wysaid.view.CameraGLSurfaceView;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 얼굴인식 카메라와 필터를 함께 사용하기 위해
 */
public class CameraGLSurfaceViewCombine extends CameraGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback  {

    protected byte[] mPreviewBuffer0;
    protected byte[] mPreviewBuffer1;
    protected TextureDrawerNV12ToRGB mYUVDrawer;
    protected int mTextureY, mTextureUV;
    protected int mTextureWidth, mTextureHeight;
    protected ByteBuffer mBufferY, mBufferUV;
    protected int mYSize, mUVSize;
    protected int mBufferSize;
    protected SurfaceTexture mSurfaceTexture;
    protected boolean mBufferUpdated = false;
    protected final int[] mBufferUpdateLock = new int[0];

    public CameraGLSurfaceViewCombine(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }


    protected void onRelease() {
        super.onRelease();

        // CameraGLSurfaceViewWithTexture
        if(mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if(mTextureID != 0) {
            Common.deleteTextureID(mTextureID);
            mTextureID = 0;
        }

        if(mFrameRecorder != null) {
            mFrameRecorder.release();
            mFrameRecorder = null;
        }

        // CameraGLSurfaceViewWithBuffer
        if(mYUVDrawer != null) {
            mYUVDrawer.release();
            mYUVDrawer = null;
        }

        if(mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        if(mTextureY != 0 || mTextureUV != 0) {
            GLES20.glDeleteTextures(2, new int[]{mTextureY, mTextureUV}, 0);
            mTextureY = mTextureUV = 0;
            mTextureWidth = 0;
            mTextureHeight = 0;
        }
    }

    // CameraGLSurfaceViewWithBuffer
    protected void resizeTextures() {

        if (mTextureY == 0 || mTextureUV == 0) {
            int[] textures = new int[2];
            GLES20.glGenTextures(2, textures, 0);
            mTextureY = textures[0];
            mTextureUV = textures[1];

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
            Common.texParamHelper(GLES20.GL_TEXTURE_2D, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
            Common.texParamHelper(GLES20.GL_TEXTURE_2D, GLES20.GL_LINEAR, GLES20.GL_CLAMP_TO_EDGE);
        }

        int width = cameraInstance().previewWidth();
        int height = cameraInstance().previewHeight();

        if (mTextureWidth != width || mTextureHeight != height) {
            mTextureWidth = width;
            mTextureHeight = height;

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mTextureWidth, mTextureHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, mTextureWidth / 2, mTextureHeight / 2, 0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, null);
        }
    }

    protected void updateTextures() {
        if(mBufferUpdated) {
            synchronized (mBufferUpdateLock) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mTextureWidth, mTextureHeight, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mBufferY.position(0));
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mTextureWidth / 2, mTextureHeight / 2, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mBufferUV.position(0));
                mBufferUpdated = false;
            }
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureUV);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //super.onSurfaceCreated(gl, config);

        // CameraGLSurfaceViewWithBuffer
        mYUVDrawer = TextureDrawerNV12ToRGB.create();
        mYUVDrawer.setFlipScale(1.0f, 1.0f);
        mYUVDrawer.setRotation((float) (Math.PI / 2.0));
        mSurfaceTexture = new SurfaceTexture(0);

        // CameraGLSurfaceViewWithTexture
        mFrameRecorder = new CGEFrameRecorder();
        mIsTransformMatrixSet = false;
        if (!mFrameRecorder.init(mRecordWidth, mRecordHeight, mRecordWidth, mRecordHeight)) {
            Log.e(LOG_TAG, "Frame Recorder init failed!");
        }

        mFrameRecorder.setSrcRotation((float) (Math.PI / 2.0));
        mFrameRecorder.setSrcFlipScale(1.0f, -1.0f);
        mFrameRecorder.setRenderFlipScale(1.0f, -1.0f);

        mTextureID = Common.genSurfaceTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        super.onSurfaceCreated(gl, config);
    }


    @Override
    public void resumePreview() {
        if(mYUVDrawer == null) {
            return;
        }

        if (!cameraInstance().isCameraOpened()) {
            int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;

            cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                @Override
                public void cameraReady() {
                    Log.i(LOG_TAG, "tryOpenCamera OK...");
                }
            }, facing);
        }

        if (!cameraInstance().isPreviewing()) {
            Camera camera = cameraInstance().getCameraDevice();
            Camera.Parameters parameters = camera.getParameters();
            parameters.getPreviewFormat();
            Camera.Size sz = parameters.getPreviewSize();
            int format = parameters.getPreviewFormat();

            if(format != ImageFormat.NV21)
            {
                try {
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    camera.setParameters(parameters);
                    format = ImageFormat.NV21;
                } catch (Exception e) {
                    e.printStackTrace();
                    return ;
                }
            }

            mYSize = sz.width * sz.height;
            int newBufferSize = mYSize * ImageFormat.getBitsPerPixel(format) / 8;

            if(mBufferSize != newBufferSize) {
                mBufferSize = newBufferSize;
                mUVSize = mBufferSize - mYSize;
                mBufferY = ByteBuffer.allocateDirect(mYSize).order(ByteOrder.nativeOrder());
                mBufferUV = ByteBuffer.allocateDirect(mUVSize).order(ByteOrder.nativeOrder());

                mPreviewBuffer0 = new byte[mBufferSize];
                mPreviewBuffer1 = new byte[mBufferSize];
            }

            camera.addCallbackBuffer(mPreviewBuffer0);
            camera.addCallbackBuffer(mPreviewBuffer1);

            cameraInstance().startPreview(mSurfaceTexture, this);
        }

        if(mIsCameraBackForward) {
            mYUVDrawer.setFlipScale(-1.0f, 1.0f);
            mYUVDrawer.setRotation((float) (Math.PI / 2.0));
        } else {
            mYUVDrawer.setFlipScale(1.0f, 1.0f);
            mYUVDrawer.setRotation((float) (Math.PI / 2.0));
        }

        resizeTextures();

        // CameraGLSurfaceViewWithTexture
        if (mFrameRecorder == null) {
            Log.e(LOG_TAG, "resumePreview after release!!");
            return;
        }

        if (!cameraInstance().isCameraOpened()) {

            int facing = mIsCameraBackForward ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;

            cameraInstance().tryOpenCamera(new CameraInstance.CameraOpenCallback() {
                @Override
                public void cameraReady() {
                    Log.i(LOG_TAG, "tryOpenCamera OK...");
                }
            }, facing);
        }

        if (!cameraInstance().isPreviewing()) {
            cameraInstance().startPreview(mSurfaceTexture);
            mFrameRecorder.srcResize(cameraInstance().previewHeight(), cameraInstance().previewWidth());
        }

        requestRender();
    }


    public void drawCurrentFrame() {
        if(mYUVDrawer == null) {
            return;
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0,0,0,1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(mDrawViewport.x, mDrawViewport.y, mDrawViewport.width, mDrawViewport.height);
        updateTextures();
        mYUVDrawer.drawTextures();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // CameraGLSurfaceViewWithBuffer
        drawCurrentFrame();

        // CameraGLSurfaceViewWithTexture
        if (mSurfaceTexture == null || !cameraInstance().isPreviewing()) {
            return;
        }

        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        mFrameRecorder.update(mTextureID, mTransformMatrix);

        mFrameRecorder.runProc();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mFrameRecorder.render(mDrawViewport.x, mDrawViewport.y, mDrawViewport.width, mDrawViewport.height);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        synchronized (mBufferUpdateLock) {
            mBufferY.position(0);
            mBufferUV.position(0);
            mBufferY.put(data, 0, mYSize);
            mBufferUV.put(data, mYSize, mUVSize);
            mBufferUpdated = true;
        }

        camera.addCallbackBuffer(data);
    }

    ////////////////////////////////////////////
    //    ////////////////////////////////////////////
    //    ////////////////////////////////////////////
    //    ////////////////////////////////////////////
    //    ////////////////////////////////////////////

    protected int mTextureID;
    protected boolean mIsTransformMatrixSet = false;
    protected CGEFrameRecorder mFrameRecorder;

    public CGEFrameRecorder getRecorder() {
        return mFrameRecorder;
    }


    public synchronized void setFilterWithConfig(final String config) {
        queueEvent(new Runnable() {
            @Override
            public void run() {

                if (mFrameRecorder != null) {
                    mFrameRecorder.setFilterWidthConfig(config);
                } else {
                    Log.e(LOG_TAG, "setFilterWithConfig after release!!");
                }
            }
        });
    }

    public void setFilterIntensity(final float intensity) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mFrameRecorder != null) {
                    mFrameRecorder.setFilterIntensity(intensity);
                } else {
                    Log.e(LOG_TAG, "setFilterIntensity after release!!");
                }
            }
        });
    }

    //定制一些初始化操作
    public void setOnCreateCallback(final CameraGLSurfaceView.OnCreateCallback callback) {
        if (mFrameRecorder == null || callback == null) {
            mOnCreateCallback = callback;
        } else {
            // Already created, just run.
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    callback.createOver();
                }
            });
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);

        if (!cameraInstance().isPreviewing()) {
            resumePreview();
        }
    }



    protected float[] mTransformMatrix = new float[16];


//    protected long mTimeCount2 = 0;
//    protected long mFramesCount2 = 0;
//    protected long mLastTimestamp2 = 0;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        requestRender();

//        if (mLastTimestamp2 == 0)
//            mLastTimestamp2 = System.currentTimeMillis();
//
//        long currentTimestamp = System.currentTimeMillis();
//
//        ++mFramesCount2;
//        mTimeCount2 += currentTimestamp - mLastTimestamp2;
//        mLastTimestamp2 = currentTimestamp;
//        if (mTimeCount2 >= 1000) {
//            Log.i(LOG_TAG, String.format("camera sample rate: %d", mFramesCount2));
//            mTimeCount2 %= 1000;
//            mFramesCount2 = 0;
//        }
    }

    @Override
    protected void onSwitchCamera() {
        super.onSwitchCamera();
        if(mFrameRecorder != null) {
            mFrameRecorder.setSrcRotation((float) (Math.PI / 2.0));
            mFrameRecorder.setRenderFlipScale(1.0f, -1.0f);
        }
    }

    @Override
    public void takeShot(final CameraGLSurfaceView.TakePictureCallback callback) {
        assert callback != null : "callback must not be null!";

        if (mFrameRecorder == null) {
            Log.e(LOG_TAG, "Recorder not initialized!");
            callback.takePictureOK(null);
            return;
        }

        queueEvent(new Runnable() {
            @Override
            public void run() {

                FrameBufferObject frameBufferObject = new FrameBufferObject();
                int bufferTexID;
                IntBuffer buffer;
                Bitmap bmp;

                bufferTexID = Common.genBlankTextureID(mRecordWidth, mRecordHeight);
                frameBufferObject.bindTexture(bufferTexID);
                GLES20.glViewport(0, 0, mRecordWidth, mRecordHeight);
                mFrameRecorder.drawCache();
                buffer = IntBuffer.allocate(mRecordWidth * mRecordHeight);
                GLES20.glReadPixels(0, 0, mRecordWidth, mRecordHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
                bmp = Bitmap.createBitmap(mRecordWidth, mRecordHeight, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);
                Log.i(LOG_TAG, String.format("w: %d, h: %d", mRecordWidth, mRecordHeight));

                frameBufferObject.release();
                GLES20.glDeleteTextures(1, new int[]{bufferTexID}, 0);

                callback.takePictureOK(bmp);
            }
        });

    }

    //isBigger 为true 表示当宽高不满足时，取最近的较大值.
    // 若为 false 则取较小的
    public void setPictureSize(int width, int height, boolean isBigger) {
        //默认会旋转90度.
        cameraInstance().setPictureSize(height, width, isBigger);
    }

    public synchronized void takePicture(final CameraGLSurfaceView.TakePictureCallback photoCallback, Camera.ShutterCallback shutterCallback, final String config, final float intensity, final boolean isFrontMirror) {

        Camera.Parameters params = cameraInstance().getParams();

        if (photoCallback == null || params == null) {
            Log.e(LOG_TAG, "takePicture after release!");
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        try {
            params.setRotation(90);
            cameraInstance().setParams(params);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error when takePicture: " + e.toString());
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        cameraInstance().getCameraDevice().takePicture(shutterCallback, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                Camera.Parameters params = camera.getParameters();
                Camera.Size sz = params.getPictureSize();

                boolean shouldRotate;

                Bitmap bmp;
                int width, height;

                //当拍出相片不为正方形时， 可以判断图片是否旋转
                if (sz.width != sz.height) {
                    //默认数据格式已经设置为 JPEG
                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    width = bmp.getWidth();
                    height = bmp.getHeight();
                    shouldRotate = (sz.width > sz.height && width > height) || (sz.width < sz.height && width < height);
                } else {
                    Log.i(LOG_TAG, "Cache image to get exif.");

                    try {
                        String tmpFilename = getContext().getExternalCacheDir() + "/picture_cache000.jpg";
                        FileOutputStream fileout = new FileOutputStream(tmpFilename);
                        BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
                        bufferOutStream.write(data);
                        bufferOutStream.flush();
                        bufferOutStream.close();

                        ExifInterface exifInterface = new ExifInterface(tmpFilename);
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {
                            //被保存图片exif记录只有旋转90度， 和不旋转两种情况
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                shouldRotate = true;
                                break;
                            default:
                                shouldRotate = false;
                                break;
                        }

                        bmp = BitmapFactory.decodeFile(tmpFilename);
                        width = bmp.getWidth();
                        height = bmp.getHeight();

                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Err when saving bitmap...");
                        e.printStackTrace();
                        return;
                    }
                }


                if (width > mMaxTextureSize || height > mMaxTextureSize) {
                    float scaling = Math.max(width / (float) mMaxTextureSize, height / (float) mMaxTextureSize);
                    Log.i(LOG_TAG, String.format("目标尺寸(%d x %d)超过当前设备OpenGL 能够处理的最大范围(%d x %d)， 现在将图片压缩至合理大小!", width, height, mMaxTextureSize, mMaxTextureSize));

                    bmp = Bitmap.createScaledBitmap(bmp, (int) (width / scaling), (int) (height / scaling), false);

                    width = bmp.getWidth();
                    height = bmp.getHeight();
                }

                Bitmap bmp2;

                if (shouldRotate) {
                    bmp2 = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bmp2);

                    if (cameraInstance().getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Matrix mat = new Matrix();
                        int halfLen = Math.min(width, height) / 2;
                        mat.setRotate(90, halfLen, halfLen);
                        canvas.drawBitmap(bmp, mat, null);
                    } else {
                        Matrix mat = new Matrix();

                        if (isFrontMirror) {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(-1.0f, 1.0f);
                            mat.postTranslate(width / 2, height / 2);
                            int halfLen = Math.min(width, height) / 2;
                            mat.postRotate(90, halfLen, halfLen);
                        } else {
                            int halfLen = Math.max(width, height) / 2;
                            mat.postRotate(-90, halfLen, halfLen);
                        }

                        canvas.drawBitmap(bmp, mat, null);
                    }

                    bmp.recycle();
                } else {
                    if (cameraInstance().getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        bmp2 = bmp;
                    } else {

                        bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bmp2);
                        Matrix mat = new Matrix();
                        if (isFrontMirror) {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(1.0f, -1.0f);
                            mat.postTranslate(width / 2, height / 2);
                        } else {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(-1.0f, -1.0f);
                            mat.postTranslate(width / 2, height / 2);
                        }

                        canvas.drawBitmap(bmp, mat, null);
                    }

                }

                if (config != null) {
                    CGENativeLibrary.filterImage_MultipleEffectsWriteBack(bmp2, config, intensity);
                }

                photoCallback.takePictureOK(bmp2);

                cameraInstance().getCameraDevice().startPreview();
            }
        });
    }
}
