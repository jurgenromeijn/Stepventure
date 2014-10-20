package com.threekings.stepventure;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.threekings.stepventure.adapter.NavDrawerListAdapter;
import com.threekings.stepventure.adapter.TypefaceSpan;
import com.threekings.stepventure.model.NavDrawerItem;
import com.threekings.stepventure.pedometer.StepService;

import java.util.ArrayList;


public class MainActivity extends Activity{

    // Tag
    public static final String TAG = "com.threekings.stepventure.MainActivity";

    // Nav drawer
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;  // Nav drawer title
    private CharSequence mTitle;        // used to store app title

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    // Custom ListAdapter Array
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    // Service
    private int stepCount;
    public static LocalBroadcastManager mBroadcaster;

    // Settings
    private SharedPreferences settings;
    private boolean showIntroduction;
    public static final String ID_SETTINGS = "gameSettings";
    public static final String ID_STEPCOUNT = "stepCount";
    public static final String ID_NEXTFIGHT = "nextFight";
    public static final String ID_BROADCAST_RECEIVER_ACTIVE = "broadcastReceiverActive";
    public static final String ID_SHOW_INTRODUCTION = "showIntroduction";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "--- CREATE");

        // Settings
        settings = getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);
        showIntroduction = settings.getBoolean(MainActivity.ID_SHOW_INTRODUCTION, true);

        // First Start
        Intent i;
        if (showIntroduction){
            // Show Splash
            i = new Intent(MainActivity.this, SplashScreen.class);
            startActivity(i);
            finish();
        } else {
            // Set Layout
            setContentView(R.layout.activity_main);

            mTitle = mDrawerTitle = getTitle();
            mBroadcaster = LocalBroadcastManager.getInstance(this);

            // load slide menu items
            navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

            // nav drawer icons from resources
            navMenuIcons = getResources()
                    .obtainTypedArray(R.array.nav_drawer_icons);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
            navDrawerItems = new ArrayList<NavDrawerItem>();

            // adding nav drawer items to array
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));     // Dashboard
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));     // Stats
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));     // Battles
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));     // About

            // Recycle the typed array
            navMenuIcons.recycle();

            mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

            // setting the nav drawer list adapter
            adapter = new NavDrawerListAdapter(getBaseContext(),
                    navDrawerItems);
            mDrawerList.setAdapter(adapter);

            // enabling action bar app icon and behaving it as toggle button
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            //nav menu toggle icon
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.drawer_open, // nav drawer open - description for accessibility
                    R.string.drawer_close // nav drawer close - description for accessibility
            ) {
                public void onDrawerClosed(View view) {
                    setSpannableString(mTitle);
                    // calling onPrepareOptionsMenu() to show action bar icons
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    setSpannableString(mDrawerTitle);
                    // calling onPrepareOptionsMenu() to hide action bar icons
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (savedInstanceState == null) {
                // on first time display view for first nav item
                displayView(0);
            }

            startBattle(getIntent());
        }
    }

    private void startBattle(Intent intent) {
        if (intent.getAction() != null){
            if(intent.getAction().equals("OPEN_BATTLES")) {
                Fragment fragment = new BattlesFragment();
                Bundle args = new Bundle();
                args.putBoolean("index", true);
                fragment.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment).commit();
                setTitle(navMenuTitles[2]);
            }
        }
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Implement MainActivity BroadcastReceiver functionality (And uncomment register functions)
        }
    };

    public void startPedometer(){
        //Set up pedometer
        if (!isMyServiceRunning()){
            Log.i(TAG, "--- START StepService");
            startService(new Intent(this, StepService.class));

            mBroadcaster.registerReceiver(messageReceiver, new IntentFilter(StepService.ID_COMMUNICATION_INTENT_STEP));
        }
    }

    public boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StepService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopPedometer(){
        if (isMyServiceRunning()){
            Log.i(TAG, "--- STOP StepService");

            stopService(new Intent(this, StepService.class));

            // mBroadcaster.unregisterReceiver(messageReceiver);
        }
    }

    /** State Methods */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "--- START");
        EasyTracker.getInstance(this).activityStart(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "--- RESUME");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "--- PAUSE");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "--- STOP");
        EasyTracker.getInstance(this).activityStop(this);
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "--- RESTART");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "--- DESTROY");
    }
    @Override
    protected void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);
        Log.i(TAG, "--- NEW INTENT");
        startBattle(newIntent);
    }

    /** Create string with custom font */
    private void setSpannableString(CharSequence title){
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan(this, "square.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(s);
    }

    /** Set Actionbar title */
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        setSpannableString(mTitle);
    }

    /** Slide menu item click listener */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // Normal typeface for all menu items
            for (int i = 0; i < parent.getCount(); i++) {
                TextView tv = (TextView)parent.getChildAt(i).findViewById(R.id.title);
                if (tv != null)
                    tv.setTypeface(Typeface.create("sans-serif-thin", 0));
            }

            // Bold typeface for selected menu item
            TextView tv = (TextView) view.findViewById(R.id.title);
            tv.setTypeface(Typeface.create("sans-serif",1));

            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /** Options menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
//            TODO: Implement Settings Activity
//            case R.id.action_settings:
//                return true;
            case R.id.action_exit:
                finish();   // Exit, but stay in background
                //System.exit(0);   // Full exit (no saved state)
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when invalidateOptionsMenu() is triggered */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        TODO: Implement Settings Activity
//        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /** Displaying fragment view for selected nav drawer list item */
    private void displayView(int position) {
        Boolean index = false;
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new DashboardFragment();
                break;
            case 1:
                fragment = new StatsFragment();
                break;
            case 2:
                fragment = new BattlesFragment();
                // Supply index input as an argument.
                Bundle args = new Bundle();
                args.putBoolean("index", index);
                fragment.setArguments(args);
                break;
            case 3:
                fragment = new AboutFragment();
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            if(position == 0){
                setTitle(R.string.app_name);
            } else {
                setTitle(navMenuTitles[position]);
            }

            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            // error in creating fragment
            Log.e(TAG, "Error in creating fragment");
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
