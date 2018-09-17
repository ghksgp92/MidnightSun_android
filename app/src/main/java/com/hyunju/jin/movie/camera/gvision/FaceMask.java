package com.hyunju.jin.movie.camera.gvision;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.hyunju.jin.movie.R;

/**
 * 얼굴인식 마스크 필터 클래스
 * 모든 얼굴인식 마스크 필터가 이곳에 모여있다.
 */
public class FaceMask extends GraphicOverlay.Graphic {

    private static final String TAG = "FaceMask";

    private boolean mIsFrontFacing; // 현재 사용중인 카메라 정보.

    // This variable may be written to by one of many threads. By declaring it as volatile, we guarantee that when we read its contents, we're reading the most recent "write" by any thread.
    // 이 주석이 말하는 의미를 모르겠음.
    private volatile FaceData mFaceData;

    // 인식된 얼굴 크기에 맞게 마스크를 조정할 때 사용한다.
    final float HAT_FACE_WIDTH_RATIO = (float) (1.0 / 4.0);
    final float HAT_FACE_HEIGHT_RATIO = (float) (1.0 / 6.0);
    final float HAT_CENTER_Y_OFFSET_FACTOR = (float) (1.0 / 8.0);

    private Paint mHintTextPaint;
    private Paint mHintOutlinePaint;
    private Paint mEyeWhitePaint; // 눈을 뜨고 있을 때 눈 색
    private Paint mIrisPaint; // 눈 뜨고 있을 때 눈동자 색
    private Paint mEyeOutlinePaint; // 눈 외곽 라인
    private Paint mEyelidPaint; // 눈 감았을 때 눈 색

    // We want each iris to move independently,
    // so each one gets its own physics engine.
    private EyePhysics mLeftPhysics = new EyePhysics(); // 왼쪽 눈의.. ??
    private EyePhysics mRightPhysics = new EyePhysics();  // 오른쪽 눈의 ..?

    private int currentMaskPosition;    // 현재 사용중인 마스크 종류를 나타낸다.

    public FaceMask(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
        super(overlay);
        mIsFrontFacing = isFrontFacing;
        Resources resources = context.getResources();
        initializeGraphics(resources);
        currentMaskPosition = 0;
    }

    public int getCurrentMaskPosition() {
        return currentMaskPosition;
    }

    public void setCurrentMaskPosition(int currentMaskPosition) {
        this.currentMaskPosition = currentMaskPosition;
    }

    // 이미지 요소
    // 필터1 에서 사용하는 이미지 요소
    private Drawable mask1EarLeftGraphic;
    private Drawable mask1EarRightGraphic;
    private Drawable mask1NoseGraphic;
    // 필터2 에서 사용하는 이미지 요소
    private Drawable mask2EarLeftGraphic;
    private Drawable mask2EarRightGraphic;
    private Drawable mask2CheckGraphic;
    private Drawable mask2RainbowMouthGraphic;
    // 필터3 에서 사용하는 이미지 요소
    private Drawable mask3Graphic;
    // 필터4 에서 사용하는 이미지 요소
    private Drawable mask4Graphic;
    // 필터5 에서 사용하는 이미지 요소
    private Drawable mask5LeftEarGraphic;
    private Drawable mask5RightEarGraphic;
    private Drawable mask5EyeGraphic;
    private Drawable mask5NoseGraphic;
    private Drawable mask5HeartGraphic;

