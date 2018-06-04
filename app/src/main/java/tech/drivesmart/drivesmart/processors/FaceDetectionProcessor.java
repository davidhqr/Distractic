package tech.drivesmart.drivesmart.processors;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import tech.drivesmart.drivesmart.CameraActivity;
import tech.drivesmart.drivesmart.models.FrameMetadata;
import tech.drivesmart.drivesmart.models.GraphicOverlay;

import java.io.IOException;
import java.util.List;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

  private static final String TAG = "FaceDetectionProcessor";

  private final FirebaseVisionFaceDetector detector;

  private CameraActivity instance;

  private int speed = 20;

  public FaceDetectionProcessor(CameraActivity instance) {
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
    }
  }

  private void processFace(FirebaseVisionFace face) {
    instance.updatingFace = face;
    FirebaseVisionFace calibratedFace = instance.calibratedFace;
    if (calibratedFace != null) {
      final double diff1 = Math.abs(calibratedFace.getHeadEulerAngleY() - face.getHeadEulerAngleY());
      final FirebaseVisionFaceLandmark noseYCalibrated = calibratedFace.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
      final FirebaseVisionFaceLandmark noseYFace  = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
      final boolean diff2 = (noseYCalibrated == null) || (noseYFace == null) ||
              (Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) > 110);
      if (diff1 > 15 && speed > 10) {
        instance.distractedX = true;
        instance.distractedY = false;
        if (noseYCalibrated != null && noseYFace != null) {
          // instance.debug.setText("LEFT/RIGHT: " + Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) + " | " + diff1);
        }
        instance.handler2.postDelayed(new Runnable(){
          @Override
          public void run(){
            if (instance.distractedX) {
              instance.status.setText("You are distracted! (LEFT/RIGHT)");
              instance.trueDistracted = true;
            }
          }
        }, 1600);
      } else if (diff2 && speed > 10) {
        instance.distractedY = true;
        instance.distractedX = false;
        if (noseYCalibrated != null && noseYFace != null) {
         // instance.debug.setText("UP/DOWN: " + Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) + " | " + diff1);
        }
        instance.handler3.postDelayed(new Runnable(){
          @Override
          public void run(){
            if (diff2 && instance.distractedY) {
              instance.status.setText("You are distracted! (UP/DOWN)");
              instance.trueDistracted = true;
            }
          }
        }, 700);
      } else {
        instance.status.setText("You are not distracted");
        // instance.debug.setText("NONE: " + Math.abs(noseYCalibrated.getPosition().getY() - noseYFace.getPosition().getY()) + " | " + diff1);
        instance.distractedX = false;
        instance.distractedY = false;
        instance.trueDistracted = false;
        instance.handler2.removeCallbacksAndMessages(null);
        instance.handler3.removeCallbacksAndMessages(null);
      }
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }
}
