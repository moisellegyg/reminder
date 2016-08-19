package com.yugegong.reminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ygong on 8/3/16.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder>{
    private final static String TAG = ReminderAdapter.class.getSimpleName();

    private final ReminderAdapterOnClickHandler mOnClickHandler;
    private final Context mContext;

    private Cursor mCursor;

    public ReminderAdapter(Context context, ReminderAdapterOnClickHandler handler) {
        mContext = context;
        mOnClickHandler = handler;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ProductImageView mImageView;
        public final TextView mNameTextView;
        public final TextView mCreateDateTextView;
        public final TextView mExpireDateTextView;

        public ViewHolder(View itemView){
            super(itemView);

            mImageView = (ProductImageView) itemView.findViewById(R.id.product_image);
//            mImageView.setTargetSize(Utils.displayWidthPixels(), Utils.convertDpToPixels(192));

            mNameTextView = (TextView) itemView.findViewById(R.id.product_name);
            mCreateDateTextView = (TextView) itemView.findViewById(R.id.item_create_date);
            mExpireDateTextView = (TextView) itemView.findViewById(R.id.item_expire_date);
            itemView.setOnClickListener(this);
        }

        public void bindCursor(Cursor cursor) {
            String name = cursor.getString(ProductListFragment.COL_PRODUCT_NAME);
            Log.d(TAG, name);
            mNameTextView.setText(name);
            mCreateDateTextView.setText(
                    Utils.getDateTimeString(cursor.getLong(ProductListFragment.COL_PRODUCT_CREATE_DATE))
            );
            mExpireDateTextView.setText(
                    Utils.getDateTimeString(cursor.getLong(ProductListFragment.COL_PRODUCT_EXPIRE_DATE))
            );

            String path = cursor.getString(ProductListFragment.COL_PRODUCT_IMG_PATH);
            Log.d(TAG, "image path: " + path);
            mImageView.loadImageFromFile(path);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (mCursor.moveToPosition(position)) {
                long _id = mCursor.getLong(ProductListFragment.COL_PRODUCT_ID);

                mOnClickHandler.onClick(this, _id);

            }
        }
    }

    public interface ReminderAdapterOnClickHandler {
        void onClick(ViewHolder viewHolder, long _id);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.list_item_reminder, parent, false);
            view.setFocusable(true);
            return new ViewHolder(view);
        } else throw new RuntimeException("ReminderAdapter is not bound to RecyclerView.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder " + position + " " + mCursor.getCount());

        if (mCursor == null) return;
        if (mCursor.moveToPosition(position)) {
            holder.bindCursor(mCursor);
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