    /**
     * 인식된 얼굴 랜드마크 중 그릴 이미지 요소 로드.
     * @param resources
     */
    private void initializeGraphics(Resources resources) {

        // 필터1 에서 사용하는 이미지 요소 로드
        mask1EarLeftGraphic = resources.getDrawable(R.drawable.mask1_ear_left);
        mask1EarRightGraphic = resources.getDrawable(R.drawable.mask1_ear_right);
        mask1NoseGraphic = resources.getDrawable(R.drawable.mask1_nose);

        // 필터2 에서 사용하는 이미지 요소 로드.
        mask2EarLeftGraphic = resources.getDrawable(R.drawable.mask2_hat_left);
        mask2EarRightGraphic = resources.getDrawable(R.drawable.mask2_hat_right);
        mask2CheckGraphic = resources.getDrawable(R.drawable.mask2_check);
        mask2RainbowMouthGraphic = resources.getDrawable(R.drawable.mask_rainbow_mouth);

        // 필터3 에서 사용하는 이미지 요소 로드.
        mask3Graphic = resources.getDrawable(R.drawable.mask3);

        // 필터4 에서 사용하는 이미지 요소 로드.
        mask4Graphic = resources.getDrawable(R.drawable.mask4);

        // 필터5 에서 사용하는 이미지 요소 로드.
        mask5LeftEarGraphic = resources.getDrawable(R.drawable.mask5_left_ear);
        mask5RightEarGraphic = resources.getDrawable(R.drawable.mask5_right_ear);
        mask5EyeGraphic = resources.getDrawable(R.drawable.mask5_eye);  // 이거 안경인데 필터1 하고 중복되서 현재 사용 안함.
        mask5NoseGraphic = resources.getDrawable(R.drawable.mask5_mouth);
        mask5HeartGraphic = resources.getDrawable(R.drawable.mask5_heart);  // 웃을때 보여줄 하트 이펙트
    }

    /**
     *  얼굴인식 결과가 업데이트되면 호출된다.
     */
    void update(FaceData faceData) {
        mFaceData = faceData;
        postInvalidate(); // 화면을 다시 그리도록 트리거함. draw() 가 호출된다.
    }

