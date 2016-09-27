package com.yugegong.reminder;

import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Callback class that implements callback methods for action mode. After long pressing the view,
 * action mode is invoked, callback methods will be called and {@link MultiSelectionState} will be in
 * selectable mode as well.
 */
public class ModalMultiChoiceCallback implements ActionMode.Callback {
    private static final String LOG_TAG = "ModalCallback";

    private MultiSelectionState mMultiSelectionState;

    public ModalMultiChoiceCallback(MultiSelectionState selector) {
        mMultiSelectionState = selector;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(LOG_TAG, "onCreateActionMode");
        mMultiSelectionState.setSelectable(true);
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(LOG_TAG, "onDestroyActionMode");
        mMultiSelectionState.clearSelections();
        mMultiSelectionState.setSelectable(false);
    }
}
