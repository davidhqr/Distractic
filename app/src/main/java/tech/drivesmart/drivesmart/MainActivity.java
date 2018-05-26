package tech.drivesmart.drivesmart;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.annotation.KeepName;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import tech.drivesmart.drivesmart.facedetection.FaceDetectionProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source. */
@KeepName
public final class MainActivity extends AppCompatActivity
    implements OnRequestPermissionsResultCallback,
        OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
  private static final String FACE_DETECTION = "Face Detection";
  private static final String TAG = "MainActivity";
  private static final int PERMISSION_REQUESTS = 1;

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = FACE_DETECTION;

  public FirebaseVisionFace updatingFace = null;
  public FirebaseVisionFace calibratedFace = null;
  public TextView status = null;
  public Handler handler;
  public Handler handler2;
  public boolean distractedX = false;
  public boolean distractedY = false;
  public boolean trueDistracted = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    hideSystemUI();
    setContentView(R.layout.activity_main);

    preview = (CameraSourcePreview) findViewById(R.id.firePreview);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = (GraphicOverlay) findViewById(R.id.fireFaceOverlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    ToggleButton facingSwitch = (ToggleButton) findViewById(R.id.facingswitch);
    facingSwitch.setOnCheckedChangeListener(this);

    Button button = (Button) findViewById(R.id.calibratebutton);
    button.setOnClickListener(this);

    status = (TextView) findViewById(R.id.status);

    handler = new Handler();
    handler2 = new Handler();

    createCameraSource(selectedModel);
    beepDistracted();
  }

  private void hideSystemUI() {
    // Enables regular immersive mode.
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
  }

  private void beepDistracted() {
    final MediaPlayer mp = MediaPlayer.create(this, R.raw.distracted);
    final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    handler2.postDelayed(new Runnable(){
      public void run(){
        if (trueDistracted) {
          v.vibrate(300);
          mp.start();
        }
        handler2.postDelayed(this, 500);
      }
    }, 500);
  }

  @Override
  public void onClick(View view) {
      if (updatingFace != null) {
          calibratedFace = updatingFace;
          trueDistracted = false;
          distractedX = false;
          distractedY = false;
          Toast.makeText(this, "Resting face position calibrated",
                  Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(this, "Error: No face found",
                Toast.LENGTH_SHORT).show();
      }
  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    selectedModel = parent.getItemAtPosition(pos).toString();
    preview.stop();
    if (allPermissionsGranted()) {
      createCameraSource(selectedModel);
      startCameraSource();
    } else {
      getRuntimePermissions();
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }
    try {
      switch (model) {
        case FACE_DETECTION:
          cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this));
          break;
        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (Exception e) {
      Log.e(TAG, "Cannot create camera source: " + model);
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        Log.d(TAG, "Camera source started");
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
    Log.e(TAG, "Camera source is null");
  }

  @Override
  public void onResume() {
    super.onResume();
    startCameraSource();
    beepDistracted();
    hideSystemUI();
  }

  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
    handler2.removeCallbacksAndMessages(null);
    handler.removeCallbacksAndMessages(null);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info =
          this.getPackageManager()
              .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
      String[] ps = info.requestedPermissions;
      if (ps != null && ps.length > 0) {
        return ps;
      } else {
        return new String[0];
      }
    } catch (Exception e) {
      return new String[0];
    }
  }

  private boolean allPermissionsGranted() {
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        return false;
      }
    }
    return true;
  }

  private void getRuntimePermissions() {
    List<String> allNeededPermissions = new ArrayList<>();
    for (String permission : getRequiredPermissions()) {
      if (!isPermissionGranted(this, permission)) {
        allNeededPermissions.add(permission);
      }
    }

    if (!allNeededPermissions.isEmpty()) {
      ActivityCompat.requestPermissions(
          this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }
  }

  @Override
  public void onRequestPermissionsResult(
          int requestCode, String[] permissions, int[] grantResults) {
    Log.i(TAG, "Permission granted!");
    if (allPermissionsGranted()) {
      createCameraSource(selectedModel);
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  private static boolean isPermissionGranted(Context context, String permission) {
    if (ContextCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED) {
      Log.i(TAG, "Permission granted: " + permission);
      return true;
    }
    Log.i(TAG, "Permission NOT granted: " + permission);
    return false;
  }
}
