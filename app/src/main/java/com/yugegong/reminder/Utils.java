package com.yugegong.reminder;

import android.app.Activity;
import android.util.DisplayMetrics;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by ygong on 8/11/16.
 */
public class Utils {

    private static DisplayMetrics metrics = new DisplayMetrics();
    public static void setMetrics(Activity activity) {
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    public static int dpToPixel(int dp) {
        return (int) Math.ceil(dp * metrics.density);
    }

    public static int displayWidthPixels() {
        return metrics.widthPixels;
    }


    public static String getDateTimeString(long timestamp) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(timestamp));
    }

}
