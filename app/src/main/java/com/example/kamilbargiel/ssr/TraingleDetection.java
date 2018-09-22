package com.example.kamilbargiel.ssr;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

public class TraingleDetection {

    public static List<Mat> detectTriangles(Mat inputFrame){
        Mat hsv = new Mat();
        Mat yellow = new Mat();
        Mat triangles = new Mat();
        Mat blurred = new Mat();
        LinkedList<Mat> output = new LinkedList<>();
        LinkedList<MatOfPoint> contours = new LinkedList<>();
        LinkedList<MatOfPoint> triangleContours = new LinkedList<>();

        Imgproc.medianBlur(inputFrame, blurred, 3);
        Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_RGB2HSV); // device frame
        Core.inRange(hsv, new Scalar(10, 100, 100), new Scalar(30, 255, 255), yellow);
        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 5, 5);
        output.add(yellow);
        Imgproc.findContours(yellow, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for(MatOfPoint cnt : contours){
            double arc = Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()), approx, 0.04*arc, true);
            MatOfPoint result = new MatOfPoint(approx.toArray());
            if(result.total() == 3){
                triangleContours.add(cnt);
            }
        }

//        Imgproc.drawContours(inputFrame, triangleContours, -1, new Scalar(89, 255, 255));
        Imgproc.drawContours(inputFrame, contours, -1, new Scalar(89, 255, 255));

        hsv.release();
        yellow.release();
        triangles.release();
        blurred.release();

        return output;
    }

}
