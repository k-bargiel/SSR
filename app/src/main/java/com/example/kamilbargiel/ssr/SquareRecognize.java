package com.example.kamilbargiel.ssr;

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

import static org.opencv.core.CvType.CV_8UC3;

public class SquareRecognize {

    public static List<Mat> squareRecognition(Mat inputFrame) {
        //cvInRangeS(imgHSV, cvScalar(10, 100, 100), cvScalar(30, 255, 255), imgThreshed)
        List<Mat> output = new ArrayList<>();
        Mat downscaled = new Mat();
        Mat upscaled = new Mat();
        Mat hsv = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat redColor = new Mat();
        Mat pictureContoured;
        MatOfPoint2f approx = new MatOfPoint2f();
        Mat onlyCountour = new Mat(inputFrame.rows(), inputFrame.cols(), CV_8UC3, new Scalar(0, 0, 0));

        Imgproc.pyrDown(inputFrame, downscaled, new Size(inputFrame.cols() / 2, inputFrame.rows() / 2));
        Imgproc.pyrUp(downscaled, upscaled, inputFrame.size());

        Imgproc.cvtColor(downscaled, hsv, Imgproc.COLOR_RGB2HSV); // device frame
        Core.inRange(hsv, new Scalar(0, 70, 50), new Scalar(10, 255, 255), lowerRed); //this
        Core.inRange(hsv, new Scalar(170, 70, 50), new Scalar(179, 255, 255), upperRed); //this
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, redColor); // this
        Imgproc.Canny(redColor, redColor, 0, 255);
        Imgproc.dilate(redColor, redColor, new Mat(), new Point(-1, 1), 1);
//        output.add(redColor);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        pictureContoured = redColor.clone();
        output.add(pictureContoured);
        Imgproc.findContours(
                pictureContoured,
                contours,
                new Mat(),
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

            Imgproc.drawContours(inputFrame, contours, 1, new Scalar(0, 255, 255), 3);

        for (MatOfPoint countour : contours) {
            MatOfPoint2f curve = new MatOfPoint2f(countour.toArray());
            Imgproc.approxPolyDP(
                    curve,
                    approx,
                    0.02 * Imgproc.arcLength(curve, true),
                    true
            );

            int numberVertices = (int) approx.total();
            double contourArea = Imgproc.contourArea(countour);

            Log.i("Vertices found:", Integer.toString(numberVertices));

            if (Math.abs(contourArea) < 100) {
                continue;
            }

//            if (numberVertices == 3) {
//                Imgproc.drawContours(onlyCountour, contours, 1, new Scalar(0,100,100));
//                output.add(onlyCountour);
//            }

//            // rectangle, pentagon and hexagon detection
//            if (numberVertices >= 4 && numberVertices <= 6) {
//
//                List<Double> cos = new ArrayList<>();
//                for (int j = 2; j < numberVertices + 1; j++) {
//                    cos.add(
//                            angle(
//                                    approxCurve.toArray()[j % numberVertices],
//                                    approxCurve.toArray()[j - 2],
//                                    approxCurve.toArray()[j - 1]
//                            )
//                    );
//                }
//                Collections.sort(cos);
//
//                double mincos = cos.get(0);
//                double maxcos = cos.get(cos.size() - 1);
//
//                // rectangle detection
//                if (numberVertices == 4
//                        && mincos >= -0.1 && maxcos <= 0.3
//                        ) {
//                    if (DISPLAY_IMAGES) {
//                        doSomethingWithContent("rectangle");
//                    } else {
//                        setLabel(dst, "RECT", cnt);
//                    }
//                }
//                // pentagon detection
//                else if (numberVertices == 5
//                        && mincos >= -0.34 && maxcos <= -0.27) {
//                    if (!DISPLAY_IMAGES) {
//                        setLabel(dst, "PENTA", cnt);
//                    }
//                }
//                // hexagon detection
//                else if (numberVertices == 6
//                        && mincos >= -0.55 && maxcos <= -0.45) {
//                    if (!DISPLAY_IMAGES) {
//                        setLabel(dst, "HEXA", cnt);
//                    }
//                }
//            }
//            // circle detection
//            else {
            Rect r = Imgproc.boundingRect(countour);
            int radius = r.width / 2;
            if (Math.abs(1 - (r.width / r.height)) <= 0.2 && Math.abs(1 - (contourArea / (Math.PI * radius * radius))) <= 0.2) {
                Rect rect = Imgproc.boundingRect(countour);
                Imgproc.rectangle(inputFrame, rect.tl(), rect.br(), new Scalar(0, 0, 255));
            }

//            }
//
//
//        }
        }
        output.add(inputFrame);
        return output;
    }

}
