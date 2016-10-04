package com.yugegong.reminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yugegong.reminder.data.ProductContract;
import com.yugegong.reminder.data.ProductQueryHandler;
import com.yugegong.reminder.notification.AlarmService;

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
    public static final String INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION = "disableDeleteMenuOption";
//    private boolean mDisableDeleteMenuOption;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String PRODUCT_URI = "uri";
    private Uri mProductUri;

    // Keys for savedInstanceState bundle
    private static final String KEY_NAME = "prod_name";
    private static final String KEY_CREATE_TIME = "create_time";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_RETRIEVED_IMG_PATH = "saved_path";
    private static final String KEY_LAST_IMG_URI = "last_uri";
    private static final String KEY_PREINSERT_URI = "preinsert_uri";
    private static final String KEY_IS_USED = "is_used";

    // Values
    private String mName;
    private String mRetrievedImgPath;
    private Uri mPreInsertUri;
    private Uri mLastUri;
    private boolean mIsUsed;

//    private LinearLayout mEditLayout;
    private FrameLayout mImageFrameLayout;
    private ProductImageView mImageView;
    private TextInputEditText mNameEditTxt;
    private DatePickEditText mFromEditTxt;
    private DatePickEditText mToEditTxt;
    private LinearLayout mCancelBtn;
    private LinearLayout mSaveBtn;
    private CheckBox mUsedCheckBox;

    private static final String[] PRODUCT_COLUMNS = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_UPC,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IMG_PATH,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_CREATE_DATE,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED
    };

//    public static final int COL_PRODUCT_ID = 0;
    public static final int COL_PRODUCT_NAME = 1;
