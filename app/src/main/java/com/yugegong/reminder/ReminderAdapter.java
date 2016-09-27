package com.yugegong.reminder;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private final static String TAG = ReminderAdapter.class.getSimpleName();

    private final OnItemClickCallback mOnItemClickCallback;
    private final Context mContext;
    /**
     * Cursor type data for the {@code ReminderAdapter}
     */
    private Cursor mCursor;

    /**
     * Public constructor.
     * @param context The context the adapter is being created in.
     * @param listener Implementation for {@link OnItemClickCallback}
     */
    public ReminderAdapter(Context context, OnItemClickCallback listener) {
        mContext = context;
        mOnItemClickCallback = listener;
        // Set to be true so that each list view item would has a unique ID.
        // This will help the adapter to figure out which view items are selected by MultiSelectionState.
        setHasStableIds(true);
    }

    /**
     * Interface definition for a callback to be invoked when a list view item in
     * {@link ReminderAdapter} is being clicked.
     */
    public interface OnItemClickCallback {
        /**
         * Called when a list view item is being clicked.
         * @param vh The view holder that holds the list view item being clicked
         */
        void onItemClicked(ViewHolder vh);
    }

    private ActionMode mActionMode;
    private MultiSelectionState mMultiSelectionState = new MultiSelectionState();
    private ModalMultiChoiceCallback mActionModeCallback = new ModalMultiChoiceCallback(mMultiSelectionState) {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            super.onCreateActionMode(mode, menu);
            // Inflate a menu resource providing context menu items
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
                        if (mMultiSelectionState.isItemSelected(i)) {
                            long _id = mMultiSelectionState.getItemId(i);
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

    /**
     * Save the state of this adapter. In this case, save the data of {@link MultiSelectionState}
     * used by this adapter.
     * @return Bundle in which to place your saved state
     */
    public Bundle saveAdapterState() {
        return mMultiSelectionState.saveSelectionStats();
    }

    /**
     * Restore the data of {@link MultiSelectionState} used by this adapter. It will also restore
     * the {@link ActionMode} status of the context in which this adapter is being used.
     * @param savedState Previous saved state for this adapter
     */
    public void restoreAdapterState(Bundle savedState) {
        mMultiSelectionState.restoreSelectionStats(savedState);
        if (mMultiSelectionState.isSelectable()) {
            mActionMode = ((AppCompatActivity) mContext).startSupportActionMode(mActionModeCallback);
            updateActionModeTitle();
        }
    }

    /**
     * Customized {@link RecyclerView.ViewHolder} used by the {@link ReminderAdapter}.
     * Each view holder in the adapter holds a view item for a single product.
     */
    public class ViewHolder extends MultiSelectableViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        final ProductImageView mImageView;
        final TextView mNameTextView;
        final TextView mCreateDateTextView;
        final TextView mExpireDateTextView;

        public ViewHolder(View itemView){
            super(itemView, mMultiSelectionState);
            mImageView = (ProductImageView) itemView.findViewById(R.id.product_image);
            mNameTextView = (TextView) itemView.findViewById(R.id.product_name);
            mCreateDateTextView = (TextView) itemView.findViewById(R.id.item_create_date);
            mExpireDateTextView = (TextView) itemView.findViewById(R.id.item_expire_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        /**
         * Bind data with views in the current {@code ViewHolder}
         * @param cursor Cursor type data will be bound
         */
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

        /**
         * This method will be called when user clicks a view item. If the {@link MultiSelectionState}
         * is turned on, this view will be added to the selected list.
         * Otherwise, it will navigate to the edit page of this selected product. Normally,a
         * {@link ProductEditActivity} will be started.
         */
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (!toggleItemSelection(this, position, getItemId())) {
                mOnItemClickCallback.onItemClicked(this);
            }
        }

        /**
         * This method will be called when user long presses a view item. Action mode and
         * multi-selection status will be started for the related {@link RecyclerView}.
         */
        @Override
        public boolean onLongClick(View v) {
            Log.d("VH", "onLongClick");
            int position = getAdapterPosition();
            if (!mMultiSelectionState.isSelectable()) {
                mActionMode = ((AppCompatActivity) mContext).startSupportActionMode(mActionModeCallback);
                // The first checked item in multi choice mode
                mMultiSelectionState.setItemSelected(this, position, getItemId(), true);
                updateActionModeTitle();
                return true;
            }

            return false;
        }
    }

    /**
     * Toggle the selection status of the current list view item. If the item is selected, then it
     * will be unselected. If the item is not selected, then it will be selected.
     * @param holder View holder that holds the list view item
     * @param position The position of the list view item in the adapter
     * @param _id Unique id of the list view item
     * @return
     */
    private boolean toggleItemSelection(MultiSelectableViewHolder holder, int position, long _id) {
        boolean success = mMultiSelectionState.toggleItemSelection(holder, position, _id);
        if (success) {
            updateActionModeTitle();
        }
        return success;
    }

    /**
     * Update the title of the action bar when action mode is turned on.
     */
    private void updateActionModeTitle() {
        if (mActionMode == null) return;
        int count = mMultiSelectionState.getSelectedItemCount();
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
                + holder.itemView.isActivated() + " " + mMultiSelectionState.isItemSelected(position));

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

    /**
     * Call this method when data for this adapter needs to be refreshed.
     * @param newCursor
     */
    public void swapCursor(Cursor newCursor) {
        Log.d(TAG, "swap1!");
        mCursor = newCursor;
        this.notifyDataSetChanged();
    }
}
