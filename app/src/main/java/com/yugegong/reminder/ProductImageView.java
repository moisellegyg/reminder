package com.yugegong.reminder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by ygong on 8/11/16.
 */
public class ProductImageView extends ImageView {
    private final static String TAG = ProductImageView.class.getSimpleName();
    private int mTargetW, mTargetH;
    private String mImgPath;
    private boolean mIsLoaded;
    public ProductImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProductImageView, 0, 0);
        try {
            mIsLoaded = a.getBoolean(R.styleable.ProductImageView_imgLoaded, false);
        } finally {
            a.recycle();
        }
    }

    public void setTargetSize(int targetWidth, int targetHeight) {
        mTargetW = targetWidth;
        mTargetH = targetHeight;
    }

    public boolean isImgLoaded() {
        return mIsLoaded;
    }
    public void setImgLoaded(boolean isLoaded) {
        mIsLoaded = isLoaded;
        invalidate();
        requestLayout();
    }

    public void loadImageViewFromFile(String path, int targetW, int targetH) {
        setTargetSize(targetW, targetH);
        loadImageViewFromFile(path);
    }

    public void loadImageViewFromFile(String path) {
        mImgPath = path;
        if (mTargetW == 0 || mTargetH == 0) {
            Log.d(TAG, "need to set target size for this ImageView");
            return;
        }
//        ContentResolver cr = getContext().getContentResolver();
////            cr.notifyChange(mPhotoURI, null);
//        Bitmap imageBitmap;
//        try {
//            imageBitmap = MediaStore.Images.Media.getBitmap(cr, mPhotoURI);
//            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            mImageView.setImageBitmap(imageBitmap);
//
//        } catch (IOException e) {
//            Toast.makeText(getContext(), "Failed to load", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Failed to load", e);
//        }
        LoadImageTask task = new LoadImageTask();
        task.execute(this);
//        mImageView.setImgLoaded(true);
    }


    private class LoadImageTask extends AsyncTask<ProductImageView, Void, Bitmap> {
        ProductImageView imageView = null;
        @Override
        protected Bitmap doInBackground(ProductImageView... imageViews) {
            this.imageView = imageViews[0];
            return decodeBitmapFromFile(mImgPath, mTargetW, mTargetH);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                Log.d(TAG, "bitmap is null, no image will be loaded");
                return;
            }
            imageView.setImageBitmap(bitmap);
        }

        private Bitmap decodeBitmapFromFile(String path, int targetW, int targetH) {

            // Get the dimensions of the original bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            int photoW = options.outWidth;
            int photoH = options.outHeight;

            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;

            // Decode bitmap with inSampleSize set
            return BitmapFactory.decodeFile(path, options);
        }
    }
}