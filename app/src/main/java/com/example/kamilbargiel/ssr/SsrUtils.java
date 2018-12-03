package com.example.kamilbargiel.ssr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SsrUtils {

    private static HashMap<ImageView, CountDownTimer> timerToFieldMap = new HashMap<>();
    private static HashMap<ImageView, Long> tsToFieldMap = new HashMap<>();
    private static HashMap<ImageView, Mat> actualSigns = new HashMap<>();

    public static void findProperViewAndShowImage(Activity activity, short number, Bitmap bitmap, Mat signMat, MediaPlayer mp) {
//        if(!isAlreadyOnScreen(signMat, activity)) {
            if (activity.findViewById(R.id.sign0).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView) activity.findViewById(R.id.sign0), signMat);
            } else if (activity.findViewById(R.id.sign1).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView) activity.findViewById(R.id.sign1), signMat);
            } else if (activity.findViewById(R.id.sign2).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView) activity.findViewById(R.id.sign2), signMat);
            } else if (activity.findViewById(R.id.sign3).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView) activity.findViewById(R.id.sign3), signMat);
            } else {
                setImage(bitmap, getLongestShowedView(), signMat);
            }
            mp.start();
//        }
    }

    private static ImageView getLongestShowedView(){
        Map.Entry<ImageView, Long> min = null;
        for (Map.Entry<ImageView, Long> entry : tsToFieldMap.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }
        return min.getKey();
    }

    private static void setImage(Bitmap bitmap, ImageView view, Mat signMat){
        view.setImageBitmap(bitmap);
        view.setVisibility(View.VISIBLE);
        actualSigns.put(view, signMat);
        if(timerToFieldMap.get(view) != null) {
            timerToFieldMap.get(view).cancel();
        }
        tsToFieldMap.put(view, System.currentTimeMillis());
        timerToFieldMap.put(view, createCountDownTimer(view).start());
    }

    private static CountDownTimer createCountDownTimer(final View view){
        return new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                view.setVisibility(View.INVISIBLE);
                actualSigns.remove(view);
            }
        };
    }

    public static void saveOnDevice(Mat input, Context context) {
//        Imgproc.cvtColor(input, input, Imgproc.COLOR_BGR2RGB);
        Bitmap bmp = Bitmap.createBitmap(input.width(), input.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(input, bmp);
        String saved = MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp, "testTitle", "testDesc");
        if(saved == null){
            Log.e("FAAAAAIL", "FAAAAAIL");
        }
    }

    public static boolean isSignAlreadyOnScreen(Mat mat){
        double res = 0;
        boolean isAlreadyOnScreen = false;
        for(Mat onScreen : actualSigns.values()){
            Mat hist0 = new Mat();
            Mat hist1 = new Mat();
            Mat matHSV = new Mat();
            Mat onScreenHSV = new Mat();

            int hist_bins = 30;           //number of histogram bins
            int hist_range[]= {0,180};//histogram range
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt histSize = new MatOfInt(25);
            Imgproc.cvtColor(mat, matHSV, Imgproc.COLOR_BGR2HSV);
            Imgproc.cvtColor(onScreen, onScreenHSV, Imgproc.COLOR_BGR2HSV);

            Imgproc.calcHist(Collections.singletonList(matHSV), new MatOfInt(2), new Mat(), hist0, histSize, ranges);
            Imgproc.calcHist(Collections.singletonList(onScreenHSV), new MatOfInt(2), new Mat(), hist1, histSize, ranges);

            res = Imgproc.compareHist(hist0, hist1, Imgproc.CV_COMP_CORREL);
            if(res > 0.8){
                isAlreadyOnScreen = true;
            }
        }
        Log.i("Histogram compare", "Histogram compare: " + Double.toString(res));
        return isAlreadyOnScreen;
    }

    public static boolean isAlreadyOnScreen(Mat mat, Activity activity){
        boolean isAlreadyOnScreen = false;
        for(Mat onScreen : actualSigns.values()){
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
            DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            //first image
            Mat descriptors1 = new Mat();
            MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

            detector.detect(onScreen, keypoints1);
            descriptor.compute(onScreen, keypoints1, descriptors1);

            //second image
            Mat descriptors2 = new Mat();
            MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

            detector.detect(onScreen, keypoints2);
            descriptor.compute(onScreen, keypoints2, descriptors2);

            //matcher should include 2 different image's descriptors
            MatOfDMatch matches = new MatOfDMatch();
            matcher.match(descriptors1,descriptors2,matches);
            //feature and connection colors
            Scalar RED = new Scalar(255,0,0);
            Scalar GREEN = new Scalar(0,255,0);
            //output image
            Mat outputImg = new Mat();
            MatOfByte drawnMatches = new MatOfByte();
            //this will draw all matches, works fine
            Features2d.drawMatches(onScreen, keypoints1, onScreen, keypoints2, matches,
                    outputImg, GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
            Bitmap bmp = Bitmap.createBitmap(outputImg.width(), outputImg.height(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputImg, bmp);
            Log.i("Histogram compare", "Histogram compare: " + matches.size());
            setImage(bmp, (ImageView) activity.findViewById(R.id.sign0), outputImg);
        }
        return isAlreadyOnScreen;
    }

}
