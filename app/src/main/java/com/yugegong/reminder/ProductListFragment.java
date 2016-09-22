package com.yugegong.reminder;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public interface ProductListFragmentCallback {
        void onItemSelected(ReminderAdapter.ViewHolder vh);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_reminders);
        mRecyclerView.setHasFixedSize(true);
        mReminderAdapter = new ReminderAdapter(getContext(), new ReminderAdapter.OnItemClickedListener() {
            @Override
            public void onItemSelected(ReminderAdapter.ViewHolder vh) {
                mPosition = vh.getAdapterPosition();
                Log.v(TAG, "onClick mPosition = " + mPosition);
                ((ProductListFragmentCallback)getActivity()).onItemSelected(vh);
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
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(TAG);
            mReminderAdapter.restoreMultiSelectorStats(bundle);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        Bundle bundle = mReminderAdapter.saveMultiSelectorStats();
        if (bundle != null) {
            outState.putBundle(TAG, bundle);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
