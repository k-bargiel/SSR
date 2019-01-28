package com.example.kamilbargiel.ssr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.kamilbargiel.ssr.speed.CLocation;
import com.example.kamilbargiel.ssr.speed.IBaseGpsListener;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.List;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, IBaseGpsListener {

    private long framesCount = 0;
    private long ignoreFrames = 0;
    private short signViewCount = 0;
    private static final String TAG = "TAG";
    private CameraBridgeViewBase mOpenCvCameraView;
    private List<Mat> images;
    private Mat frame;
    private MediaPlayer mp;

    private int minRad = 3;
    private int maxRad = 200;
    private double param1 = 190;
    private double param2 = 65;
    private double dp = 0.4;
    private double epsilon = 0.02;
    private int areaMax = 50;
    private EditText epsilonEdit;
    private EditText maxAreaEdit;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.i("Success", "Success");
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mp = MediaPlayer.create(this, R.raw.alert);

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setMaxFrameSize(1300, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //speed meter
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        this.updateSpeed(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i("Success", "Success");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        frame = new Mat();
    }

    public void onCameraViewStopped() {
        frame.release();
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame openCvFrame) {
        this.frame = openCvFrame.rgba();
        framesCount++;
        if (framesCount % 99999 == 0) {
            framesCount = 0;
        }

        List<Mat> signsRecognized;
        if (ignoreFrames != -1 && (ignoreFrames == 0 || framesCount % ignoreFrames == 0)) {
            try {
                signsRecognized = CircleRecognize.cirleRecognize(frame, this, minRad, maxRad, param1, param2, dp);
                signsRecognized.addAll(TraingleDetection.detectTriangles(frame, this, epsilon, areaMax));
                showSignsOnScreen(signsRecognized);
            } catch (Exception e) {
                Log.e("Main activity", "EXCEPTION!", e);
            }
        }
        return frame;
    }

    private void showSignsOnScreen(final List<Mat> circles) {
        if (!circles.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Mat circle : circles) {
                        Bitmap bmp = Bitmap.createBitmap(circle.width(), circle.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(circle, bmp);
                        SsrUtils.findProperViewAndShowImage(MainActivity.this, signViewCount, bmp, circle, mp);
                        signViewCount++;
                        if (signViewCount % 4 == 0) {
                            signViewCount = 0;
                        }
                    }
                }
            });
        }
    }

    // **** SPEED METER **** //

    private void updateSpeed(CLocation location) {
        double nCurrentSpeed = 0;

        if (location != null) {
            nCurrentSpeed = location.getSpeed();
        }

        String strUnits = "km/h";
//        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.speed);
        setIgnoreFramesCount(nCurrentSpeed);
//        txtCurrentSpeed.setText(nCurrentSpeed + " " + strUnits);
    }

    private void setIgnoreFramesCount(double currentSpeed) {
        if (currentSpeed >= 80.0) {
            ignoreFrames = 0;
        } else if (currentSpeed >= 70.0 && currentSpeed < 80.0) {
            ignoreFrames = 3;
        } else if (currentSpeed >= 60.0 && currentSpeed < 70.0) {
            ignoreFrames = 3;
        } else if (currentSpeed >= 50.0 && currentSpeed < 60.0) {
            ignoreFrames = 3;
        } else if (currentSpeed >= 40.0 && currentSpeed < 50.0) {
            ignoreFrames = 3;
        } else if (currentSpeed >= 30.0 && currentSpeed < 40.0) {
            ignoreFrames = 4;
        } else if (currentSpeed >= 20.0 && currentSpeed < 30.0) {
            ignoreFrames = 5;
        } else if (currentSpeed >= 10.0 && currentSpeed < 20.0) {
            ignoreFrames = 6;
        } else if (currentSpeed >= 7.0 && currentSpeed < 10.0) {
            ignoreFrames = 7;
        } else if (currentSpeed >= 3.0 && currentSpeed < 7.0) {
            ignoreFrames = 8;
        } else if (currentSpeed >= 0.0 && currentSpeed < 3.0) {
            ignoreFrames = -1;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            CLocation myLocation = new CLocation(location);
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onGpsStatusChanged(int event) {
    }

}
