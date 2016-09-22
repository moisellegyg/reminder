package com.yugegong.reminder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.yugegong.reminder.data.ProductContract;

public class MainActivity extends AppCompatActivity implements ProductListFragment.ProductListFragmentCallback {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 3;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        mPager.setAdapter(mPagerAdapter);

        mTabLayout.setupWithViewPager(mPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProductEditActivity.class);
                intent.putExtra(ProductEditFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION, true);
                startActivity(intent);
            }
        });

        Utils.setMetrics(getResources());
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "onDestroy");
        super.onDestroy();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onItemSelected(ReminderAdapter.ViewHolder vh) {
        long _id = vh.getItemId();
        Uri productUri = ProductContract.ProductEntry.buildProductUri(_id);
        Intent editIntent = new Intent(this, ProductEditActivity.class);
        editIntent.putExtra(ProductEditFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION, false);
        editIntent.setData(productUri);

        String transitionName = getString(R.string.transition_product_img);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        vh.mImageView,   // The view which starts the transition
                        transitionName    // The transitionName of the view we’re transitioning to
                );
        ActivityCompat.startActivity(this, editIntent, options.toBundle());
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("PagerAdapter", "getItem " + position);
            ProductListFragment.DataLoadType type = null;
            switch (position) {
                case 0:
                    type = ProductListFragment.DataLoadType.DATA_LOAD_FRESH;
                    break;
                case 1:
                    type = ProductListFragment.DataLoadType.DATA_LOAD_USED;
                    break;
                case 2:
                    type = ProductListFragment.DataLoadType.DATA_LOAD_EXPIRED;
                    break;
                default:
                    break;
            }
            Bundle arg = new Bundle();
            arg.putSerializable(ProductListFragment.KEY_DATA_LOAD_TYPE, type);
            Fragment fragment = new ProductListFragment();
            fragment.setArguments(arg);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_fresh);
                case 1:
                    return getString(R.string.tab_used);
                case 2:
                    return getString(R.string.tab_expired);
                default:
                    return "error";
            }
        }
    }
}
