package com.cachirulop.logmytrip.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.fragment.TabMapFragment;
import com.cachirulop.logmytrip.fragment.TabStatisticsFragment;

/**
 * Created by hp1 on 21-01-2015.
 */
public class TripDetailViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence[] _titles;
    Fragment[] _fragments;

    public TripDetailViewPagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());

        _titles = new CharSequence[2];
        _titles[0] = activity.getString(R.string.tab_title_map);
        _titles[1] = activity.getString(R.string.tab_title_statistics);

        _fragments = new Fragment[2];
        _fragments[0] = new TabMapFragment();
        _fragments[1] = new TabStatisticsFragment();
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        return _fragments[position];
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return _titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return _fragments.length;
    }
}