    @Override
    public void draw(Canvas canvas) {

        // mFaceData 값은 매순간 값이 바뀌기 때문에 현 시점의 얼굴 인식 정보를 복제한다.
        FaceData faceData = mFaceData;

        // 얼굴 인식 정보가 있는지 확인한다. null 이면 마스크를 그릴 수 없다.
        if (faceData == null) {
            return;
        }

        // 각 위치가 정확히 어떤 부분을 말하는지는 FaceTracker.onUpdate() 메서드에서 확인할 수 있다.
        // 아래 위치는 마스크를 그리기 위해 필수적인 위치임.
        PointF detectPosition = faceData.getPosition(); // 얼굴의 좌상단 위치
        PointF detectLeftEyePosition = faceData.getLeftEyePosition();
        PointF detectRightEyePosition = faceData.getRightEyePosition();
        PointF detectNoseBasePosition = faceData.getNoseBasePosition();
        PointF detectMouthLeftPosition = faceData.getMouthLeftPosition();
        PointF detectMouthBottomPosition = faceData.getMouthBottomPosition();
        PointF detectMouthRightPosition = faceData.getMouthRightPosition();

        // 어떻게 이런 괄호가 가능한지, 왜 한건지 모르겠다.
        {
            if ((detectPosition == null) || (detectLeftEyePosition == null) || (detectRightEyePosition == null) || (detectNoseBasePosition == null) ||
                    (detectMouthLeftPosition == null) || (detectMouthBottomPosition == null) || (detectMouthRightPosition == null)) {
                return;

            }
        }

        // 카메라에서 인식된 좌표를 View 좌표로 변환한다. 여기서 말하는 View 는 GraphicOverlay 다.

        // 얼굴 위치, 치수 및 각도
        PointF facePosition = new PointF(translateX(detectPosition.x), translateY(detectPosition.y));
        float faceWidth = scaleX(faceData.getWidth());
        float faceHeight = scaleY(faceData.getHeight());

        // 눈 위치
        PointF leftEyePosition = new PointF(translateX(detectLeftEyePosition.x), translateY(detectLeftEyePosition.y));
        PointF rightEyePosition = new PointF(translateX(detectRightEyePosition.x), translateY(detectRightEyePosition.y));

        // 눈 상태. 왼쪽 오른쪽 별개로 관리한다.
        boolean leftEyeOpen = faceData.isLeftEyeOpen();
        boolean rightEyeOpen = faceData.isRightEyeOpen();

        // 코 위치
        PointF noseBasePosition = new PointF(translateX(detectNoseBasePosition.x), translateY(detectNoseBasePosition.y));

        // 입술 위치
        PointF mouthLeftPosition = new PointF(translateX(detectMouthLeftPosition.x), translateY(detectMouthLeftPosition.y));
        PointF mouthRightPosition = new PointF(translateX(detectMouthRightPosition.x), translateY(detectMouthRightPosition.y));
        PointF mouthBottomPosition = new PointF(translateX(detectMouthBottomPosition.x), translateY(detectMouthBottomPosition.y));

        // 웃는 중인지?
        boolean smiling = faceData.isSmiling();

        // 얼굴 경사
        float eulerY = faceData.getEulerY();    // 왼쪽 오른쪽 회전 정도
        float eulerZ = faceData.getEulerZ();

        // 피타고라스 공식을 사용하여 눈 사이의 거리를 계산한다.
        final float EYE_RADIUS_PROPORTION = 0.45f;
        final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;

        // 3개의 꼭지점이 필요한데 양쪽 눈동자와 양쪽 눈동자 가운데 위치로 거리를 구한다? 피타고라스 정의가 뭐더라요
        float distance = (float) Math.sqrt( (rightEyePosition.x - leftEyePosition.x) * (rightEyePosition.x - leftEyePosition.x) + (rightEyePosition.y - leftEyePosition.y) * (rightEyePosition.y - leftEyePosition.y));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        // 실제로 인식된 위치에 마스크를 그린다.
        // 마스크별로 필요한 데이터가 다르기 때문에 현재 마스크 종류에따라 필요한 메서드를 호출한다.

        if(currentMaskPosition == 1){
            // 머리띠처럼 동물 귀를 그린다.
            PointF leftIrisPosition = mLeftPhysics.nextIrisPosition(leftEyePosition, eyeRadius, irisRadius);
            drawEyeMaskPosition1(canvas, "left", leftEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, leftIrisPosition);
            PointF rightIrisPosition = mRightPhysics.nextIrisPosition(rightEyePosition, eyeRadius, irisRadius);
            drawEyeMaskPosition1(canvas, "right", rightEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, rightIrisPosition);

            // 코 위치를 기반으로 안경과 동물 코와 입을 그린다.
            drawNoseMaskPosition1(canvas, mouthBottomPosition, noseBasePosition, leftEyePosition, rightEyePosition, faceWidth);

            final float HEAD_TILT_HAT_THRESHOLD = 15.0f;
            //Log.e(TAG, "기울기 "+Math.abs(eulerZ)+"");
            if (Math.abs(eulerZ) > HEAD_TILT_HAT_THRESHOLD) {
                //drawHat(canvas, facePosition, faceWidth, faceHeight, noseBasePosition);
            }

        }else if(currentMaskPosition == 2) {
            // 귀와 뿔을 그린다.
            PointF leftIrisPosition = mLeftPhysics.nextIrisPosition(leftEyePosition, eyeRadius, irisRadius);
            drawMask2Hat(canvas, "left", leftEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, leftIrisPosition);
            PointF rightIrisPosition = mRightPhysics.nextIrisPosition(rightEyePosition, eyeRadius, irisRadius);
            drawMask2Hat(canvas, "right", rightEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, rightIrisPosition);

            // 볼터치 위치
            PointF detectLeftCheekPosition = faceData.getLeftCheekPosition();
            PointF detectRightCheekPosition = faceData.getRightCheekPosition();
            if ((detectLeftCheekPosition == null) || (detectLeftCheekPosition == null) ){
                return;
            }
            PointF leftCheckPosition = new PointF(translateX(detectLeftCheekPosition.x), translateY(detectLeftCheekPosition.y));
            PointF rightCheckPosition = new PointF(translateX(detectRightCheekPosition.x), translateY(detectRightCheekPosition.y));

            // 볼터치를 그린다.
            drawMask2Check(canvas, leftCheckPosition, rightCheckPosition, facePosition, faceWidth, faceHeight);

            // 입을 벌리면 무지개토를 한다.
            drawMask2Mouth(canvas, noseBasePosition, leftEyePosition, mouthLeftPosition, mouthRightPosition, mouthBottomPosition, faceHeight, faceWidth);

        }else if(currentMaskPosition == 3){
            drawMask3(canvas, mouthBottomPosition, rightEyePosition, noseBasePosition, facePosition, faceWidth, faceHeight);

        }else if(currentMaskPosition == 4){
            drawMask4Nose(canvas, mouthBottomPosition, rightEyePosition, noseBasePosition, facePosition, faceWidth, faceHeight);

        }else if(currentMaskPosition == 5){
            // 눈을 그린다.
            PointF leftIrisPosition = mLeftPhysics.nextIrisPosition(leftEyePosition, eyeRadius, irisRadius);
            drawMask5Eye(canvas, "left", leftEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, leftIrisPosition);
            PointF rightIrisPosition = mRightPhysics.nextIrisPosition(rightEyePosition, eyeRadius, irisRadius);
            drawMask5Eye(canvas, "right", leftEyeOpen, smiling, facePosition, faceWidth, faceHeight, noseBasePosition, rightIrisPosition);

            // 코를 그린다.
            drawMask5Mouth(canvas, mouthBottomPosition, noseBasePosition, leftEyePosition, rightEyePosition, faceWidth);

            // 웃을 경우 얼굴 주변에 하트를 띄운다
            drawMask5Heart(canvas, smiling, facePosition, faceWidth, faceHeight);
        }else{
            // 필터 선택 안하거나 잘못된 필터 번호일 경우. 아무것도 안한다.
        }

    }

