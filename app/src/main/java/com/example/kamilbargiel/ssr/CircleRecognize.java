package com.example.kamilbargiel.ssr;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class CircleRecognize {

    private static final double dp = 1d;
    private static final int minRadius = 0;
    private static final int maxRadius = 0;
    private static final double param1 = 100;
    private static final double param2 = 20;

    public static ArrayList<Mat> cirleRecognize(Mat inputFrame) {
        Mat hsv = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_BGR2HSV);
        Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), lowerRed);
        Core.inRange(hsv, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upperRed);
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, hsv);
        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 2, 2);
        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, hsv.rows() / 8, param1, param2, 0, 20);
        ArrayList<Mat> images = new ArrayList<>();

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);
            images.add(getCircleFromImage(inputFrame, center, radius));
            Imgproc.circle(inputFrame, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
            Imgproc.circle(inputFrame, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
        }
//        images.add(inputFrame);

        lowerRed.release();
        upperRed.release();

        return images;
    }

    private static Mat getCircleFromImage(Mat inputFrame, Point center, int radius) {
        Rect rect = new Rect(new Point(center.x - (radius + 1), center.y - (radius + 1)), new Point(center.x + (radius + 1), center.y + (radius + 1)));
        return new Mat(inputFrame, rect);
    }

}