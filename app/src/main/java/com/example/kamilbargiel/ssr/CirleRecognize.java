package com.example.kamilbargiel.ssr;

public class CircleRecognize {

    private static final double dp = 1d;
    private static final int minRadius = 0;
    private static final int maxRadius = 0;
    private static final double param1 = 100;
    private static final double param2 = 20;

    public static void cirleRecognize(Mat inputFrame){
        Mat hsv = new Mat();
        Mat lowerRed = new Mat();
        Mat upperRed = new Mat();
        Mat circles = new Mat();

        Imgproc.cvtColor(inputFrame, hsv, Imgproc.COLOR_RGB2HSV);
        Core.inRange(hsv, new Scalar(0, 100, 100), new Scalar(10, 255, 255), lowerRed);
        Core.inRange(hsv, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upperRed);
        Core.addWeighted(lowerRed, 1.0, upperRed, 1.0, 0.0, hsv);
        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 2, 2);
        Imgproc.HoughCircles(hsv, circles, Imgproc.CV_HOUGH_GRADIENT, dp, inputFrame.rows() / 8, param1, param2, 100, maxRadius);

        Mat mask = new Mat(inputFrame.rows(), inputFrame.cols(), CvType.CV_8U, Scalar.all(0));

        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
//            Imgproc.circle(inputFrame, center, 1, new Scalar(0, 100, 100), 3, 8, 0); center of circle
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(mask, center, radius, new Scalar(255, 0, 255), 3, 8, 0);
        }

        Mat croppedCirle = getCircleFromImage(Mat mask);

        lowerRed.release();
        upperRed.release();
    }

    private static Mat getCircleFromImage(Mat mask){
        Mat masked = new Mat();
        src.copyTo( masked, mask );
        Mat thresh = new Mat();
        Imgproc.threshold( mask, thresh, 1, 255, Imgproc.THRESH_BINARY );
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Rect rect = Imgproc.boundingRect(contours.get(0));
        Mat cropped = masked.submat(rect);
        return cropped;
    }

}