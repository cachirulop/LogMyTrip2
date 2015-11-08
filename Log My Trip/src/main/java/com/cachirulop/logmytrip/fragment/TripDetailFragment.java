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
import com.cachirulop.logmytrip.manager.TripManager;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by david on 14/09/15.
 */
public class TripDetailFragment
        extends Fragment
{
    private FloatingActionButton       _fabDetailChangeView;
    private ViewPager                  _vpDetailPager;
    private TripDetailViewPagerAdapter _adapter;
    private int                        _mapType;

    public TripDetailFragment ()
    {
        _mapType = GoogleMap.MAP_TYPE_NORMAL;
    }

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
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
        Trip trip;
        long tripId;

        tripId = getArguments ().getLong (MainFragment.ARG_PARAM_TRIP_ID);
        trip = TripManager.getInstance ().getTrip (tripId);
        app = (AppCompatActivity) getActivity ();

        // Toolbar and back button
        toolbar = (Toolbar) view.findViewById (R.id.trip_detail_toolbar);
        toolbar.setTitle (trip.getTitle ());

        app.setSupportActionBar (toolbar);
        ab = app.getSupportActionBar ();
        ab.setDisplayHomeAsUpEnabled (true);

        // Adapter
        _adapter = new TripDetailViewPagerAdapter (getActivity (), trip);

        // ViewPager
        _vpDetailPager = (ViewPager) view.findViewById (R.id.vpDetailPager);
        _vpDetailPager.setAdapter (_adapter);


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

    public int getMapType ()
    {
        return _mapType;
    }

    public void setMapType (int type)
    {
        _mapType = type;
        _adapter.setMapType (type);
    }
}
