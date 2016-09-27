package com.yugegong.reminder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ygong on 8/24/16.
 */
public abstract class MultiSelectableViewHolder extends RecyclerView.ViewHolder {
    private final MultiSelectionState mMultiSelectionState;

    private boolean mIsSelectable = false;

    public MultiSelectableViewHolder(View itemView, MultiSelectionState selector) {
        super(itemView);
        mMultiSelectionState = selector;
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
        mMultiSelectionState.bindHolder(this, getAdapterPosition(), getItemId());
    }
}
