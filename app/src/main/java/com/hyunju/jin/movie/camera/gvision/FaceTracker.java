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

import android.content.Context;
import android.graphics.PointF;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.HashMap;
import java.util.Map;

/**
 *  카메라에서 감지된 얼굴의 위치와 랜드마크를 수집하는 클래스
 */
public class FaceTracker extends Tracker<Face> {

  private static final String TAG = "FaceTracker";

  private GraphicOverlay mOverlay;
  private FaceMask mFaceGraphic;
  private Context mContext;
  private boolean mIsFrontFacing;
  private FaceData mFaceData;

  /*
    일시적으로 얼굴을 추적할 수 없는 경우 (얼굴이 너무 빨리 움직여서 인식하기 어렵거나 화면상에서 사라질 때)
    이전에 감지된 얼굴인식 정보를 사용하도록 한다.
   */
  private Map<Integer, PointF> mPreviousLandmarkPositions = new HashMap<>(); // 이전에 감지된 얼굴 랜드마크를 저장해두는 Map (랜드마크가 여러개이기 때문에 Map 형태를 사용함)

  private boolean mPreviousIsLeftEyeOpen = true;    // 이전에 감지된 왼쪽 눈의 오픈 상태를 저장해둔다.
  private boolean mPreviousIsRightEyeOpen = true;   // 이전에 감지된 오른쪽 눈의 오픈 상태를 저장해둔다.

  public FaceMask getmFaceGraphic() {
    return mFaceGraphic;
  }

  public void setmFaceGraphic(FaceMask mFaceGraphic) {
    this.mFaceGraphic = mFaceGraphic;
  }

  public FaceTracker(GraphicOverlay overlay, Context context, boolean isFrontFacing) {
    mOverlay = overlay;
    mContext = context;
    mIsFrontFacing = isFrontFacing;
    mFaceData = new FaceData();
  }

  // 얼굴인식 이벤트 핸들러

  /**
   *  새로운 얼굴이 감지되고 얼굴 추적을 시작하면 호출된다.
   */
  @Override
  public void onNewItem(int id, Face face) {
    // 감지된 얼굴 랜드마크에 그릴 그래픽 요소를 생성한다.
    mFaceGraphic = new FaceMask(mOverlay, mContext, mIsFrontFacing);
  }

  /**
   *  감지된 얼굴에서 얼굴 랜드마크, 특징 정보가 변경될 때 호출되어 얼굴 마스크를 업데이트한다.
   */
  @Override
  public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
    /*
      Face 클래스 문서 https://developers.google.com/android/reference/com/google/android/gms/vision/face/Face.html#getLandmarks()
      Face API 에 대한 구글 공식문서 https://developers.google.com/vision/face-detection-concepts
     */

    mOverlay.add(mFaceGraphic);
    updatePreviousLandmarkPositions(face);

    // Get face dimensions.
    mFaceData.setPosition(face.getPosition());
    mFaceData.setWidth(face.getWidth());
    mFaceData.setHeight(face.getHeight());

    // Get head angles.
    mFaceData.setEulerY(face.getEulerY());
    mFaceData.setEulerZ(face.getEulerZ());

    // 눈 위치
    mFaceData.setLeftEyePosition(getLandmarkPosition(face, Landmark.LEFT_EYE));
    mFaceData.setRightEyePosition(getLandmarkPosition(face, Landmark.RIGHT_EYE));

    mFaceData.setLeftCheekPosition(getLandmarkPosition(face, Landmark.LEFT_CHEEK));
    mFaceData.setRightCheekPosition(getLandmarkPosition(face, Landmark.RIGHT_CHEEK));

    // 코 가운데 위치?
    mFaceData.setNoseBasePosition(getLandmarkPosition(face, Landmark.NOSE_BASE));

