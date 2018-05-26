package tech.drivesmart.drivesmart.facedetection;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import tech.drivesmart.drivesmart.FrameMetadata;
import tech.drivesmart.drivesmart.GraphicOverlay;
import tech.drivesmart.drivesmart.MainActivity;
import tech.drivesmart.drivesmart.VisionProcessorBase;

import java.io.IOException;
import java.util.List;

import tech.drivesmart.drivesmart.MainActivity;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

  private static final String TAG = "FaceDetectionProcessor";

  private final FirebaseVisionFaceDetector detector;

  private MainActivity instance;

  private int speed = 20;

  public FaceDetectionProcessor(MainActivity instance) {
    FirebaseVisionFaceDetectorOptions options =
        new FirebaseVisionFaceDetectorOptions.Builder()
            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setTrackingEnabled(true)
            .build();

    detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    this.instance = instance;
  }

  @Override
  public void stop() {
    try {
      detector.close();
    } catch (IOException e) {
      Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
    }
  }

  @Override
  protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
    return detector.detectInImage(image);
  }

  @Override
  protected void onSuccess(
      @NonNull List<FirebaseVisionFace> faces,
      @NonNull FrameMetadata frameMetadata,
      @NonNull GraphicOverlay graphicOverlay) {
    graphicOverlay.clear();
    if (faces.size() > 0) {
      FirebaseVisionFace face = faces.get(0);
      processFace(face);
      /*FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay);
      graphicOverlay.add(faceGraphic);
      faceGraphic.updateFace(face, frameMetadata.getCameraFacing());*/
    }
  }

  private void processFace(FirebaseVisionFace face) {
    instance.updatingFace = face;
    FirebaseVisionFace calibratedFace = instance.calibratedFace;
    if (calibratedFace != null) {
      final double diff1 = Math.abs(calibratedFace.getHeadEulerAngleY() - face.getHeadEulerAngleY());
      FirebaseVisionFaceLandmark noseYCalibrated = calibratedFace.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
      FirebaseVisionFaceLandmark noseYFace  = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
      final boolean diff2 = (((noseYCalibrated == null) || (noseYFace == null) || (Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) > 120)) && (diff1 < 4)) ||
              (((noseYCalibrated == null) || (noseYFace == null) || (Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) > 280)) && (diff1 < 7));
      if ((diff1 > 15 || diff2) && speed > 10) {
        int delay = 2000;
        String msg = " (left/right)";
        if (diff2) {
          delay = 1000;
          msg = " (up/down)";
          instance.distractedY = true;
        } else {
          instance.distractedX = true;
        }
        final String msg1 = msg;
        instance.handler.postDelayed(new Runnable(){
          @Override
          public void run(){
            if (diff1 > 12 && instance.distractedX) {
              instance.status.setText("You are distracted!" + msg1);
              instance.trueDistracted = true;
            } else if (diff2 && instance.distractedY) {
              instance.status.setText("You are distracted!" + msg1);
              instance.trueDistracted = true;
            }
          }
        }, delay);
      } else {
        instance.status.setText("You are not distracted!");
        instance.distractedX = false;
        instance.distractedY = false;
        instance.trueDistracted = false;
        instance.handler.removeCallbacksAndMessages(null);
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}
