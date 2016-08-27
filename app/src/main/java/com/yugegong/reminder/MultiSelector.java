package com.yugegong.reminder;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseLongArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ygong on 8/22/16.
 */
public class MultiSelector {
    private static final String LOG_TAG = MultiSelector.class.getSimpleName();
    private static final String KEY_SELECTED_POSITIONS = "positions";
    private static final String KEY_SELECTOR_STATE = "state";


    /**
     * Running state of which positions are currently checked
     */
    private SparseBooleanArray mCheckStats = new SparseBooleanArray();
    private SparseLongArray mCheckIdStats = new SparseLongArray();
    private WeakHolderTracker mTracker = new WeakHolderTracker();
    private int mSelectedCount = 0;

    private boolean mIsSelectable;

    public interface MultiSelectorListener {
        Bundle saveMultiSelectorStats();
        void restoreMultiSelectorStats(Bundle savedStats);
    }


    public boolean isSelectable() {
        return mIsSelectable;
    }

    public void setSelectable(boolean isSelectable) {
        mIsSelectable = isSelectable;
        refreshAllHolders();
    }

    public boolean isItemSelected(int position) {
        return mCheckStats.get(position, false);
    }

    public void setItemSelected(MultiSelectableHolder holder, int position, long _id, boolean isSelected) {
        mCheckStats.put(position, isSelected);
        mCheckIdStats.put(position, _id);
        mSelectedCount += isSelected ? 1 : -1;
        refreshHolder(holder);
    }

    public boolean toggleItemSelection(MultiSelectableHolder holder, int position, long _id) {
        if (mIsSelectable) {
            boolean isSelected = isItemSelected(position);
            Log.d(LOG_TAG, position + "/" + _id + " isSelected = " + isSelected);
            setItemSelected(holder, position, _id, !isSelected);
            return true;
        }
        return false;
    }

    public long getItemId(int position) {
        return mCheckIdStats.get(position, -1);
    }

    private long getHolderId(int position) {
        MultiSelectableHolder holder = mTracker.getHolder(position);
        return holder != null ? holder.getItemId() : -1;
    }

    public void clearSelections() {
        mCheckStats.clear();
        mCheckIdStats.clear();
        mSelectedCount = 0;
        refreshAllHolders();
    }

    public int getSelectedItemCount() {
        return mSelectedCount;
    }

    public void bindHolder(MultiSelectableHolder holder, int position, long _id) {
        Log.d(LOG_TAG, "Bind holder " + position + " _id = " + _id);
        mTracker.bindHolder(position, holder);
        refreshHolder(holder);
    }

    private void refreshAllHolders() {
        List<MultiSelectableHolder> holders = mTracker.getHolders();
        Log.d(LOG_TAG, "refresh all " + holders.size());
        for (MultiSelectableHolder holder : holders) {
            refreshHolder(holder);
        }
    }

    private void refreshHolder(MultiSelectableHolder holder) {
        if (holder == null) return;
        holder.setSelectable(mIsSelectable);
        boolean isSelected = mCheckStats.get(holder.getAdapterPosition(), false);
        Log.d(LOG_TAG, "refresh " + holder.getAdapterPosition() + " isSelected = " + isSelected + " " + mCheckStats.size());
        holder.setActivated(isSelected);
    }

    public Bundle saveSelectionStats() {
        Log.d(LOG_TAG, "saveSelectionStats");
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(KEY_SELECTED_POSITIONS,
                (ArrayList<Integer>)getSelectedPositions());
        bundle.putBoolean(KEY_SELECTOR_STATE, mIsSelectable);
        return bundle;
    }

    public void restoreSelectionStats(Bundle savedStats) {
        Log.d(LOG_TAG, "restoreSelectionStats");
        if (savedStats == null) return;
        mIsSelectable = savedStats.getBoolean(KEY_SELECTOR_STATE);
        restoreSelectedPositions(savedStats.getIntegerArrayList(KEY_SELECTED_POSITIONS));
    }

    private void restoreSelectedPositions(List<Integer> positions) {
        if (positions == null) return;
        mCheckStats.clear();
        for (int position : positions) {
            mCheckStats.put(position, true);
            long _id = getHolderId(position);
            Log.d(LOG_TAG, "restore _id = " + _id);
            mCheckIdStats.put(position, _id);
        }
        mSelectedCount = positions.size();
        refreshAllHolders();
    }

    private List<Integer> getSelectedPositions() {
        List<Integer> positions = new ArrayList<>();
        int n = mCheckStats.size();
        for (int i = 0; i < n; i++) {
            if (mCheckStats.valueAt(i)) {
                Log.d(LOG_TAG, "getSelectedPositions " + i);
                positions.add(mCheckStats.keyAt(i));
            }
        }
        return positions;
    }

    private static class WeakHolderTracker {
        private SparseArray<WeakReference<MultiSelectableHolder>> mHolders = new SparseArray<>();

        void bindHolder(int position, MultiSelectableHolder holder) {
            mHolders.put(position, new WeakReference<>(holder));
        }

        MultiSelectableHolder getHolder(int position) {
            WeakReference<MultiSelectableHolder> weakRef = mHolders.get(position);
            if (weakRef == null) return null;

            MultiSelectableHolder holder = weakRef.get();
            if (holder == null || holder.getAdapterPosition() != position) {
                mHolders.delete(position);
                return null;
            }
            return holder;
        }

        List<MultiSelectableHolder> getHolders() {
            int n = mHolders.size();
            List<MultiSelectableHolder> list = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                int position = mHolders.keyAt(i);
                MultiSelectableHolder holder = getHolder(position);
                if (holder != null) {
                    list.add(holder);
                }
            }
            return list;

        }
    }


}
