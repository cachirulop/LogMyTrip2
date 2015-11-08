package com.cachirulop.logmytrip.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.TabMapFragment;
import com.cachirulop.logmytrip.fragment.TabStatisticsFragment;
import com.cachirulop.logmytrip.util.LogHelper;

/**
 * Created by hp1 on 21-01-2015.
 */
public class TripDetailViewPagerAdapter
        extends FragmentStatePagerAdapter
{

    CharSequence[] _titles;
    Fragment[] _fragments;

    public TripDetailViewPagerAdapter (FragmentActivity activity, Trip trip)
    {
        super (activity.getSupportFragmentManager ());

        LogHelper.d ("*** TripDetailViewPagerAdapter constructor");

        _titles = new CharSequence[2];
        _titles[0] = activity.getString (R.string.title_map);
        _titles[1] = activity.getString (R.string.title_statistics);

        _fragments = new Fragment[2];
        _fragments[0] = TabMapFragment.newInstance (trip);
        _fragments[1] = TabStatisticsFragment.newInstance (trip);

        LogHelper.d ("*** TripDetailViewPagerAdapter constructor DONE");
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem (int position)
    {
        return _fragments[position];
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle (int position)
    {
        return _titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount ()
    {
        return _fragments.length;
    }

    public void setMapType (int mapType)
    {
        getMapFragment ().setMapType (mapType);
        getStatisticsFragment ().setMapType (mapType);
    }

    private TabMapFragment getMapFragment ()
    {
        return (TabMapFragment) _fragments[0];
    }

    private TabStatisticsFragment getStatisticsFragment ()
    {
        return (TabStatisticsFragment) _fragments[1];
    }
}