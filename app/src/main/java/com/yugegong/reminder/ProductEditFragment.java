package com.yugegong.reminder;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ygong on 8/4/16.
 */
public class ProductEditFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, View.OnFocusChangeListener{
    private final static String TAG = ProductEditFragment.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;


    private CoordinatorLayout mEditLayout;
    private FrameLayout mImageFrameLayout;
    private ImageView mImageView;
    private TextInputEditText mNameEditTxt;
    private DatePickEditText mFromEditTxt;
    private DatePickEditText mToEditTxt;
    private Button mCancelBtn;
    private Button mSaveBtn;

    public static class DatePickEditText extends EditText implements DatePickerDialog.OnDateSetListener {
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

        public DatePickEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar rightNow = Calendar.getInstance();
            rightNow.set(year, monthOfYear, dayOfMonth);
            this.setText(mDateFormat.format(rightNow.getTime()));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Log.d(TAG, "Width = " + imageBitmap.getWidth() + " " + mImageFrameLayout.getWidth());
//            mImageFrameLayout.getLayoutParams().height = mImageFrameLayout.getWidth();
            mImageView.setImageBitmap(imageBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_product_edit, container, false);
        mEditLayout = (CoordinatorLayout) rootView.findViewById(R.id.product_edit_root);
        mEditLayout.setOnTouchListener(this);

        mImageFrameLayout = (FrameLayout) rootView.findViewById(R.id.product_image_frame);
        mImageFrameLayout.setOnTouchListener(this);
        mImageFrameLayout.setOnClickListener(this);

        mImageView = (ImageView) rootView.findViewById(R.id.product_image);

        mNameEditTxt = (TextInputEditText) rootView.findViewById(R.id.product_name);
        mNameEditTxt.setOnFocusChangeListener(this);

        mFromEditTxt = (DatePickEditText) rootView.findViewById(R.id.created_time);
        mFromEditTxt.setInputType(InputType.TYPE_NULL);
        mFromEditTxt.setOnClickListener(this);
        mToEditTxt = (DatePickEditText) rootView.findViewById(R.id.expired_time);
        mToEditTxt.setInputType(InputType.TYPE_NULL);
        mToEditTxt.setOnClickListener(this);
        return rootView;
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
        if (v == mFromEditTxt || v == mToEditTxt) {
            hideSoftInput(getActivity(), v);
            setDate(v);
        } else if (v == mImageFrameLayout) {
            capturePhoto();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Toast.makeText(getContext(), "Touched!", Toast.LENGTH_SHORT).show();
        if (v == mEditLayout || v == mImageFrameLayout) {
            hideSoftInput(getActivity(), v);
            mNameEditTxt.clearFocus();
            mFromEditTxt.clearFocus();
            mToEditTxt.clearFocus();

        } else {
            Toast.makeText(getContext(), "Touched other views.", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == mNameEditTxt && !hasFocus) {
            hideSoftInput(getActivity(), v);
        }
    }

    private void hideSoftInput(Activity activity, View v) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    }

    private void capturePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }
}
