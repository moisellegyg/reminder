package com.yugegong.reminder;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yugegong.reminder.data.ProductContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProductListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = ProductListFragment.class.getSimpleName();
    private final static int LOADER_ID = 0;

    private RecyclerView mRecyclerView;
    private int mPosition = mRecyclerView.NO_POSITION;

    private ReminderAdapter mReminderAdapter;
    private RecyclerView.LayoutManager mLayoutManger;

    private static final String[] PRODUCT_COLUMNS = {
            ProductContract.ProductEntry._ID,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_UPC,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IMG_PATH,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_CREATE_DATE,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE
    };

    public static final int COL_PRODUCT_ID = 0;
    public static final int COL_PRODUCT_NAME = 1;
    public static final int COL_PRODUCT_UPC = 2;
    public static final int COL_PRODUCT_IMG_PATH = 3;
    public static final int COL_PRODUCT_CREATE_DATE = 4;
    public static final int COL_PRODUCT_EXPIRE_DATE = 5;

    public ProductListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // retain this fragment
    }

    public interface ProductListFragmentCallback {
        void onItemSelected(ReminderAdapter.ViewHolder vh, long _id);
    }

    public ReminderAdapter getReminderAdapter() {
        return mReminderAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_reminders);
        mRecyclerView.setHasFixedSize(true);
        mReminderAdapter = new ReminderAdapter(getContext(), new ReminderAdapter.ReminderAdapterOnClickHandler() {
            @Override
            public void onClick(ReminderAdapter.ViewHolder vh, long _id) {
                mPosition = vh.getAdapterPosition();
                Log.v(TAG, "onClick mPosition = " + mPosition + " " + _id);
                ((ProductListFragmentCallback)getActivity()).onItemSelected(vh, _id);
            }

        });
        mRecyclerView.setAdapter(mReminderAdapter);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManger = new LinearLayoutManager(getContext());
        } else {
            mLayoutManger = new GridLayoutManager(getContext(), 2);
        }

        mRecyclerView.setLayoutManager(mLayoutManger);

        /*
         * Initializes the CursorLoader. The LOADER_ID value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootView;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
    }
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        switch (id) {
            case LOADER_ID:
                Uri productUri = ProductContract.ProductEntry.CONTENT_URI;
                String sort_by = ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE + " ASC";
                return new CursorLoader(
                        getContext(),       // Parent activity context
                        productUri,         // Table to query
                        PRODUCT_COLUMNS,    // Projection to return
                        null,               // No selection clause
                        null,               // No selection arguments
                        sort_by             // sort by product expired date
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished " + data.getCount());
        mReminderAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");
        mReminderAdapter.swapCursor(null);
    }


}