    /*
        얼굴 인식 마스크 1번 선택 시
     */

    private void drawEyeMaskPosition1(Canvas canvas, String type, boolean eyeOpen, boolean smiling, PointF facePosition, float faceWidth, float faceHeight, PointF noseBasePosition, PointF eyePosition) {
          if (smiling) {  // 웃으면 귀가 나온다.

              float hatCenterY = facePosition.y + (faceHeight * HAT_CENTER_Y_OFFSET_FACTOR);
              float hatWidth = faceWidth * HAT_FACE_WIDTH_RATIO;
              float hatHeight = faceHeight * HAT_FACE_HEIGHT_RATIO;

              int left = (int) (eyePosition.x - (hatWidth / 2));
              int right = (int) (eyePosition.x + (hatWidth / 2));

              int top = (int) (hatCenterY - (hatHeight / 2));
              int bottom = (int) (hatCenterY + (hatHeight / 2));

              if ("left".equals(type)) {
                  mask1EarLeftGraphic.setBounds(left, top, right, bottom);
                  mask1EarLeftGraphic.draw(canvas);
              } else {
                  mask1EarRightGraphic.setBounds(left, top, right, bottom);
                  mask1EarRightGraphic.draw(canvas);
              }
          }
    }

    private void drawNoseMaskPosition1(Canvas canvas, PointF mouthBottomPosition, PointF noseBasePosition, PointF leftEyePosition, PointF rightEyePosition, float faceWidth) {
        float noseWidth = faceWidth;

        // 코 크기를 사각형으로 보고 왼쪽 오른쪽 위 아래를 구하는데.. 음..

        // noseBasePosition 은 코의 정 가운데를 말하는건가?
        int left = (int)(noseBasePosition.x - (noseWidth / 2));
        int right = (int)(noseBasePosition.x + (noseWidth / 2));

        int top = (int)(leftEyePosition.y + rightEyePosition.y) / 3;
        int bottom = (int)mouthBottomPosition.y;

        mask1NoseGraphic.setBounds(left, top, right, bottom);
        mask1NoseGraphic.draw(canvas);
    }