//    public static final int COL_PRODUCT_UPC = 2;
    public static final int COL_PRODUCT_IMG_PATH = 3;
    public static final int COL_PRODUCT_CREATE_DATE = 4;
    public static final int COL_PRODUCT_EXPIRE_DATE = 5;
    public static final int COL_PRODUCT_IS_USED = 6;

    public static class DatePickEditText extends TextInputEditText implements DatePickerDialog.OnDateSetListener {

        private static final int START_DATE_TYPE = 0;
        private static final int END_DATE_TYPE = 1;
        private long timestamp = -1;
        // startDate = 0, endDate = 1;
        private int mDateType = 0;

        public DatePickEditText(Context context) {
            super(context);
        }
        public DatePickEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.DatePickEditText,
                    0, 0);

            try {
                mDateType = a.getInteger(R.styleable.DatePickEditText_dateType, 0);
            } finally {
                a.recycle();
            }
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar rightNow = Calendar.getInstance();
            rightNow.set(year, monthOfYear, dayOfMonth);
            if (mDateType == END_DATE_TYPE) {
                rightNow.set(Calendar.HOUR_OF_DAY, 23);
                rightNow.set(Calendar.MINUTE, 59);
                rightNow.set(Calendar.SECOND, 59);
                rightNow.set(Calendar.MILLISECOND, 0);
            } else {
                rightNow.set(Calendar.HOUR_OF_DAY, 0);
                rightNow.set(Calendar.MINUTE, 0);
                rightNow.set(Calendar.SECOND, 0);
                rightNow.set(Calendar.MILLISECOND, 0);
            }

            Log.d(TAG, rightNow.getTime().toString());
            timestamp = rightNow.getTimeInMillis();
            this.setText(Utils.getDateTimeString(timestamp));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        /*
          Should not call to retain this fragment since it's fragment with UI and will cause a memory
          leak if you don't set all the views' references to null when the Fragment calls onDestroyView.
          Better to call setRetainInstance(true) when it's a fragment without UI.
          https://developer.android.com/guide/topics/resources/runtime-changes.html#HandlingTheChange
          setRetainInstance(true);
         */
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_product_edit, container, false);
        bindViews(rootView);
        setupCustomActionBar();
        setOnClickListeners(mImageFrameLayout, mCancelBtn, mSaveBtn);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            final Bundle args = getArguments();
            if (args != null) {
                mProductUri = args.getParcelable(PRODUCT_URI);
                if (mProductUri != null) {
                    Log.d(TAG, mProductUri.toString());
                    getProductFromUri(mProductUri);
                }
            }
        }
        bindDataToViews();
    }

    private void bindViews(View rootView) {
        LinearLayout editLayout = (LinearLayout) rootView.findViewById(R.id.product_edit_root);
        editLayout.setOnTouchListener(this);
        mImageFrameLayout = (FrameLayout) rootView.findViewById(R.id.product_image_frame);
        mImageView = (ProductImageView) rootView.findViewById(R.id.product_image);
        mNameEditTxt = (TextInputEditText) rootView.findViewById(R.id.product_name);
        mNameEditTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                // When mNameEditText loses focus, IME will be hidden
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                }
            }
        });
        mUsedCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox_is_used);
        mFromEditTxt = (DatePickEditText) rootView.findViewById(R.id.created_time);
        mToEditTxt = (DatePickEditText) rootView.findViewById(R.id.expired_time);
        mToEditTxt.setInputType(InputType.TYPE_NULL);
        mToEditTxt.setOnTouchListener(this);
    }

    private void setupCustomActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setCustomView(R.layout.edit_product_custom_actionbar);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                            ActionBar.DISPLAY_SHOW_TITLE);

            View customActionBarView = actionBar.getCustomView();
            if (customActionBarView != null) {
                mCancelBtn = (LinearLayout) customActionBarView.findViewById(R.id.cancel_menu_item);
                mSaveBtn = (LinearLayout) customActionBarView.findViewById(R.id.save_menu_item);
            }
        }
    }

    private void bindDataToViews() {
        if (mName != null && mName.length() > 0) mNameEditTxt.setText(mName);
        if (mFromEditTxt.timestamp != -1) {
            mFromEditTxt.setText(Utils.getDateTimeString(mFromEditTxt.timestamp));
        } else {
            mFromEditTxt.timestamp = Utils.getTodayTimeInMillis();
            mFromEditTxt.setText(Utils.getDateTimeString(mFromEditTxt.timestamp));
        }
        if (mToEditTxt.timestamp != -1) mToEditTxt.setText(Utils.getDateTimeString(mToEditTxt.timestamp));
        if (mRetrievedImgPath != null) {
            mImageView.loadImageFromFile(mRetrievedImgPath);
        } else if (mLastUri != null) {
            mImageView.loadImageFromFile(mLastUri.getPath());
        }
        mUsedCheckBox.setChecked(mIsUsed);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState");
        mName = savedInstanceState.getString(KEY_NAME);
        mFromEditTxt.timestamp = savedInstanceState.getLong(KEY_CREATE_TIME, -1);
        mToEditTxt.timestamp = savedInstanceState.getLong(KEY_EXPIRE_TIME, -1);
        mIsUsed = savedInstanceState.getBoolean(KEY_IS_USED, false);
        mRetrievedImgPath = savedInstanceState.getString(KEY_RETRIEVED_IMG_PATH);
        mLastUri = savedInstanceState.getParcelable(KEY_LAST_IMG_URI);
        mPreInsertUri = savedInstanceState.getParcelable(KEY_PREINSERT_URI);

//        if (mLastUri != null) Log.d(TAG, "restoreInstanceState mLastUri = " + mLastUri.toString());
//        if (mPreInsertUri != null) Log.d(TAG, "restoreInstanceState mPreInsertUri = " + mPreInsertUri.toString());
    }

    private void getProductFromUri(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, PRODUCT_COLUMNS, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            mName = cursor.getString(COL_PRODUCT_NAME);
            mFromEditTxt.timestamp = cursor.getLong(COL_PRODUCT_CREATE_DATE);
            mToEditTxt.timestamp = cursor.getLong(COL_PRODUCT_EXPIRE_DATE);
            mIsUsed = cursor.getInt(COL_PRODUCT_IS_USED) == 1;
            mRetrievedImgPath = cursor.getString(COL_PRODUCT_IMG_PATH);
            mPreInsertUri = null;
            mLastUri = null;
            Log.d(TAG, mName + " retrieved. " + cursor.getInt(COL_PRODUCT_IS_USED));
            cursor.close();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        mName = mNameEditTxt.getText().toString();
        if (mName.length() > 0) {
            outState.putString(KEY_NAME, mName);
        }
        if (mFromEditTxt.timestamp != -1) {
            outState.putLong(KEY_CREATE_TIME, mFromEditTxt.timestamp);
        }
        if (mToEditTxt.timestamp != -1) {
            outState.putLong(KEY_EXPIRE_TIME, mToEditTxt.timestamp);
        }
        outState.putBoolean(KEY_IS_USED, mUsedCheckBox.isChecked());

        if (mRetrievedImgPath != null && mRetrievedImgPath.length() > 0) {
            outState.putString(KEY_RETRIEVED_IMG_PATH, mRetrievedImgPath);
        }
        if (mPreInsertUri != null) {
            Log.d(TAG, "onSaveInstanceState mPreInsertUri = " + mPreInsertUri.toString());
            outState.putParcelable(KEY_PREINSERT_URI, mPreInsertUri);
        }
        if (mLastUri != null) {
            Log.d(TAG, "onSaveInstanceState mLastUri = " + mLastUri.toString());
            outState.putParcelable(KEY_LAST_IMG_URI, mLastUri);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode + " " + requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            if (mLastUri != null) {
                Log.d(TAG, "old mLastUri = " + mLastUri.toString());
                Utils.deleteFile(mLastUri.getPath());
                mLastUri = null;
            }

            if (data != null) mLastUri = data.getData();
            if (mLastUri == null && mPreInsertUri != null) {
                mLastUri = mPreInsertUri;
                Log.d(TAG, "new mLastUri = " + mLastUri.toString());
            }
            mImageView.loadImageAfterSaveToUri(mLastUri);
        }

        mPreInsertUri = null;
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
                mPreInsertUri = Uri.fromFile(photoFile);
                Log.d(TAG, "Input mPreInsertUri = " + mPreInsertUri.toString());
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPreInsertUri);
                startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Nullable
    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName =  "JPEG_" + timestamp + ".jpg";

        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) return null;

