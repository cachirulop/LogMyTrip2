package com.cachirulop.logmytrip.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedTripHolder;


public class TabStatisticsFragment
        extends Fragment
{
    private RecyclerView          _recyclerView;
    private TripStatisticsAdapter _adapter;
    private Trip                  _trip;
    private Context               _ctx;

    private BroadcastReceiver _onNewLocationReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            _adapter.notifyDataSetChanged ();
        }
    };

    public TabStatisticsFragment ()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TabStatisticsFragment.
     */
    public static TabStatisticsFragment newInstance (Trip trip)
    {
        return new TabStatisticsFragment ();
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);

        _trip = SelectedTripHolder.getInstance ().getSelectedTrip ();
    }

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_tab_statistics, container, false);
    }

    @Override
    public void onResume ()
    {
        super.onResume ();

        LogMyTripBroadcastManager.registerNewLocationReceiver (getContext (),
                                                               _onNewLocationReceiver);
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        _ctx = getActivity ();

        _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvSegments);
        _recyclerView.setLayoutManager (new LinearLayoutManager (_ctx));
        _recyclerView.setHasFixedSize (true);

        _recyclerView.setItemAnimator (new DefaultItemAnimator ());

        _adapter = new TripStatisticsAdapter (_ctx, this, _trip);
        _recyclerView.setAdapter (_adapter);
    }

    public void setMapType (int mapType)
    {
        _adapter.setMapType (mapType);
    }

}
