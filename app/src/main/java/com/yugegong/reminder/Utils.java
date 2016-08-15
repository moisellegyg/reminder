package com.yugegong.reminder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static int convertDpToPixels(int dp) {
        return (int) Math.ceil(dp * metrics.density);
    }

    public static int displayWidthPixels() {
        return metrics.widthPixels;
    }


    public static String getDateTimeString(long timestamp) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(timestamp));
    }

    public static Bitmap decodeBitmapFromFile(String path, int targetW, int targetH) {

        // Get the dimensions of the original bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int photoW = options.outWidth;
        int photoH = options.outHeight;

        Log.d("decodeBitmapFromFile", photoW + " " + photoH);

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(path, options);
    }

    public static void saveBitmapFile(String path, Bitmap bitmap) {
        File file = new File(path);
        if (file.exists()) {
            Log.d("saveBitMapFile", "original size: " + file.length());
            file.delete();
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d("saveBitMapFile", "new size: " + new File(path).length());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
