package com.yugegong.reminder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yugegong.reminder.data.ProductContract;
import com.yugegong.reminder.data.ProductProvider;

/**
 * Created by ygong on 8/3/16.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder>{
    private final static String TAG = ReminderAdapter.class.getSimpleName();

    private final ReminderAdapterOnClickHandler mOnClickHandler;
    private final Context mContext;

    private Cursor mCursor;
    private ActionMode mActionMode;
    private MultiSelector mMultiSelector = new MultiSelector();
    private ModalMultiChoiceCallback mActionModeCallback = new ModalMultiChoiceCallback(mMultiSelector) {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            super.onCreateActionMode(mode, menu);
            // Inflate a menu resource providing context menu items
//            ((AppCompatActivity) mContext).getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            mode.getMenuInflater().inflate(R.menu.action_mode_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(TAG, "onActionItemClicked");
            switch (item.getItemId()) {
                case R.id.action_delete: {
                    int total = getItemCount();
                    for (int i = 0; i < total; i++) {
                        if (mMultiSelector.isItemSelected(i)) {
                            long _id = mMultiSelector.getItemId(i);
                            Log.d(TAG, "delete _id = " + _id);
                            String[] selectionArgs = {Long.toString(_id)};
                            mContext.getContentResolver().delete(
                                    ProductContract.ProductEntry.CONTENT_URI,
                                    ProductProvider.PRODUCT_ID_SELECTION,
                                    selectionArgs);
//                            notifyItemRemoved(i);
                        }
                    }
                    mode.finish();
                    return true;
                }
                default:
                    return false;
            }
        }
    };

    public ReminderAdapter(Context context, ReminderAdapterOnClickHandler handler) {
        mContext = context;
        mOnClickHandler = handler;
        setHasStableIds(true);
    }

    public class ViewHolder extends MultiSelectableHolder
            implements View.OnClickListener, View.OnLongClickListener {

        public final ProductImageView mImageView;
        public final TextView mNameTextView;
        public final TextView mCreateDateTextView;
        public final TextView mExpireDateTextView;

        public ViewHolder(View itemView){
            super(itemView, mMultiSelector);
            mImageView = (ProductImageView) itemView.findViewById(R.id.product_image);
//            mImageView.setTargetSize(Utils.displayWidthPixels(), Utils.convertDpToPixels(192));
            mNameTextView = (TextView) itemView.findViewById(R.id.product_name);
            mCreateDateTextView = (TextView) itemView.findViewById(R.id.item_create_date);
            mExpireDateTextView = (TextView) itemView.findViewById(R.id.item_expire_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindView(Cursor cursor) {
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
                if (!toggleItemSelection(this, position, _id)) {
                    mOnClickHandler.onClick(this, _id);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("VH", "onLongClick");
            int position = getAdapterPosition();
            if (mCursor.moveToPosition(position)) {
                long _id = mCursor.getLong(ProductListFragment.COL_PRODUCT_ID);
                if (!mMultiSelector.isSelectable()) {
                    mActionMode = ((AppCompatActivity) mContext).startSupportActionMode(mActionModeCallback);
                    // The first checked item in multi choice mode
                    mMultiSelector.setItemSelected(this, position, _id, true);
                    updateActionModeTitle();
                    return true;
                }
            }

            return false;
        }
    }

    public interface ReminderAdapterOnClickHandler {
        void onClick(ViewHolder viewHolder, long _id);
    }

    private boolean toggleItemSelection(MultiSelectableHolder holder, int position, long _id) {
        boolean success = mMultiSelector.toggleItemSelection(holder, position, _id);
        if (success) {
            updateActionModeTitle();
        }
        return success;
    }

    private void updateActionModeTitle() {
        if (mActionMode == null) return;
        int count = mMultiSelector.getCheckedItemCount();
        String title = mContext.getResources()
                .getQuantityString(R.plurals.number_of_items_selected, count, count);
        mActionMode.setTitle(title);
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
        Log.d(TAG, "onBindViewHolder " + position + " " + mCursor.getCount() + " "
                + holder.itemView.isActivated() + " " + mMultiSelector.isItemSelected(position));

        if (mCursor == null) return;
        if (mCursor.moveToPosition(position)) {
            holder.bindView(mCursor);
            holder.onBindSelector();
        }
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds() && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(ProductListFragment.COL_PRODUCT_ID);
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        if (null == mCursor)
            return 0;
        Log.d(TAG, "getItemCount = " + mCursor.getCount());
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.d(TAG, "swap1!");
        mCursor = newCursor;
        this.notifyDataSetChanged();
    }
}
