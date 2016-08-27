package com.yugegong.reminder;

import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by ygong on 8/23/16.
 */
public class ModalMultiChoiceCallback implements ActionMode.Callback {
    private static final String LOG_TAG = "ModalCallback";

    private MultiSelector mMultiSelector;

    public ModalMultiChoiceCallback(MultiSelector selector) {
        mMultiSelector = selector;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Log.d(LOG_TAG, "onCreateActionMode");
        mMultiSelector.setSelectable(true);
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
        mMultiSelector.clearSelections();
        mMultiSelector.setSelectable(false);
    }
}
