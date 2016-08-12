package com.yugegong.reminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ygong on 8/3/16.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>{
    private final static String TAG = ReminderAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;


    public ReminderAdapter(Context context) {
        mContext = context;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ProductImageView mImageView;
        public final TextView mNameTextView;
        public final TextView mCreateDateTextView;
        public final TextView mExpireDateTextView;

        public ReminderViewHolder(View view){
            super(view);
            mImageView = (ProductImageView) view.findViewById(R.id.product_image);
            mImageView.setTargetSize(Utils.displayWidthPixels(), Utils.dpToPixel(192));
            mNameTextView = (TextView) view.findViewById(R.id.product_name);
            mCreateDateTextView = (TextView) view.findViewById(R.id.item_create_date);
            mExpireDateTextView = (TextView) view.findViewById(R.id.item_expire_date);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), mNameTextView.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.list_item_reminder, parent, false);
            view.setFocusable(true);
            return new ReminderViewHolder(view);
        } else throw new RuntimeException("ReminderAdapter is not bound to RecyclerView.");
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder " + position + " " + mCursor.getCount());
        if (mCursor == null) return;
        if (mCursor.moveToPosition(position)) {
            String name = mCursor.getString(MainActivityFragment.COL_PRODUCT_NAME);
            Log.d(TAG, name);
            holder.mNameTextView
                    .setText(name);
            holder.mCreateDateTextView
                    .setText(Utils.getDateTimeString(mCursor.getLong(MainActivityFragment.COL_PRODUCT_CREATE_DATE)));
            holder.mExpireDateTextView
                    .setText(Utils.getDateTimeString(mCursor.getLong(MainActivityFragment.COL_PRODUCT_EXPIRE_DATE)));

            String path = mCursor.getString(MainActivityFragment.COL_PRODUCT_IMG_PATH);
            Log.d(TAG, "image path: " + path);
            holder.mImageView.loadImageViewFromFile(path);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        Log.d(TAG, "getItemCount = " + mCursor.getCount());
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.d(TAG, "swap!");
        mCursor = newCursor;
        this.notifyDataSetChanged();
    }



}
