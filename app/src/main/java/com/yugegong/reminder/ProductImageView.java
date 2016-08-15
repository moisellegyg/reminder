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
public class ProductImageView extends ImageView{
    private final static String TAG = ProductImageView.class.getSimpleName();
    private int mTargetW, mTargetH;
    private String mPath;
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
    public void setImagePath(String path) {
        mPath = path;
    }

    public void setTargetSize(int width, int height) {
        mTargetW = width;
        mTargetH = height;
    }

    public boolean isImgLoaded() {
        return mIsLoaded;
    }
    public void setImgLoaded(boolean isLoaded) {
        mIsLoaded = isLoaded;
        invalidate();
        requestLayout();
    }

    public void loadImageAfterSave(String path, int targetW, int targetH) {
        mPath = path;
        mTargetW = targetW;
        mTargetH = targetH;
        SaveImageTask task = new SaveImageTask();
        task.execute(this);
    }

    private class SaveImageTask extends AsyncTask<ProductImageView, Void, Bitmap> {
        private ProductImageView imageView = null;
        @Override
        protected Bitmap doInBackground(ProductImageView... imageViews) {
            Log.d("SaveImageTask", "doInBackground");
            imageView = imageViews[0];
            Bitmap bitmap = Utils.decodeBitmapFromFile(imageView.mPath,
                    imageView.mTargetW, imageView.mTargetH);
            Utils.saveBitmapFile(imageView.mPath, bitmap);

            return BitmapFactory.decodeFile(mPath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d("SaveImageTask", "onPostExecute " + bitmap.getWidth() + " " + bitmap.getHeight());
            if (bitmap == null) {
                Log.d(TAG, "bitmap is null, no image will be loaded");
                return;
            }
            imageView.setImageBitmap(bitmap);
        }
    }

    public void loadImageFromFile(String path) {
        mPath = path;
        LoadImageTask task = new LoadImageTask();
        task.execute(this);
    }

    private class LoadImageTask extends AsyncTask<ProductImageView, Void, Bitmap> {
        private ProductImageView imageView = null;
        @Override
        protected Bitmap doInBackground(ProductImageView... imageViews) {
            Log.d(TAG, "doInBackground");
            this.imageView = imageViews[0];
            return BitmapFactory.decodeFile(imageView.mPath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute");
            if (bitmap == null) {
                Log.d(TAG, "bitmap is null, no image will be loaded");
                return;
            }
            imageView.setImageBitmap(bitmap);
        }

    }


}