package com.example.kamilbargiel.ssr;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.example.kamilbargiel.ssr.SsrUtils.saveOnDevice;

public class CircleRecognize {

    public static ArrayList<Mat> cirleRecognize(Mat inputFrame, Context context, int minRad, int maxRad, double param1, double param2, double dp) {
        Mat hsv = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat circles = new Mat();
        Mat blurred = new Mat();

        Imgproc.medianBlur(inputFrame, blurred, 3);
        Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_RGB2HSV); // device frame
//        Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_BGR2HSV); // image frame
        Core.inRange(hsv, new Scalar(0, 70, 50), new Scalar(10, 255, 255), lowerRed); //this
        Core.inRange(hsv, new Scalar(170, 70, 50), new Scalar(179, 255, 255), upperRed); //this
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, hsv); // this
        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 5, 5);
        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, 70, param1, param2, minRad, maxRad);
        ArrayList<Mat> images = new ArrayList<>();

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);
            Mat sign = getCircleFromImage(inputFrame, center, radius);
            if (sign != null) {
                images.add(sign);
            }
        }

        lowerRed.release();
        upperRed.release();
        hsv.release();
        circles.release();
        blurred.release();

        return images;
    }

    private static Mat getCircleFromImage(Mat inputFrame, Point center, int radius) {
        Mat mat = new Mat();
        inputFrame.copyTo(mat);
        Rect rect = new Rect(new Point(center.x - (radius + 2), center.y - (radius + 2)), new Point(center.x + (radius + 2), center.y + (radius + 2)));
        try {
            return new Mat(mat, rect);
        } catch (Exception e) {
            Log.getStackTraceString(e);
            return null;
        }
    }

//    private static boolean checkWhiteCircleInsideRedCircle(Mat inputFrame, Context context) {
//        Mat hsv = new Mat();
//        Mat circles = new Mat();
//        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_BGR2HSV); // image frame
//        Core.inRange(hsv, new Scalar(80, 4, 73), new Scalar(80, 4, 73), hsv);
//        //test.add(hsv);
//        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 2, 2);
//        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, hsv.rows() / 8, param1, param2, 0, 0);
//        saveOnDevice(hsv, context);
//        //Core.countNonZero(hsv);
//        for (int x = 0; x < circles.cols(); x++) {
//            Log.i("CircleRecognize", "Found circle!");
//            double[] c = circles.get(0, x);
//            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
//            int radius = (int) Math.round(c[2]);
//            Imgproc.circle(inputFrame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
//            Imgproc.circle(inputFrame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
//        }
//
//        return circles.cols() != 0;
//    }
//
//    public static List<Mat> whiteCircle(Mat inputFrame) {
//        Mat hsv = new Mat();
//        Mat circles = new Mat();
//        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_BGR2HSV);
//        Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(0, 0, 255), hsv);
//        ArrayList<Mat> images = new ArrayList<>();
//        images.add(hsv);
////        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 2, 2);
////        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, hsv.rows() / 8, param1, param2, 0, 0);
//
////        for (int x = 0; x < circles.cols(); x++) {
////            Log.i("CircleRecognize", "Found circle!");
////            double[] c = circles.get(0, x);
////            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
////            int radius = (int) Math.round(c[2]);
////            Imgproc.circle(inputFrame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
////            Imgproc.circle(inputFrame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
////        }
//
//        return images;
//    }

}