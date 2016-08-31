package com.yugegong.reminder;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by ygong on 8/4/16.
 */
public class ProductEditActivity extends AppCompatActivity {
    private static final String TAG = ProductEditActivity.class.getSimpleName();

    private static final String TAG_EDIT_FRAGMENT = "edit_fragment";
    private ProductEditFragment mEditFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        FragmentManager fm = getSupportFragmentManager();
        mEditFragment = (ProductEditFragment) fm.findFragmentByTag(TAG_EDIT_FRAGMENT);

        if (mEditFragment == null) {
            mEditFragment = new ProductEditFragment();
            Uri uri = getIntent().getData();
            if (uri != null) {
                Log.d(TAG, uri.toString());
                Bundle args = new Bundle();
                args.putParcelable(ProductEditFragment.PRODUCT_URI, uri);
                mEditFragment.setArguments(args);
            }
            fm.beginTransaction()
                    .add(R.id.edit_fragment_container, mEditFragment, TAG_EDIT_FRAGMENT)
                    .commit();
        }
    }


    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu_edit, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();
    }

}
