package com.cachirulop.logmytrip.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.JourneyDetailViewPagerAdapter;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.manager.SelectedJourneyHolder;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by david on 14/09/15.
 */
public class JourneyDetailFragment
        extends Fragment
{
    private ViewPager                     _vpDetailPager;
    private JourneyDetailViewPagerAdapter _adapter;
    private int                           _mapType;
    private Toolbar                       _toolbar;
    private TabLayout                     _tabs;

    public JourneyDetailFragment ()
    {
        _mapType = GoogleMap.MAP_TYPE_NORMAL;
    }

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_journey_detail, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        ActionBar         ab;
        AppCompatActivity app;
        Journey journey;

        journey = SelectedJourneyHolder.getInstance ().getSelectedJourney ();
        app = (AppCompatActivity) getActivity ();

        // Toolbar and back button
        _toolbar = (Toolbar) view.findViewById (R.id.journey_detail_toolbar);
        setToolbarTitle (journey.getTitle ());

        app.setSupportActionBar (_toolbar);
        ab = app.getSupportActionBar ();
        ab.setDisplayHomeAsUpEnabled (true);

        // Adapter
        _adapter = new JourneyDetailViewPagerAdapter (getActivity (), journey);

        // ViewPager
        _vpDetailPager = (ViewPager) view.findViewById (R.id.vpDetailPager);
        _vpDetailPager.setAdapter (_adapter);

        // Tabs
        _tabs = (TabLayout) view.findViewById (R.id.journey_detail_tablayout);
        _tabs.setupWithViewPager (_vpDetailPager);
    }

    public void setToolbarTitle (String title)
    {
        _toolbar.setTitle (title);
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