    // 귀 위치. EAR_TIP 이 뭘까?
    mFaceData.setMouthBottomPosition(getLandmarkPosition(face, Landmark.LEFT_EAR));
    mFaceData.setMouthBottomPosition(getLandmarkPosition(face, Landmark.LEFT_EAR_TIP));
    mFaceData.setMouthBottomPosition(getLandmarkPosition(face, Landmark.RIGHT_EAR));
    mFaceData.setMouthBottomPosition(getLandmarkPosition(face, Landmark.RIGHT_EAR_TIP));

    // 입술 위치. 왼쪽 오른쪽 아랫입? 아랫입 위치로 뭘 알수있지?
    mFaceData.setMouthLeftPosition(getLandmarkPosition(face, Landmark.LEFT_MOUTH));
    mFaceData.setMouthBottomPosition(getLandmarkPosition(face, Landmark.BOTTOM_MOUTH));
    mFaceData.setMouthRightPosition(getLandmarkPosition(face, Landmark.RIGHT_MOUTH));

    // 눈을 떴는지 계산한다.
    final float EYE_CLOSED_THRESHOLD = 0.4f;
    float leftOpenScore = face.getIsLeftEyeOpenProbability();
    if (leftOpenScore == Face.UNCOMPUTED_PROBABILITY) {
      mFaceData.setLeftEyeOpen(mPreviousIsLeftEyeOpen);
    } else {
      mFaceData.setLeftEyeOpen(leftOpenScore > EYE_CLOSED_THRESHOLD);
      mPreviousIsLeftEyeOpen = mFaceData.isLeftEyeOpen();
    }

    float rightOpenScore = face.getIsRightEyeOpenProbability();
    if (rightOpenScore == Face.UNCOMPUTED_PROBABILITY) {
      mFaceData.setRightEyeOpen(mPreviousIsRightEyeOpen);
    } else {
      mFaceData.setRightEyeOpen(rightOpenScore > EYE_CLOSED_THRESHOLD);
      mPreviousIsRightEyeOpen = mFaceData.isRightEyeOpen();
    }

    // 웃고있는지 계산한다.
    final float SMILING_THRESHOLD = 0.8f;
    mFaceData.setSmiling(face.getIsSmilingProbability() > SMILING_THRESHOLD);

    // 인식된 얼굴 랜드마크에 마스크를 그린다.
    mFaceGraphic.update(mFaceData);
  }

  /**
   *  얼굴이 감지되지 않을 때 호출된다.
   */
  @Override
  public void onMissing(FaceDetector.Detections<Face> detectionResults) {
    // 현재 화면에 얼굴이 없으므로 마스크를 제거한다.
    mOverlay.remove(mFaceGraphic);
  }

  /**
   * 얼굴이 카메라에서 사라졌을 때 호출된다.
   * 이 경우는 onMissing 에서도 처리할 수 있지 않나? 구지 이걸 또 만든 이유가 뭘까?
   */
  @Override
  public void onDone() {
    mOverlay.remove(mFaceGraphic);
  }

  // Facial landmark utility methods
  // ===============================

  /** Given a face and a facial landmark position,
   *  return the coordinates of the landmark if known,
   *  or approximated coordinates (based on prior data) if not.
   */
  private PointF getLandmarkPosition(Face face, int landmarkId) {
    for (Landmark landmark : face.getLandmarks()) {
      if (landmark.getType() == landmarkId) {
        return landmark.getPosition();
      }
    }

    PointF landmarkPosition = mPreviousLandmarkPositions.get(landmarkId);
    if (landmarkPosition == null) {
      return null;
    }

    float x = face.getPosition().x + (landmarkPosition.x * face.getWidth());
    float y = face.getPosition().y + (landmarkPosition.y * face.getHeight());
    return new PointF(x, y);
  }

  private void updatePreviousLandmarkPositions(Face face) {
    for (Landmark landmark : face.getLandmarks()) {
      PointF position = landmark.getPosition();
      float xProp = (position.x - face.getPosition().x) / face.getWidth();
      float yProp = (position.y - face.getPosition().y) / face.getHeight();
      mPreviousLandmarkPositions.put(landmark.getType(), new PointF(xProp, yProp));
    }
  }

}