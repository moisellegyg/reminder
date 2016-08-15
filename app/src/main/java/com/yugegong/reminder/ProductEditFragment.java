package com.yugegong.reminder;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
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
import java.io.IOException;
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

    // Keys for savedInstanceState bundle
    private static final String KEY_NAME = "prod_name";
    private static final String KEY_CREATE_TIME = "create_time";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_IMG_PATH = "path";
    private static final String KEY_IMG_URI = "uri";

    // Values
    private String mName;
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
//        private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM d, yyyy", getResources().getConfiguration().locale);

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
            timestamp = rightNow.getTimeInMillis();
            this.setText(Utils.getDateTimeString(timestamp));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_product_edit, container, false);
        initView(rootView);
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
        return rootView;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState");
        mName = savedInstanceState.getString(KEY_NAME);
        mFromEditTxt.timestamp = savedInstanceState.getLong(KEY_CREATE_TIME, -1);
        mToEditTxt.timestamp = savedInstanceState.getLong(KEY_EXPIRE_TIME, -1);
        mCurrentPhotoPath = savedInstanceState.getString(KEY_IMG_PATH);
        mPhotoURI = savedInstanceState.getParcelable(KEY_IMG_URI);

        if (mName != null && mName.length() > 0) mNameEditTxt.setText(mName);
        if (mFromEditTxt.timestamp != -1) mFromEditTxt.setText(Utils.getDateTimeString(mFromEditTxt.timestamp));
        if (mToEditTxt.timestamp != -1) mToEditTxt.setText(Utils.getDateTimeString(mToEditTxt.timestamp));
        if (mCurrentPhotoPath != null && mCurrentPhotoPath.length() > 0) {
            mImageView.loadImageFromFile(mCurrentPhotoPath);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        mName = mNameEditTxt.getText().toString();
        if (mName != null && mName.length() > 0) {
            outState.putString(KEY_NAME, mName);
        }
        if (mFromEditTxt.timestamp != -1) {
            outState.putLong(KEY_CREATE_TIME, mFromEditTxt.timestamp);
        }
        if (mToEditTxt.timestamp != -1) {
            outState.putLong(KEY_EXPIRE_TIME, mToEditTxt.timestamp);
        }
        if (mPhotoURI != null) {
            outState.putParcelable(KEY_IMG_URI, mPhotoURI);
        }
        if (mCurrentPhotoPath != null && mCurrentPhotoPath.length() > 0) {
            outState.putString(KEY_IMG_PATH, mCurrentPhotoPath);
        }

        super.onSaveInstanceState(outState);
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
        Log.d(TAG, "onActivityResult");
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            mImageView.loadImageAfterSave(mCurrentPhotoPath,
                    mImageFrameLayout.getWidth(), mImageFrameLayout.getHeight());
        }
    }

    private void setDate(View v) {
        Calendar rightNow = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (DatePickEditText)v,
                rightNow.get(rightNow.YEAR),
                rightNow.get(rightNow.MONTH),
                rightNow.get(rightNow.DAY_OF_MONTH));

        // Set limitation for a calendar when the other is set
        if (v == mToEditTxt && mFromEditTxt.timestamp != -1)
            dialog.getDatePicker().setMinDate(mFromEditTxt.timestamp);

        dialog.show();
    }

    @Override
    public void onClick(View v) {
        hideSoftInput();
        if (v == mFromEditTxt || v == mToEditTxt) {
            setDate(v);
        } else if (v == mImageFrameLayout) {
            dispatchTakePictureIntent();
        } else if (v == mCancelBtn) {
            cancelEdit();
        } else if (v == mSaveBtn) {
            saveEdit();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mToEditTxt.clearFocus();
        mFromEditTxt.clearFocus();
        mNameEditTxt.clearFocus();
        hideSoftInput();
        return false;
    }


    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), 0);
    }

    private void dispatchTakePictureIntent() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                mPhotoURI = Uri.fromFile(photoFile);
                Log.d(TAG, "Input photoURI = " + mPhotoURI.toString());
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName =  "JPEG_" + timestamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image =  File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
        return image;
    }

    private void cancelEdit() {
        startMainActivity();
    }

    private void saveEdit() {

        mName = mNameEditTxt.getText().toString();
        if (mName == null || mName.length() == 0) {
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

        insertProduct(mName, mFromEditTxt.timestamp, mToEditTxt.timestamp, mCurrentPhotoPath);
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
            Log.d(TAG, "doInBackground");
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

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "onPostExecute");

        }
    }

}
