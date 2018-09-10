package com.example.kamilbargiel.ssr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_UNCHANGED;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private long framesCount = 0;
    private short signViewCount = 0;
    private static final String TAG = "TAG";
    private CameraBridgeViewBase mOpenCvCameraView;
    private List<Mat> images;

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

        if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, 3);
        }

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.cameraView);
        mOpenCvCameraView.enableView();
        mOpenCvCameraView.setMaxFrameSize(640, 480);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        images = getAllImages(this);
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
    }

    public void onCameraViewStopped() {
//        inputFrame.release();
    }

    public Mat onCameraFrame(Mat frame) {
        framesCount++;
        if (framesCount % 99999 == 0) {
            framesCount = 0;
        }

        List<Mat> signsRecognized;

        if (framesCount % 35 == 0) {
            Log.i("MainActivity", "Next 25 frame");
//            if(images.size() > 0) {
            try {
                signsRecognized = CircleRecognize.cirleRecognize(frame);  // images.remove(images.size() - 1)
                showSignsOnScreen(signsRecognized);
            } catch (Exception e){
                Log.e("Main activity", "EXCEPTION!");
                Log.getStackTraceString(e);
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
                    for (Mat circle : circles) {
                        ImageView imageView = (ImageView) SsrUtils.findProperView(MainActivity.this, signViewCount);
                        Bitmap bmp = Bitmap.createBitmap(circle.width(), circle.height(), Bitmap.Config.ARGB_8888);
//                        Imgproc.cvtColor(circle, circle, Imgproc.COLOR_BGR2RGB);
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

    private List<Mat> getAllImages(final Context context){
        return new ArrayList<Mat>(){{
            try {
                add(Utils.loadResource(context, R.drawable.aaaa, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.burst, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.busrtaw, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.tescik, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.test, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.tree, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.aaaaaaa, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.nowy15, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.nowy12, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.piec, CV_LOAD_IMAGE_UNCHANGED));
                add(Utils.loadResource(context, R.drawable.halo, CV_LOAD_IMAGE_UNCHANGED));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }};
    }

}
