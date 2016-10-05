package com.yugegong.reminder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by ygong on 8/11/16.
 */
public class ProductImageView extends ImageView{
    public static final double HEIGHT_WIDTH_RATIO = 3/4;

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = width * 3/4;
//        Log.d(TAG, "width = " + width + " height = " + height);
        mTargetH = height;
        mTargetW = width;
        setMeasuredDimension(width, height);
    }

//    public void setTargetSize(int width, int height) {
//        mTargetW = width;
//        mTargetH = height;
//    }

    public String getPath() {
        return mPath;
    }

    public boolean isImgLoaded() {
        return mIsLoaded;
    }
    public void setImgLoaded(boolean isLoaded) {
        mIsLoaded = isLoaded;
        invalidate();
        requestLayout();
    }

    public void loadImageAfterSaveToUri(Uri uri) {
        if (uri == null) return;
        String path = uri.getPath();
        if (path == null || path.length() == 0) return;
        mPath = path;
        SaveImageTask task = new SaveImageTask();
        task.execute(this);
    }

    private class SaveImageTask extends AsyncTask<ProductImageView, Void, Bitmap> {
        private ProductImageView imageView = null;
        @Override
        protected Bitmap doInBackground(ProductImageView... imageViews) {
//            Log.d("SaveImageTask", "doInBackground");
            imageView = imageViews[0];
            Bitmap bitmap = Utils.decodeBitmapFromFile(imageView.mPath,
                    imageView.mTargetW, imageView.mTargetH);
//            Log.d("SaveImageTask", bitmap.getWidth() + " " + bitmap.getHeight());

            Utils.saveBitmapFile(imageView.mPath, bitmap);

            return BitmapFactory.decodeFile(mPath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                Log.i(TAG, "bitmap is null, no image will be loaded");
                return;
            }
//            Log.d("SaveImageTask", "imageView " + imageView.getWidth() + " " + imageView.getHeight()
//                    + " " + bitmap.getWidth() + " " + bitmap.getHeight());
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ScaleType.CENTER_CROP);
        }
    }

    public void loadImageFromFile(String path) {
        if (path == null || path.length() == 0) return;
        mPath = path;
        LoadImageTask task = new LoadImageTask();
        task.execute(this);
    }

    private class LoadImageTask extends AsyncTask<ProductImageView, Void, Bitmap> {
        private ProductImageView imageView = null;
        @Override
        protected Bitmap doInBackground(ProductImageView... imageViews) {
//            Log.d("LoadImageTask", "doInBackground");
            this.imageView = imageViews[0];
            return BitmapFactory.decodeFile(imageView.mPath);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
//            Log.d("LoadImageTask", "onPostExecute");
            if (bitmap == null) {
//                Log.d(TAG, "bitmap is null, no image will be loaded");
                return;
            }
            Log.d("onPostExecute", "image path: " + imageView.mPath);
            imageView.setImageBitmap(bitmap);
            imageView.setScaleType(ScaleType.CENTER_CROP);

        }

    }


}