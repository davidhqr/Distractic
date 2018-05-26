package tech.drivesmart.drivesmart;

import android.graphics.Bitmap;
import android.media.Image;

import com.google.firebase.ml.common.FirebaseMLException;

import java.nio.ByteBuffer;

public interface VisionImageProcessor {

  /** Processes the images with the underlying machine learning models. */
  void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay)
      throws FirebaseMLException;

  /** Processes the bitmap images. */
  void process(Bitmap bitmap, GraphicOverlay graphicOverlay);

  /** Processes the images. */
  void process(Image bitmap, int rotation, GraphicOverlay graphicOverlay);

  /** Stops the underlying machine learning model and release resources. */
  void stop();
}
