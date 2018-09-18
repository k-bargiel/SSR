package com.example.kamilbargiel.ssr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_UNCHANGED;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private long framesCount = 0;
    private short signViewCount = 0;
    private static final String TAG = "TAG";
    private CameraBridgeViewBase mOpenCvCameraView;
    private List<Mat> images;
    private Mat frame;
    private MediaPlayer mp;

    private EditText minRadET;
    private EditText maxRadET;
    private EditText param1ET;
    private EditText param2ET;
    private EditText dpET;
    private int minRad = 5;
    private int maxRad = 150;
    private double param1 = 190;
    private double param2 = 65;
    private double dp = 0.4;

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
//        initializeSeekBarVariables();
//        createListenersForSeekBars();
        mp = MediaPlayer.create(this, R.raw.alert);
        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
        }
        if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setMaxFrameSize(1300, 800);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
//        images = getAllImages(this);
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
        Log.i("min", Integer.toString(minRad));
        Log.i("max", Integer.toString(maxRad));
        Log.i("param1", Double.toString(param1));
        Log.i("param2", Double.toString(param2));
        Log.i("dp", Double.toString(dp));
        framesCount++;
        if (framesCount % 99999 == 0) {
            framesCount = 0;
        }

        List<Mat> signsRecognized;

        if (framesCount % 5 == 0) {
            Log.i("MainActivity", "Next 25 frame");
//            SsrUtils.saveOnDevice(frame, this);
//            if (images.size() > 0) {
            try {
                signsRecognized = CircleRecognize.cirleRecognize(frame, this, minRad, maxRad, param1, param2, dp);
//                signsRecognized = CircleRecognize.cirleRecognize(images.remove(images.size() - 1), this, minRad, maxRad, param1, param2, dp);
//                    signsRecognized = CircleRecognize2.cirleRecognize(frame, this);
//                signsRecognized = CircleRecognize2.cirleRecognize(images.remove(images.size() - 1), this);
                showSignsOnScreen(signsRecognized);
            } catch (Exception e) {
                Log.e("Main activity", "EXCEPTION!", e);
            }
//            } else {
//                images = getAllImages(this);
//            }
        }
        return frame;
    }

    private void showSignsOnScreen(final List<Mat> circles) {
        if (!circles.isEmpty()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mp.start();
                    for (Mat circle : circles) {
                        ImageView imageView = (ImageView) SsrUtils.findProperView(MainActivity.this, signViewCount);
//                        Imgproc.cvtColor(circle, circle, Imgproc.COLOR_BGR2RGB);
                        Bitmap bmp = Bitmap.createBitmap(circle.width(), circle.height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(circle, bmp);
                        imageView.setImageBitmap(bmp);
                        imageView.setVisibility(View.VISIBLE);
                        signViewCount++;
                        if (signViewCount % 4 == 0) {
                            signViewCount = 0;
                        }
                    }
                }
            });
        }
    }

    private List<Mat> getAllImages(final Context context) {
        return new ArrayList<Mat>() {{
            try {
//                add(Utils.loadResource(context, R.drawable.fhd1, CV_LOAD_IMAGE_UNCHANGED));
//                add(Utils.loadResource(context, R.drawable.saturation, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.adjusted, CV_LOAD_IMAGE_UNCHANGED));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }};
    }

//    private void initializeSeekBarVariables() {
//        minRadET = (EditText) findViewById(R.id.minRad);
//        maxRadET = (EditText) findViewById(R.id.maxRad);
//        param1ET = (EditText) findViewById(R.id.param1);
//        param2ET = (EditText) findViewById(R.id.param2);
//        dpET = (EditText) findViewById(R.id.dp);
//        minRadET.setText("5");
//        maxRadET.setText("0");
//        param1ET.setText("100");
//        param2ET.setText("38");
//        dpET.setText("1.0");
//        TextView myOutputBox = (TextView) findViewById(R.id.textViewMinRad);
//        myOutputBox.setText("minRad: ");
//        TextView myOutputBox2 = (TextView) findViewById(R.id.textViewMaxRad);
//        myOutputBox2.setText("maxRad: ");
//        TextView myOutputBox3 = (TextView) findViewById(R.id.textViewParam1);
//        myOutputBox3.setText("param1: ");
//        TextView myOutputBox4 = (TextView) findViewById(R.id.textViewParam2);
//        myOutputBox4.setText("param2: ");
//        TextView myOutputBox5 = (TextView) findViewById(R.id.textViewDp);
//        myOutputBox5.setText("dp: ");

//    }
//
//    private void createListenersForSeekBars() {
//        final Context context = this;
//        //min rad edit text
//        minRadET.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                TextView myOutputBox = (TextView) findViewById(R.id.textViewMinRad);
//                myOutputBox.setText("minRad: " + s);
//                try {
//                    minRad = Integer.parseInt(s.toString());
//                } catch (Exception e) {
//                    Toast toast = Toast.makeText(context, "provide proper variable", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
//        //max rad seekbar
//        maxRadET.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                TextView myOutputBox = (TextView) findViewById(R.id.textViewMaxRad);
//                myOutputBox.setText("maxRad: " + s);
//                try {
//                    maxRad = Integer.parseInt(s.toString());
//                } catch (Exception e) {
//                    Toast toast = Toast.makeText(context, "provide proper variable", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
//        //param1 seekbar
//        param1ET.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                TextView myOutputBox = (TextView) findViewById(R.id.textViewParam1);
//                myOutputBox.setText("param1: " + s);
//                try {
//                    param1 = Double.parseDouble(s.toString());
//                } catch (Exception e) {
//                    Toast toast = Toast.makeText(context, "provide proper variable", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
//        //param2 seekbar
//        param2ET.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                TextView myOutputBox = (TextView) findViewById(R.id.textViewParam2);
//                myOutputBox.setText("param2: " + s);
//                try {
//                    param2 = Double.parseDouble(s.toString());
//                } catch (Exception e) {
//                    Toast toast = Toast.makeText(context, "provide proper variable", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
//        //dp seekbar
//        dpET.addTextChangedListener(new TextWatcher() {
//            public void afterTextChanged(Editable s) {
//            }
//            public void beforeTextChanged(CharSequence s, int start,
//                                          int count, int after) {
//            }
//            public void onTextChanged(CharSequence s, int start,
//                                      int before, int count) {
//                TextView myOutputBox = (TextView) findViewById(R.id.textViewDp);
//                myOutputBox.setText("dp: " + s);
//                try {
//                    dp = Double.parseDouble(s.toString());
//                } catch (Exception e) {
//                    Toast toast = Toast.makeText(context, "provide proper variable", Toast.LENGTH_SHORT);
//                    toast.show();
//                }
//            }
//        });
//    }

}
