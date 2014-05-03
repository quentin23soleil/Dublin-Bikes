package com.quentindommerc.dublinbikes.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.quentindommerc.dublinbikes.R;
import com.quentindommerc.dublinbikes.adapter.DrawerAdapter;
import com.quentindommerc.dublinbikes.base.BaseActivity;
import com.quentindommerc.dublinbikes.base.BaseFragment;
import com.quentindommerc.dublinbikes.bean.MenuItem;
import com.quentindommerc.dublinbikes.fragments.AboutFragment;
import com.quentindommerc.dublinbikes.fragments.BookmarkFragment;
import com.quentindommerc.dublinbikes.fragments.ListFragment;
import com.quentindommerc.dublinbikes.fragments.MapFragment;
import com.quentindommerc.dublinbikes.utils.Constants;
import com.quentindommerc.dublinbikes.utils.Utils;

import fr.nicolaspomepuy.discreetapprate.AppRate;
import fr.nicolaspomepuy.discreetapprate.RetryPolicy;

public class Home extends BaseActivity implements FragmentManager.OnBackStackChangedListener {

    private DrawerLayout mDrawer;
    private ListView mList;
    private ActionBarDrawerToggle drawerToggle;
    private int oldSelection;
    protected boolean opened;
    private SharedPreferences prefs;
    private DrawerAdapter mAdapter;
    private BaseFragment mFrag;
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        EasyTracker.getInstance(this).activityStart(this);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        EasyTracker.getInstance(this).activityStop(this);
//    }
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppRate.with(this).initialLaunchCount(3).retryPolicy(RetryPolicy.INCREMENTAL)
                .checkAndShow();


        Utils.playServicesCheck(this);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mList = (ListView) findViewById(R.id.left_drawer);

        oldSelection = -1;
        mAdapter = new DrawerAdapter(this);
        mAdapter.add(new MenuItem(getString(R.string.station_list), R.drawable.ic_list));
        mAdapter.add(new MenuItem(getString(R.string.show_me_map), R.drawable.ic_map));
        mAdapter.add(new MenuItem(getString(R.string.my_fav), R.drawable.ic_bookmark));
        mAdapter.add(new MenuItem(getString(R.string.about), R.drawable.ic_about));
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                switch (arg2) {
                    case DrawerAdapter.MENU_LIST:
                        mFrag = ListFragment.newInstance(null, Constants.LIST_MODE);
                        break;
                    case DrawerAdapter.MENU_MAP:
                        mFrag = MapFragment.newInstance(-1);
                        break;
                    case DrawerAdapter.MENU_BOOKMARK:
                        mFrag = BookmarkFragment.newInstance();
                        break;
                    case DrawerAdapter.MENU_ABOUT:
                        mFrag = AboutFragment.newInstance();
                        break;
                }

                if (arg2 != oldSelection) {
                    oldSelection = arg2;
                    replaceFrag(mFrag);
                }
            }
        });
        drawerToggle = new ActionBarDrawerToggle(Home.this, mDrawer, R.drawable.ic_drawer,
                R.string.open, R.string.close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (!opened) {
                    opened = true;
                    if (prefs != null) {
                        Editor editor = prefs.edit();
                        editor.putBoolean("opened", true);
                        editor.apply();
                    }
                }
                getActionBar().setTitle(mFrag.getTitle());
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.app_name);
            }
        };

        mDrawer.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(android.R.color.transparent);

        prefs = getPreferences(MODE_PRIVATE);
        opened = prefs.getBoolean("opened", false);
        if (!opened) {
            mDrawer.openDrawer(mList);
        }

        mList.performItemClick(mList, 0, 0);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

    }

    private void replaceFrag(BaseFragment frag) {

        android.support.v4.app.FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_frame, frag);
        tx.commit();
        mDrawer.closeDrawer(mList);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1)
            finish();
        else
            super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        mFrag  = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (mFrag != null){
            updateTitleAndDrawer(mFrag);
        }
    }

    private void updateTitleAndDrawer(BaseFragment f) {
        if (f instanceof ListFragment) {
            setTitle(getString(R.string.station_list));
            mList.setItemChecked(DrawerAdapter.MENU_LIST, true);
        } else if (f instanceof MapFragment) {
            setTitle(getString(R.string.show_me_map));
            mList.setItemChecked(DrawerAdapter.MENU_MAP, true);
        } else if (f instanceof BookmarkFragment) {
            setTitle(getString(R.string.add_to_bookmark));
            mList.setItemChecked(DrawerAdapter.MENU_BOOKMARK, true);
        } else {
            setTitle(getString(R.string.about));
            mList.setItemChecked(DrawerAdapter.MENU_ABOUT, true);
        }

    }
}
