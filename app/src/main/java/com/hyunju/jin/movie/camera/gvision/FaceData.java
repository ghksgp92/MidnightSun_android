/*
 * Copyright (c) 2017 Razeware LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish, 
 * distribute, sublicense, create a derivative work, and/or sell copies of the 
 * Software in any work that is designed, intended, or marketed for pedagogical or 
 * instructional purposes related to programming, coding, application development, 
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works, 
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hyunju.jin.movie.camera.gvision;

import android.graphics.PointF;

/**
 * 구글 Face API 를 이용한 얼굴 인식 결과를 저장하는 데이터 클래스
 * 구글 공식 Face API 문서 https://developers.google.com/vision/face-detection-concepts
 *
 * 얼굴 랜드마크에 정확한 위치는 아래 링크 중간에서 사진으로 확인할 수 있다.
 * https://www.raywenderlich.com/523-augmented-reality-in-android-with-google-s-face-api
 */

public class FaceData {

    private static final String TAG = "FaceData";

    private int mId;

    // 얼굴 면적 정보
    private PointF mPosition; // 얼굴의 좌상단 좌표
    private float mWidth;     // 얼굴 가로 길이
    private float mHeight;    // 얼굴 세로 길이

    // 얼굴 방향 정보. 오일러각이라고 함. 현재 오일러 X 각은 제공되지 않는다.
    private float mEulerY;
    private float mEulerZ;

    // 얼굴 특징 정보. Face API 에서는 [Classification] 라고 함
    private boolean mLeftEyeOpen;
    private boolean mRightEyeOpen;
    private boolean mSmiling;

    // 얼굴 랜드마크. 더 정확히 어떤 위치를 말하는지 찾아서 주석 제대로 달아야함.
    private PointF mLeftEyePosition;    // 왼쪽 눈동자 위치
    private PointF mRightEyePosition;   // 오른쪽 눈동자 위치
    private PointF mLeftCheekPosition;  // 왼쪽 볼 가운데 위치 (눈동자 위치와 X 좌표 동일)
    private PointF mRightCheekPosition; // 오른쪽 볼 가운데 위치 (눈동자 위치와 X 좌표 동일)
    private PointF mNoseBasePosition;   // 콧구멍 사이 코 가장 아랫부분 위치. 그냥 콧구멍 사이라고 보면 될듯.
    private PointF mLeftEarPosition;    // 왼쪽 귀 중앙 위치
    private PointF mLeftEarTipPosition; // 왼쪽 귀 상단 위치 (귀 중앙위치와 X 좌표 동일)
    private PointF mRightEarPosition;   // 오른쪽 귀 중앙 위치
    private PointF mRightEarTipPosition;    // 오른쪽 귀 상단 위치 (귀 중앙위치와 X 좌표 동일)
    private PointF mMouthLeftPosition;      // 왼쪽 입꼬리 위치
    private PointF mMouthBottomPosition;    // 아랫입술 정중앙
    private PointF mMouthRightPosition;     // 오른쪽 입꼬리 위치

    /*
    getter, setter 정의
    */

    public int getId() {
    return mId;
    }

    public void setId(int id) {
    mId = id;
    }

    public PointF getPosition() {
    return mPosition;
    }

    public void setPosition(PointF position) {
    mPosition = position;
    }

    public float getWidth() {
    return mWidth;
    }

    public void setWidth(float width) {
    mWidth = width;
    }

    public float getHeight() {
    return mHeight;
    }

    public void setHeight(float height) {
    mHeight = height;
    }

    public float getEulerY() {
    return mEulerY;
    }

    public void setEulerY(float eulerY) {
    mEulerY = eulerY;
    }

    public float getEulerZ() {
    return mEulerZ;
    }

    public void setEulerZ(float eulerZ) {
    mEulerZ = eulerZ;
    }

    public boolean isLeftEyeOpen() {
    return mLeftEyeOpen;
    }

    public void setLeftEyeOpen(boolean leftEyeOpen) {
    this.mLeftEyeOpen = leftEyeOpen;
    }

    public boolean isRightEyeOpen() {
    return mRightEyeOpen;
    }

    public void setRightEyeOpen(boolean rightEyeOpen) {
    this.mRightEyeOpen = rightEyeOpen;
    }

    public boolean isSmiling() {
    return mSmiling;
    }

    public void setSmiling(boolean smiling) {
    this.mSmiling = smiling;
    }

    public PointF getLeftEyePosition() {
    return mLeftEyePosition;
    }

    public void setLeftEyePosition(PointF leftEyePosition) {
    this.mLeftEyePosition = leftEyePosition;
    }

    public PointF getRightEyePosition() {
    return mRightEyePosition;
    }

    public void setRightEyePosition(PointF rightEyePosition) {
        this.mRightEyePosition = rightEyePosition;
    }

    public PointF getLeftCheekPosition() {
    return mLeftCheekPosition;
    }

    public void setLeftCheekPosition(PointF leftCheekPosition) {
        mLeftCheekPosition = leftCheekPosition;
    }

    public PointF getRightCheekPosition() {
    return mRightCheekPosition;
    }

    public void setRightCheekPosition(PointF rightCheekPosition) {
        mRightCheekPosition = rightCheekPosition;
    }

    public PointF getNoseBasePosition() {
    return mNoseBasePosition;
    }

    public void setNoseBasePosition(PointF noseBasePosition) {
        this.mNoseBasePosition = noseBasePosition;
    }

    public PointF getLeftEarPosition() {
    return mLeftEarPosition;
    }

    public void setLeftEarPosition(PointF leftEarPosition) {
    mLeftEarPosition = leftEarPosition;
    }

    public PointF getLeftEarTipPosition() {
    return mLeftEarTipPosition;
    }

    public void setLeftEarTipPosition(PointF leftEarTipPosition) {
        mLeftEarTipPosition = leftEarTipPosition;
    }

    public PointF getRightEarPosition() {
    return mRightEarPosition;
    }

    public void setRightEarPosition(PointF rightEarPosition) {
        mRightEarPosition = rightEarPosition;
    }

    public PointF getRightEarTipPosition() {
    return mRightEarTipPosition;
    }

    public void setRightEarTipPosition(PointF rightEarTipPosition) {
        mRightEarTipPosition = rightEarTipPosition;
    }

    public PointF getMouthLeftPosition() {
    return mMouthLeftPosition;
    }

    public void setMouthLeftPosition(PointF mouthLeftPosition) {
    this.mMouthLeftPosition = mouthLeftPosition;
    }

    public PointF getMouthBottomPosition() {
    return mMouthBottomPosition;
    }

    public void setMouthBottomPosition(PointF mouthBottomPosition) {
        this.mMouthBottomPosition = mouthBottomPosition;
    }

    public PointF getMouthRightPosition() {
    return mMouthRightPosition;
    }

    public void setMouthRightPosition(PointF mouthRightPosition) {
        this.mMouthRightPosition = mouthRightPosition;
    }

}
