package com.example.kamilbargiel.ssr;

import android.app.Activity;
import android.view.View;

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

}
