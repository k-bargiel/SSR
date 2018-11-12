package com.example.kamilbargiel.ssr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SsrUtils {

    private static HashMap<ImageView, CountDownTimer> timerToFieldMap = new HashMap<>();
    private static HashMap<ImageView, Long> tsToFieldMap = new HashMap<>();

    public static void findProperViewAndShowImage(Activity activity, short number, Bitmap bitmap) {
            if(activity.findViewById(R.id.sign0).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView)activity.findViewById(R.id.sign0));
            } else if (activity.findViewById(R.id.sign1).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView)activity.findViewById(R.id.sign1));
            } else if (activity.findViewById(R.id.sign2).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView)activity.findViewById(R.id.sign2));
            } else if (activity.findViewById(R.id.sign3).getVisibility() == View.INVISIBLE) {
                setImage(bitmap, (ImageView)activity.findViewById(R.id.sign3));
            } else {
                setImage(bitmap, getLongestShowedView());
            }
    }

    private static ImageView getLongestShowedView(){
        Map.Entry<ImageView, Long> min = null;
        for (Map.Entry<ImageView, Long> entry : tsToFieldMap.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;
            }
        }
        return min.getKey();
    }

    private static void setImage(Bitmap bitmap, ImageView view){
        view.setImageBitmap(bitmap);
        view.setVisibility(View.VISIBLE);
        if(timerToFieldMap.get(view) != null) {
            timerToFieldMap.get(view).cancel();
        }
        tsToFieldMap.put(view, System.currentTimeMillis());
        timerToFieldMap.put(view, createCountDownTimer(view).start());
    }

    private static CountDownTimer createCountDownTimer(final View view){
        return new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                view.setVisibility(View.INVISIBLE);
            }
        };
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
