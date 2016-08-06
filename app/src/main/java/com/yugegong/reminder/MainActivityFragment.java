package com.yugegong.reminder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final static String TAG = MainActivityFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ReminderAdapter mReminderAdapter;
    private RecyclerView.LayoutManager mLayoutManger;
    private static String[] GOODS = {"Apple", "Pear", "Milk", "Soda"};

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, rootView.getTag().toString());
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_reminders);
        mReminderAdapter = new ReminderAdapter(GOODS);
        mRecyclerView.setAdapter(mReminderAdapter);
        mLayoutManger = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManger);


        return rootView;
    }
}
