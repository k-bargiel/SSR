package com.example.kamilbargiel.ssr;

import android.content.Context;

import org.opencv.core.Mat;

public class CirclesProcessingParams {

    public Mat inputFrame;
    public int minRad;
    public int maxRad;
    public double param1;
    public double param2;
    public double dp;
    public Context context;

    public CirclesProcessingParams(Mat inputFrame, int minRad, int maxRad, double param1, double param2, double dp, Context context) {
        this.inputFrame = inputFrame;
        this.minRad = minRad;
        this.maxRad = maxRad;
        this.param1 = param1;
        this.param2 = param2;
        this.dp = dp;
        this.context = context;
    }
}
