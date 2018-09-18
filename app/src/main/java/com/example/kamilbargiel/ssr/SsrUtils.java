package com.example.kamilbargiel.ssr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class SsrUtils {

    public static View findProperView(Activity activity, short number) {
        switch (number) {
            case 0:
                return activity.findViewById(R.id.sign0);
            case 1:
                return activity.findViewById(R.id.sign1);
            case 2:
                return activity.findViewById(R.id.sign2);
            case 3:
                return activity.findViewById(R.id.sign3);
            default:
                return activity.findViewById(R.id.sign3);
        }
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

}