    /*
        얼굴 인식 마스크 2번 선택 시
     */
    private void drawMask2Hat(Canvas canvas, String type, boolean eyeOpen, boolean smiling, PointF facePosition, float faceWidth, float faceHeight, PointF noseBasePosition, PointF eyePosition) {

        // 메서드 명이 눈 마스크를 그리는데 사실 귀와 뿔을 그린다.

        float hatCenterY = facePosition.y + (faceHeight * HAT_CENTER_Y_OFFSET_FACTOR);
        float hatWidth = faceWidth * HAT_FACE_WIDTH_RATIO;
        float hatHeight = faceHeight * HAT_FACE_HEIGHT_RATIO;

        int top = (int)(hatCenterY - (hatHeight / 2));
        int bottom = (int)(hatCenterY + (hatHeight / 2));

        if( "left".equals(type)){
            int left = (int)(eyePosition.x - (hatWidth / 2));
            int right = (int)(eyePosition.x + (hatWidth / 2));

            mask2EarLeftGraphic.setBounds(left, top, right, bottom);
            mask2EarLeftGraphic.draw(canvas);

        }else if("right".equals(type)){
            int left = (int)(eyePosition.x - (hatWidth / 2));
            int right = (int)(eyePosition.x + (hatWidth / 2));

            mask2EarRightGraphic.setBounds(left, top, right, bottom);
            mask2EarRightGraphic.draw(canvas);
        }else{

        }
    }

    private void drawMask2Check(Canvas canvas, PointF leftCheckPosition, PointF rightCheckPosition, PointF facePosition, float faceWidth, float faceHeight){

        // 메서드 명이 눈 마스크를 그리는데 사실 귀와 뿔을 그린다.

        float hatWidth = faceWidth * HAT_FACE_WIDTH_RATIO;
        float hatHeight = faceHeight * HAT_FACE_HEIGHT_RATIO;

        int left = (int)(leftCheckPosition.x - (hatWidth / 2));
        int right = (int)(leftCheckPosition.x + (hatWidth / 2));
        int top = (int)(leftCheckPosition.y - (hatHeight / 2));
        int bottom = (int)(leftCheckPosition.y + (hatHeight / 2));
        mask2CheckGraphic.setBounds(left, top, right, bottom);
        mask2CheckGraphic.draw(canvas);

        left = (int)(rightCheckPosition.x - (hatWidth / 2));
        right = (int)(rightCheckPosition.x + (hatWidth / 2));
        top = (int)(rightCheckPosition.y - (hatHeight / 2));
        bottom = (int)(rightCheckPosition.y + (hatHeight / 2));
        mask2CheckGraphic.setBounds(left, top, right, bottom);
        mask2CheckGraphic.draw(canvas);
    }


    private void drawMask2Mouth(Canvas canvas, PointF noseBasePosition, PointF leftEyePosition, PointF mouthLeftPosition , PointF mouthRightPosition, PointF mouthBottomPosition, float faceHeight, float faceWidth){
        //drawMask2Mouth(canvas, noseBasePosition, leftEyePosition, mouthLeftPosition, mouthRightPosition, mouthBottomPosition, faceHeight);

        float mouthOpenHeight = faceWidth * HAT_CENTER_Y_OFFSET_FACTOR;
        Log.e("hatHeight - ", "hatHeight("+mouthOpenHeight);
        float mouthPositionY = (mouthLeftPosition.y + mouthRightPosition.y ) / 2;

        Log.e("mouthBottomPosition, Y", "mouthBottomPosition: "+mouthBottomPosition+", mouthPositionY:"+mouthPositionY);
        if( Math.abs( mouthBottomPosition.y - mouthPositionY) > mouthOpenHeight){   // mouthOpenHeight 값이 작을수록 더 정확도가 높겠다.

            float noseWidth = faceWidth;

            int left = (int)(noseBasePosition.x - (noseWidth / 2));
            int right = (int)(noseBasePosition.x + (noseWidth / 2));

            int top = (int) (leftEyePosition.y - (leftEyePosition.y * 0.3));
            int bottom = (int) (mouthBottomPosition.y + (mouthBottomPosition.y * 0.5));

            // 코 부분부터 볼터치, 얼굴 보다 조금 더 길게 무지개 토
            mask2RainbowMouthGraphic.setBounds(left, top, right, bottom);
            mask2RainbowMouthGraphic.draw(canvas);
        }
    }

    /*
       얼굴 인식 마스크 3번 선택시
     */

