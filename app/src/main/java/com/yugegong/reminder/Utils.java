package com.yugegong.reminder;

import android.content.res.Resources;
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
    // The gesture threshold expressed in dp
    private static DisplayMetrics metrics = new DisplayMetrics();
    public static void setMetrics(Resources resources) {
        // Get the screen's density scale
        metrics = resources.getDisplayMetrics();
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

    public static Bitmap decodeBitmapFromFile(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = 8;
        return BitmapFactory.decodeFile(path, options);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        Bitmap src = BitmapFactory.decodeFile(path, options);
//        final int width = options.outWidth;
//        final int height = options.outHeight;
//        Log.d("decodeBitmapFromFile", width + " " + height + " " + reqWidth + " " + reqHeight);
//
//        Bitmap scaled = Bitmap.createScaledBitmap(src, reqWidth, reqWidth * height/width, true);
//        return scaled;

//        // Get the dimensions of the original bitmap
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, options);
////        int scaleFactor = Math.min(photoW/reqWidth, photoH/reqHeight);
//        // Calculate inSampleSize
//        options.inSampleSize = calculateSampleSize(options, reqWidth, reqHeight);
//
//        // Decode bitmap with inSampleSize set
//        options.inJustDecodeBounds = false;
//        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int width = options.outWidth;
        final int height = options.outHeight;
        Log.d("decodeBitmapFromFile", width + " " + height + " " + reqWidth + " " + reqHeight);

        int inSampleSize = 8;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d("Utils", "inSampleSize = " + inSampleSize);
        return inSampleSize;

    }

    /**
     * Save the bitmap to the specified path
     * @param path absolute path to save the bitmap
     * @param bitmap bitmap to be saved
     */
    public static void saveBitmapFile(String path, Bitmap bitmap) {
        deleteFile(path);
        try {
            FileOutputStream outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d("saveBitmapFile", "new size: " + new File(path).length());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            Log.d("Utils", "file deleted");
        }
    }
}
