package com.yugegong.reminder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yugegong.reminder.data.ProductContract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ygong on 8/4/16.
 */
public class ProductEditFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {
    private final static String TAG = ProductEditFragment.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 300;

    private Uri mPhotoURI;
    private String mCurrentPhotoPath;

    private FrameLayout mEditLayout;
    private FrameLayout mImageFrameLayout;
    private ProductImageView mImageView;
    private TextInputEditText mNameEditTxt;
    private DatePickEditText mFromEditTxt;
    private DatePickEditText mToEditTxt;
    private LinearLayout mCancelBtn;
    private LinearLayout mSaveBtn;


    public static class DatePickEditText extends EditText implements DatePickerDialog.OnDateSetListener {

        private long timestamp = -1;
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy", getResources().getConfiguration().locale);

        public DatePickEditText(Context context) {
            super(context);
        }

        public DatePickEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public DatePickEditText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar rightNow = Calendar.getInstance();
            rightNow.set(year, monthOfYear, dayOfMonth);
            this.setText(mDateFormat.format(rightNow.getTime()));
            timestamp = rightNow.getTimeInMillis();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_product_edit, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mEditLayout = (FrameLayout) rootView.findViewById(R.id.product_edit_root);
        mEditLayout.setOnTouchListener(this);

        mImageFrameLayout = (FrameLayout) rootView.findViewById(R.id.product_image_frame);
        mImageView = (ProductImageView) rootView.findViewById(R.id.product_image);

        mNameEditTxt = (TextInputEditText) rootView.findViewById(R.id.product_name);

        mFromEditTxt = (DatePickEditText) rootView.findViewById(R.id.created_time);
        mFromEditTxt.setInputType(InputType.TYPE_NULL);
        mToEditTxt = (DatePickEditText) rootView.findViewById(R.id.expired_time);
        mToEditTxt.setInputType(InputType.TYPE_NULL);

        mCancelBtn = (LinearLayout) rootView.findViewById(R.id.btn_cancel);
        mSaveBtn = (LinearLayout) rootView.findViewById(R.id.btn_save);
        setOnClickListeners(mImageFrameLayout, mFromEditTxt, mToEditTxt, mCancelBtn, mSaveBtn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        galleryAddPic(mPhotoURI);

        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                mImageView.loadImageViewFromFile(mCurrentPhotoPath,
                        mImageFrameLayout.getWidth(), mImageFrameLayout.getHeight());

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG, "requestCode = " + requestCode);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // related task you need to do.
                    mImageView.loadImageViewFromFile(mCurrentPhotoPath,
                            mImageFrameLayout.getWidth(), mImageFrameLayout.getHeight());

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "Permission denied. Cannot load the photo.", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void galleryAddPic(Uri photoURI) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(photoURI);
        getContext().sendBroadcast(mediaScanIntent);
    }

    private void setDate(View v) {
        Calendar rightNow = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(), (DatePickEditText)v,
                rightNow.get(rightNow.YEAR), rightNow.get(rightNow.MONTH), rightNow.get(rightNow.DAY_OF_MONTH));
//        if (v == mToEditTxt) dialog.getDatePicker().setMinDate(rightNow.getTime().getTime());
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
        hideSoftInput();
        if (v == mFromEditTxt || v == mToEditTxt) {
            setDate(v);
        } else if (v == mImageFrameLayout) {
            capturePhoto();
        } else if (v == mCancelBtn) {
            cancelEdit();
        } else if (v == mSaveBtn) {
            saveEdit();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "Touched!");
        mToEditTxt.clearFocus();
        mFromEditTxt.clearFocus();
        mNameEditTxt.clearFocus();
        Log.d(TAG, "v.isFocused " + v.isFocused());
        hideSoftInput();
        return false;
    }


    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), 0);
    }

    private void capturePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                mPhotoURI = Uri.fromFile(photoFile);
                Log.d(TAG, "Input photoURI = " + mPhotoURI.toString());
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + ".jpg";
        File storeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Reminder");
        storeDir.mkdir();
        File image = new File(storeDir, imageFileName);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
        return image;
    }

    private void cancelEdit() {
        startMainActivity();
    }

    private void saveEdit() {

        String name = mNameEditTxt.getText().toString();
        if (name == null || name.length() == 0) {
            Toast.makeText(getContext(), "Product name is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mFromEditTxt.timestamp == -1) {
            Toast.makeText(getContext(), "Create date is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mToEditTxt.timestamp == -1) {
            Toast.makeText(getContext(), "Expire date is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        insertProduct(name, mFromEditTxt.timestamp, mToEditTxt.timestamp, mCurrentPhotoPath);
        Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();

        startMainActivity();
    }

    private void setOnClickListeners(View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    private void insertProduct(String productName, long createTimestamp, long expireTimestamp, String imgPath) {
        EditProductTask task = new EditProductTask();
        task.execute(productName, Long.toString(createTimestamp), Long.toString(expireTimestamp), imgPath);




    }

    private class EditProductTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String productName = params[0];
            String createTimestamp = params[1];
            String expireTimestamp = params[2];
            String imgPath = params[3];

            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME, productName);
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_CREATE_DATE, createTimestamp);
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE, expireTimestamp);

            if (imgPath != null && imgPath.length() != 0) {
                contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IMG_PATH, imgPath);
            }

            Uri productUri = ProductContract.ProductEntry.CONTENT_URI;
            Uri newUri = getContext().getContentResolver().insert(productUri, contentValues);
            Log.d(TAG, newUri.toString());
            return null;
        }
    }

}
