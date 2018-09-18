package com.example.kamilbargiel.ssr;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.example.kamilbargiel.ssr.SsrUtils.saveOnDevice;
import static org.opencv.core.CvType.CV_8UC3;

public class CircleRecognize2 {

    private static final double dp = 0.8d;
    private static final int minRadius = 20;
    private static final int maxRadius = 200;
    private static final double param1 = 100;
    private static final double param2 = 30;

    public static ArrayList<Mat> cirleRecognize(Mat inputFrame, Context context) {
        Mat mHsvMat = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat hsv = new Mat();
        Mat hierarchy = new Mat();
        Mat circles = new Mat();
        Mat mDilatedMat = new Mat();
        Mat test = new Mat(inputFrame.rows(), inputFrame.cols(), CV_8UC3, new Scalar(0,0,0));

//        Imgproc.cvtColor(inputFrame, mHsvMat, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(inputFrame, mHsvMat, Imgproc.COLOR_RGB2HSV);
//        Core.inRange(mHsvMat, new Scalar(0, 70, 50), new Scalar(10, 255, 255), lowerRed);
//        Core.inRange(mHsvMat, new Scalar(170, 70, 50), new Scalar(179, 255, 255), upperRed);
        Core.inRange(mHsvMat, new Scalar(0, 100, 100), new Scalar(10, 255, 255), lowerRed);
        Core.inRange(mHsvMat, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upperRed);
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, hsv);
        Imgproc.dilate(hsv, mDilatedMat, new Mat());
        List<MatOfPoint> contours = new ArrayList<>();

        Imgproc.findContours(mDilatedMat, contours, hierarchy, Imgproc.RETR_LIST, 1);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(test, contours, contourIdx, new Scalar(0, 255, 0), 2);
        }

        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, 75, param1, param2, minRadius, maxRadius);
        ArrayList<Mat> images = new ArrayList<>();
//        images.add(hsv);

        for (int x = 0; x < circles.cols(); x++) {
            Log.i("CircleRecognize", "Found circle!");
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);
            Mat sign = getCircleFromImage(inputFrame, center, radius);
            if(sign != null){ // && checkWhiteCircleInsideRedCircle(sign, context)
                saveOnDevice(sign, context);
                images.add(sign);
            }
            Imgproc.circle(inputFrame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            Imgproc.circle(inputFrame, center, radius, new Scalar(0, 255, 0), 3, 8, 0);
        }
        saveOnDevice(inputFrame, context);

        images.add(test);
        return images;
    }

    private static Mat getCircleFromImage(Mat inputFrame, Point center, int radius) {
        Mat mat = new Mat();
        inputFrame.copyTo(mat);
        Rect rect = new Rect(new Point(center.x - (radius + 2), center.y - (radius + 2)), new Point(center.x + (radius + 2), center.y + (radius + 2)));
        try {
            return new Mat(mat, rect);
        } catch (Exception e){
            Log.getStackTraceString(e);
            return null;
        }
    }

}