package com.yugegong.reminder;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by ygong on 8/4/16.
 */
public class ProductEditActivity extends AppCompatActivity {
    private static final String TAG_EDIT_FRAGMENT = "edit_fragment";
    private ProductEditFragment mEditFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        FragmentManager fm = getSupportFragmentManager();
        mEditFragment = (ProductEditFragment) fm.findFragmentByTag(TAG_EDIT_FRAGMENT);
        if (mEditFragment == null) {
            mEditFragment = new ProductEditFragment();
            Uri uri = getIntent().getData();
            if (uri != null) {
                Log.d("ProductEditActivity", uri.toString());
                Bundle args = new Bundle();
                args.putParcelable(ProductEditFragment.PRODUCT_URI, uri);
                mEditFragment.setArguments(args);
            }
            fm.beginTransaction()
                    .add(R.id.edit_fragment_container, mEditFragment, TAG_EDIT_FRAGMENT)
                    .commit();
        }
    }

}