//        File storageDir = getContext().getExternalCacheDir();
        String path = storageDir.getAbsolutePath() + "/" + imageFileName;
//        File image =  File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        mCurrentPhotoPath = image.getAbsolutePath();
//        Log.d(TAG, "mCurrentPhotoPath = " + mCurrentPhotoPath);
        return new File(path);
    }

    private void setDate(View v) {
        Calendar rightNow = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (DatePickEditText)v,
                rightNow.get(Calendar.YEAR),
                rightNow.get(Calendar.MONTH),
                rightNow.get(Calendar.DAY_OF_MONTH));

        // Set limitation for a calendar when the other is set
        if (v == mToEditTxt && mFromEditTxt.timestamp != -1)
            dialog.getDatePicker().setMinDate(mFromEditTxt.timestamp);

        dialog.show();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        if (v == mImageFrameLayout) {
            dispatchTakePictureIntent();
        } else if (v == mCancelBtn) {
            cancelEdit();
        } else if (v == mSaveBtn) {
            saveEdit();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch " + event.toString());
        if (v == mFromEditTxt || v == mToEditTxt) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setDate(v);
            }
        } else {
            Log.d(TAG, "onTouch else where");
            mToEditTxt.clearFocus();
            mFromEditTxt.clearFocus();
            mNameEditTxt.clearFocus();
        }
        return true;
    }

    private void cancelEdit() {
        if (mLastUri != null) {
            Log.d(TAG, "delete: " + mLastUri.toString());
            Utils.deleteFile(mLastUri.getPath());
        }
        getActivity().onBackPressed();
    }

    private void saveEdit() {
        mName = mNameEditTxt.getText().toString();
        if (mName.length() == 0) {
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
        String currentImgPath = mImageView.getPath();
        if (mRetrievedImgPath != null && !mRetrievedImgPath.equals(currentImgPath)) {
            Log.d(TAG, "delete " + mRetrievedImgPath);
            Utils.deleteFile(mRetrievedImgPath);
        }

        int isUsed = mUsedCheckBox.isChecked() ? 1 : 0;
        saveProduct(mProductUri, mName, mFromEditTxt.timestamp, mToEditTxt.timestamp, currentImgPath, isUsed);
        startMainActivity();
    }

    private void saveProduct(Uri productUri, String productName, long createTimestamp, long expireTimestamp, String imgPath, int isUsed) {
        EditProductTask task = new EditProductTask(productUri);
        task.execute(productName, Long.toString(createTimestamp), Long.toString(expireTimestamp), imgPath, Integer.toString(isUsed));
    }

    private void setOnClickListeners(View... views) {
        for (View view : views) {
            view.setOnClickListener(this);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private class EditProductTask extends AsyncTask<String, Void, Uri> {
        private Uri mUpdateUri;
        private String mProductName;
        private Long mExpireTimestamp;
        private int mIsUsed;
        EditProductTask(Uri productUri) {
            mUpdateUri = productUri;
        }

        @Override
        protected Uri doInBackground(String... params) {
            Log.d("EditProductTask", "doInBackground");
            mProductName = params[0];
            Long createTimestamp = Long.parseLong(params[1]);
            mExpireTimestamp = Long.parseLong(params[2]);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mExpireTimestamp);
            Log.d("EditProductTask", "expiredTime = " + c.getTime().toString());
            String imgPath = params[3];
            mIsUsed = Integer.parseInt(params[4]);

            ContentValues contentValues = new ContentValues();
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME, mProductName);
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_CREATE_DATE, createTimestamp);
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE, mExpireTimestamp);
            contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED, mIsUsed);

            if (imgPath != null && imgPath.length() != 0) {
                contentValues.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IMG_PATH, imgPath);
            }

            if (mUpdateUri != null) {
                Log.d("EditProductTask", "update");
                getContext().getContentResolver().update(mUpdateUri, contentValues, null, null);
            } else {
                Uri contentUri = ProductContract.ProductEntry.CONTENT_URI;
                mUpdateUri = getContext().getContentResolver().insert(contentUri, contentValues);
                Log.d("EditProductTask", "insert to " + mUpdateUri.toString());
            }

            return mUpdateUri;
        }

        @Override
        protected void onPostExecute(Uri productUri) {
            if (mIsUsed == 1) return;
            long currentTimeInMillis = Utils.getCurrentTimeInMillis();
            long notifyAtMillis = mExpireTimestamp - Utils.THREE_DAYS_IN_MILLIS;
            long triggerAtMillis = notifyAtMillis < currentTimeInMillis ? currentTimeInMillis : notifyAtMillis;
            scheduleNotification(productUri, mProductName, triggerAtMillis, mExpireTimestamp);
        }

        private void scheduleNotification(Uri productUri, String productName, long triggerAtMillis, long expireTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(triggerAtMillis);
            Log.d("EditProductTask", "scheduleNotification at: " + calendar.getTime().toString());
            AlarmService alarmService = new AlarmService(getContext(), productUri, productName, expireTime);
            alarmService.setAlarm(triggerAtMillis);
        }

    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_edit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        MenuItem deleteMenu = menu.findItem(R.id.action_delete);
        boolean disableDeleteMenuOption = getActivity().getIntent().getBooleanExtra(INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION, false);
        deleteMenu.setVisible(!disableDeleteMenuOption);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.action_delete: {
                startMainActivity();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
    }

}
