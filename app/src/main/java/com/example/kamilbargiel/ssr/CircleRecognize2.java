package com.example.kamilbargiel.ssr;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
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

    public static ArrayList<Mat> cirleRecognize(Mat inputFrame, Context context) {
        Mat mHsvMat = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat hsv = new Mat();
        Mat blurred = new Mat();
        Mat circles = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        ArrayList<Mat> images = new ArrayList<>();
        Mat test = new Mat(inputFrame.rows(), inputFrame.cols(), CV_8UC3, new Scalar(0,0,0));

        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_RGB2HSV); // device frame
        saveOnDevice(hsv, context);
        Core.inRange(hsv, new Scalar(0, 70, 50), new Scalar(10, 255, 255), lowerRed); //this
        Core.inRange(hsv, new Scalar(170, 70, 50), new Scalar(179, 255, 255), upperRed); //this
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, hsv); // this
        Imgproc.findContours(hsv, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(test, contours, contourIdx, new Scalar(0, 255, 0), 2);
        }
        for (MatOfPoint contour : contours) {
            MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(temp, approxCurve,
                    Imgproc.arcLength(temp, true) * 0.02, true);
            Log.i("SIZE IS:", Integer.toString(approxCurve.toList().size()));
            if(approxCurve.toList().size() == 4){
                Log.i("RECTANGLE!!!!!!!!", "RECTANGLE!!!!!!!!");
            }
        }
        images.add(test);
//        private static Mat getCircleFromImage(Mat inputFrame, Point center, int radius) {
//            Mat mat = new Mat();
//            inputFrame.copyTo(mat);
//            Rect rect = new Rect(new Point(center.x - (radius + 2), center.y - (radius + 2)), new Point(center.x + (radius + 2), center.y + (radius + 2)));
//            try {
//                return new Mat(mat, rect);
//            } catch (Exception e){
//                Log.getStackTraceString(e);
//                return null;
//            }
        return images;
        }

    }