    private void drawMask3(Canvas canvas, PointF mouthBottomPosition, PointF rightEyePosition, PointF noseBasePosition, PointF facePosition, float faceWidth, float faceHeight) {

        // noseBasePosition 은 코의 정 가운데를 말하는건가?
        int left = (int)(noseBasePosition.x - (faceWidth / 2));
        int right = (int)(noseBasePosition.x + (faceWidth / 2) );

        //int top = (int)(leftEyePosition.y + rightEyePosition.y) / 5;
        int top = (int) facePosition.y;
        int bottom = (int) (rightEyePosition.y + (rightEyePosition.y / 4));

        mask3Graphic.setBounds(left, top, right, bottom);
        mask3Graphic.draw(canvas);
    }

     /*
       얼굴 인식 마스크 3번 선택시
     */

    private void drawMask4Nose(Canvas canvas, PointF mouthBottomPosition, PointF rightEyePosition, PointF noseBasePosition, PointF facePosition, float faceWidth, float faceHeight) {
        float noseWidth = faceWidth;

        // 코 크기를 사각형으로 보고 왼쪽 오른쪽 위 아래를 구하는데.. 음..

        // noseBasePosition 은 코의 정 가운데를 말하는건가?
        int left = (int)(noseBasePosition.x - (noseWidth / 2));
        int right = (int)(noseBasePosition.x + (noseWidth / 2));

        int top = (int) facePosition.y;
        int bottom = (int) (rightEyePosition.y + (rightEyePosition.y / 4));

        mask4Graphic.setBounds(left, top, right, bottom);
        mask4Graphic.draw(canvas);
    }



    /*
        얼굴 인식 마스크 5번 선택시
     */

    private void drawMask5Eye(Canvas canvas, String type, boolean eyeOpen, boolean smiling, PointF facePosition, float faceWidth, float faceHeight, PointF noseBasePosition, PointF eyePosition) {

            float hatCenterY = facePosition.y + (faceHeight * HAT_CENTER_Y_OFFSET_FACTOR);
            float hatWidth = faceWidth * (float) (1.0 / 3.0);
            float hatHeight = faceHeight * HAT_FACE_HEIGHT_RATIO;

            int left = (int) (eyePosition.x - (hatWidth / 2));
            int right = (int) (eyePosition.x + (hatWidth / 2));

            int top = (int) (hatCenterY - (hatHeight / 2));
            int bottom = (int) (hatCenterY + (hatHeight / 2));

            if ("left".equals(type)) {
                mask5LeftEarGraphic.setBounds(left, top, right, bottom);
                mask5LeftEarGraphic.draw(canvas);
            } else {
                mask5RightEarGraphic.setBounds(left, top, right, bottom);
                mask5RightEarGraphic.draw(canvas);
            }

    }



    private void drawMask5Mouth(Canvas canvas, PointF mouthBottomPosition, PointF noseBasePosition, PointF leftEyePosition, PointF rightEyePosition, float faceWidth) {
        float noseWidth = faceWidth;

        // 코 크기를 사각형으로 보고 왼쪽 오른쪽 위 아래를 구하는데.. 음..

        // noseBasePosition 은 코의 정 가운데를 말하는건가?
        int left = (int)(noseBasePosition.x - (noseWidth / 2));
        int right = (int)(noseBasePosition.x + (noseWidth / 2));

        int top = (int) leftEyePosition.y;
        int bottom = (int)mouthBottomPosition.y;

        mask5NoseGraphic.setBounds(left, top, right, bottom);
        mask5NoseGraphic.draw(canvas);
    }

    private void drawMask5Heart(Canvas canvas, boolean smiling, PointF facePosition, float faceWidth, float faceHeight){
        if(smiling){    // 웃고 있는 경우에만 하트를 그린다.

            int right = (int) (facePosition.x + ( faceWidth * HAT_FACE_WIDTH_RATIO ));
            int bottom = (int) (facePosition.y + faceHeight);
            mask5HeartGraphic.setBounds((int) facePosition.x, (int) facePosition.y, right, bottom);
            mask5HeartGraphic.draw(canvas);
        }
    }
}
