package com.threekings.stepventure.introduction;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.threekings.stepventure.R;

class IntroFragmentAdapter extends FragmentPagerAdapter{
    private Context context;
    private String[] introContent;
    private TypedArray introImages;

    private int mCount;

    public IntroFragmentAdapter(FragmentManager fm, Context nContext) {
        super(fm);
        context = nContext;
        introContent = context.getResources().getStringArray(R.array.introduction_texts);
        introImages = context.getResources().obtainTypedArray(R.array.introduction_images);
        mCount = introContent.length;
    }

    @Override
    public Fragment getItem(int position) {
        return IntroFragment.newInstance(position, mCount, introContent[position], introImages.getResourceId(position, -1));
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return introContent[position];
    }
}