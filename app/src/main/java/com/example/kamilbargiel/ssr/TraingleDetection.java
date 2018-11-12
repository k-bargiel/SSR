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
import java.util.LinkedList;
import java.util.List;

public class TraingleDetection {

    public static List<Mat> detectTriangles(Mat inputFrame, Context context) {
        Mat hsv = new Mat();
        Mat yellow = new Mat();
        Mat triangles = new Mat();
        Mat blurred = new Mat();
        ArrayList<Mat> output = new ArrayList<>();
        LinkedList<MatOfPoint> contours = new LinkedList<>();
        LinkedList<MatOfPoint> triangleContours = new LinkedList<>();

//        Imgproc.medianBlur(inputFrame, blurred, 3);
        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_RGB2HSV); // device frame

        Core.inRange(hsv, new Scalar(15, 0, 66), new Scalar(45, 255, 255), yellow);
        output.add(yellow);
        Imgproc.GaussianBlur(yellow, yellow, new Size(5, 5), 0);
        Imgproc.findContours(yellow, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint cnt : contours) {
            double contourArea = Imgproc.contourArea(cnt);
            double area = Math.abs(contourArea);
            if (area < 600) {
                continue;
            }

            double arc = Imgproc.arcLength(new MatOfPoint2f(cnt.toArray()), true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(cnt.toArray()), approx, 0.2 * arc, true);
            MatOfPoint result = new MatOfPoint(approx.toArray());
            if (result.total() == 3) {
                Log.i("COUNTOUTES AREA: ", Double.toString(contourArea));
                Rect rect = Imgproc.boundingRect(result);
                Mat sign = getSignFromImage(inputFrame, rect);
                output.add(sign);
                SsrUtils.saveOnDevice(sign, context);
                // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                Imgproc.rectangle(inputFrame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(10, 255, 255), 3);
                triangleContours.add(cnt);
            }
        }

        Imgproc.drawContours(inputFrame, triangleContours, -1, new Scalar(89, 255, 255), 3);
//        Imgproc.drawContours(inputFrame, contours, -1, new Scalar(89, 255, 255), 5);

        SsrUtils.saveOnDevice(inputFrame, context);
        SsrUtils.saveOnDevice(yellow, context);

        hsv.release();
//        yellow.release();
        triangles.release();
        blurred.release();

        return output;
    }

    private static Mat getSignFromImage(Mat inputFrame, Rect rect) {
        Mat mat = new Mat();
        inputFrame.copyTo(mat);
        try {
            return new Mat(mat, rect);
        } catch (Exception e) {
            Log.getStackTraceString(e);
            return null;
        }
    }

}
