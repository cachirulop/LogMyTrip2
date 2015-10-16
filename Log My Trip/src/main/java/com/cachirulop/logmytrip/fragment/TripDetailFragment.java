package com.cachirulop.logmytrip.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripDetailViewPagerAdapter;
import com.cachirulop.logmytrip.entity.Trip;

/**
 * Created by david on 14/09/15.
 */
public class TripDetailFragment
        extends Fragment
{
    FloatingActionButton _fabDetailChangeView;
    ViewPager            _vpDetailPager;

    public TripDetailFragment ()
    {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_trip_detail, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        Toolbar           toolbar;
        ActionBar         ab;
        AppCompatActivity app;

        app = (AppCompatActivity) getActivity ();

        // Toolbar and back button
        toolbar = (Toolbar) view.findViewById (R.id.trip_detail_toolbar);
        app.setSupportActionBar (toolbar);
        ab = app.getSupportActionBar ();
        ab.setDisplayHomeAsUpEnabled (true);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        TripDetailViewPagerAdapter adapter;

        adapter = new TripDetailViewPagerAdapter (getActivity (),
                                                  (Trip) getArguments ().getSerializable (
                                                          MainFragment.ARG_PARAM_TRIP));

        // ViewPager
        _vpDetailPager = (ViewPager) view.findViewById (R.id.vpDetailPager);
        _vpDetailPager.setAdapter (adapter);


        // Change-page button
        _fabDetailChangeView = (FloatingActionButton) view.findViewById (R.id.fabDetailChangeView);
        _fabDetailChangeView.setOnClickListener (new View.OnClickListener ()
        {
            @Override
            public void onClick (View v)
            {
                if (_vpDetailPager.getCurrentItem () == 0) {
                    _vpDetailPager.setCurrentItem (1);
                    _fabDetailChangeView.setImageResource (R.mipmap.ic_button_map);
                }
                else {
                    _vpDetailPager.setCurrentItem (0);
                    _fabDetailChangeView.setImageResource (R.mipmap.ic_button_statistics);
                }
            }
        });

    }

}
