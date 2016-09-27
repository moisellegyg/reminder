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

    private final static String KEY_ADAPTER_BUNDLE = "adapter_bundle";

    /**
     * Type of product the adapter will load. There are three types of product in total:<br>
     * {@link DataLoadType#DATA_LOAD_FRESH} - Products haven't been used and aren't expired yet.<br>
     * {@link DataLoadType#DATA_LOAD_USED} - <br>
     * {@link DataLoadType#DATA_LOAD_EXPIRED}.
     */
    public enum DataLoadType {
        DATA_LOAD_FRESH, DATA_LOAD_USED, DATA_LOAD_EXPIRED
    }
    public static final String KEY_DATA_LOAD_TYPE = "load_type";
    private DataLoadType mDataLoadType;

    private final static int LOADER_ID = 0;
    private final static String VALUE_IS_NOT_USED = "0";
    private final static String VALUE_IS_USED = "1";

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
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE,
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED
    };

    public static final int COL_PRODUCT_ID = 0;
    public static final int COL_PRODUCT_NAME = 1;
    public static final int COL_PRODUCT_UPC = 2;
    public static final int COL_PRODUCT_IMG_PATH = 3;
    public static final int COL_PRODUCT_CREATE_DATE = 4;
    public static final int COL_PRODUCT_EXPIRE_DATE = 5;
    public static final int COL_PRODUCT_IS_USED = 6;

    private static final String SELECTION_IF_USED =
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED + " = ?";
    private static final String SELECTION_FRESH_IF_USED =
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE + " >= ? AND "
                    + SELECTION_IF_USED;
    private static final String SELECTION_EXPIRED_IF_USED =
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE + " < ? AND "
                    + SELECTION_IF_USED;
    private static final String SORT_BY_EXPIRED_DATE_ASC =
            ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE + " ASC";


    public ProductListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public interface ProductViewProvider {
        /**
         *
         * @param _id product id
         * @param imageView {@link ProductImageView} that holds the image for the related product
         */
        void openProductView(long _id, ProductImageView imageView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_reminders);
        mRecyclerView.setHasFixedSize(true);
        mReminderAdapter = new ReminderAdapter(getContext(), new ReminderAdapter.OnItemClickCallback() {
            @Override
            public void onItemClicked(ReminderAdapter.ViewHolder vh) {
//                mPosition = vh.getAdapterPosition();
//                Log.v(TAG, "onClick mPosition = " + mPosition);
                ((ProductViewProvider)getActivity()).openProductView(vh.getItemId(), vh.mImageView);
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
        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(KEY_ADAPTER_BUNDLE);
            mReminderAdapter.restoreAdapterState(bundle);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        Bundle bundle = mReminderAdapter.saveAdapterState();
        if (bundle != null) {
            outState.putBundle(KEY_ADAPTER_BUNDLE, bundle);
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
        mDataLoadType = (DataLoadType) args.getSerializable(KEY_DATA_LOAD_TYPE);
        Log.d(TAG, "onCreateLoader " + mDataLoadType.name());

        // get today's time in milliseconds
        long timestamp = Utils.getTodayTimeInMillis();

        if (id == LOADER_ID) {
            String selection = null;
            String[] selectionArgs = null;
            switch (mDataLoadType) {
                case DATA_LOAD_FRESH: {
                    selection = SELECTION_FRESH_IF_USED;
                    selectionArgs = new String[]{Long.toString(timestamp), VALUE_IS_NOT_USED};
                    break;
                }
                case DATA_LOAD_USED: {
                    selection = SELECTION_IF_USED;
                    selectionArgs = new String[]{VALUE_IS_USED};
                    break;
                }
                case DATA_LOAD_EXPIRED: {
                    selection = SELECTION_EXPIRED_IF_USED;
                    selectionArgs = new String[]{Long.toString(timestamp), VALUE_IS_NOT_USED};
                    break;
                }
                default:
                    break;
            }

            Uri productUri = ProductContract.ProductEntry.CONTENT_URI;
            String sort_by = SORT_BY_EXPIRED_DATE_ASC;
            return new CursorLoader(
                    getContext(),       // Parent activity context
                    productUri,         // Table to query
                    PRODUCT_COLUMNS,    // Projection to return
                    selection,               // No selection clause
                    selectionArgs,               // No selection arguments
                    sort_by             // sort by product expired date
            );
        }
        return null;
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
