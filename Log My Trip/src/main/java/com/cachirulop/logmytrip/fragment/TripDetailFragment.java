package com.cachirulop.logmytrip.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripDetailViewPagerAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.view.SlidingTabLayout;

/**
 * Created by david on 14/09/15.
 */
public class TripDetailFragment
        extends Fragment {

    public TripDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_detail,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        TripDetailViewPagerAdapter adapter;

        adapter = new TripDetailViewPagerAdapter(getActivity(), (Trip) getArguments().getSerializable(TabMapFragment.ARG_PARAM_TRIP));

        // Assigning ViewPager View and setting the adapter
        ViewPager pager;

        pager = (ViewPager) getView().findViewById(R.id.vpTripDetail);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        SlidingTabLayout tabs;

        tabs = (SlidingTabLayout) getView().findViewById(R.id.stlTripDetail);
        tabs.setDistributeEvenly(true);

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tab_selected);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }

}
