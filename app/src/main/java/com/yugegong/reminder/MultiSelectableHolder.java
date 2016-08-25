package com.yugegong.reminder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ygong on 8/24/16.
 */
public abstract class MultiSelectableHolder extends RecyclerView.ViewHolder {
    private final MultiSelector mMultiSelector;

    private boolean mIsSelectable = false;
    private long _id = -1;

    public MultiSelectableHolder(View itemView, MultiSelector selector) {
        super(itemView);
        mMultiSelector = selector;
    }


    public boolean isActivated() {
        return itemView.isActivated();
    }

    public void setActivated(boolean isActivated) {
        itemView.setActivated(isActivated);
    }

    public boolean isSelectable() {
        return mIsSelectable;
    }

    /**
     * When selected {@link #itemView}'s background drawable
     * @param isSelectable true if selected
     */
    public void setSelectable(boolean isSelectable) {
//        boolean changed = (isSelectable != mIsSelectable);
        mIsSelectable = isSelectable;
//        if (changed) {
//            refresh();
//        }
    }

    private void refresh() {

    }

    protected void onBindSelector(){
        mMultiSelector.bindHolder(this, getAdapterPosition());
    }
}
