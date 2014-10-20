package com.threekings.stepventure.introduction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;

import com.threekings.stepventure.MainActivity;
import com.threekings.stepventure.R;
import com.threekings.stepventure.adapter.TypefaceSpan;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class IntroductionActivity extends FragmentActivity {

    // Tag
    public static final String TAG = "com.threekings.stepventure.introduction.IntroductionActivity";

    // Fragment variables
    private IntroFragmentAdapter mAdapter;
    private ViewPager mPager;
    private PageIndicator mIndicator;

    // Settings
    private SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get SharedPreferences
        settings = getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);

        // Set circle layout
        setContentView(R.layout.simple_circles);

        // Create Adapter
        mAdapter = new IntroFragmentAdapter(getSupportFragmentManager(),getApplicationContext());

        // Set Adapter to pager
        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Bind indicator to pages
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        // Custom Actionbar Font
        setSpannableString(getResources().getString(R.string.app_name));
    }

    /** Create string with custom font */
    private void setSpannableString(CharSequence title){
        SpannableString s = new SpannableString(title);
        s.setSpan(new TypefaceSpan(this, "square.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(s);
    }

    /** Exit introduction */
    public void exitIntroduction() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.ID_SHOW_INTRODUCTION, false);
        editor.commit();

        Intent i = new Intent(IntroductionActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}