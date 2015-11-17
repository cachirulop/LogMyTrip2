package com.cachirulop.logmytrip.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedTripHolder;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.util.LogHelper;
import com.cachirulop.logmytrip.util.MapHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class TabMapFragment
        extends Fragment
        implements OnMapReadyCallback
{
    private GoogleMap _map;
    private MapHelper _mapHelper;
    private Trip      _trip;

    private BroadcastReceiver _onNewLocationReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            Location     l;
            TripLocation tl;

            l = LogMyTripBroadcastManager.getLocation (intent);
            tl = new TripLocation (_trip, l);

            _trip.addLocation (tl);

            _map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (l.getLatitude (),
                                                                               l.getLongitude ()),
                                                                   18));

            drawTrackMainThread ();
        }
    };

    private BroadcastReceiver _onProviderEnabledChange = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (LogMyTripBroadcastManager.hasProviderEnable (intent)) {
                boolean enabled;

                enabled = LogMyTripBroadcastManager.getProviderEnable (intent);

                LogHelper.d ("ProviderEnabled change");
                if (!enabled) {
                    Snackbar msg;

                    msg = Snackbar.make (getView (),
                                         R.string.msg_gps_disabled,
                                         Snackbar.LENGTH_LONG);
                    msg.setAction (R.string.msg_configure_gps, new View.OnClickListener ()
                    {
                        @Override
                        public void onClick (View v)
                        {
                            startActivity (new Intent (Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });

                    msg.show ();
                }
            }
        }
    };
    private BroadcastReceiver _onProviderStatusChange  = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            ViewStub v;

            v = (ViewStub) getActivity ().findViewById (R.id.vsGPSStatusStub);
            v.inflate ();
        }
    };

    public TabMapFragment ()
    {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView ()
    {
        super.onDestroyView ();
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
        // Inflate the layout for this fragment
        return inflater.inflate (R.layout.fragment_tab_map, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        setUpMap ();
    }

    private void setUpMap ()
    {
        FragmentManager fm;

        fm = getFragmentManager ();

        ((SupportMapFragment) getChildFragmentManager ().findFragmentById (R.id.gmTripDetail)).getMapAsync (
                this);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated (savedInstanceState);
    }

    @Override
    public void onResume ()
    {
        super.onResume ();

        LogMyTripBroadcastManager.registerNewLocationReceiver (getContext (),
                                                               _onNewLocationReceiver);
        LogMyTripBroadcastManager.registerProviderEnableChange (getContext (),
                                                                _onProviderEnabledChange);
        LogMyTripBroadcastManager.registerStatusChange (getContext (), _onProviderStatusChange);
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onProviderEnabledChange);
        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onProviderStatusChange);
    }

    private void drawTrackMainThread ()
    {
        Handler  main;
        Runnable runInMain;
        Activity parent;

        parent = getActivity ();
        if (parent != null) {
            main = new Handler (getActivity ().getMainLooper ());

            runInMain = new Runnable ()
            {
                @Override
                public void run ()
                {
                    _mapHelper.drawTrip (_trip, true);
                }
            };

            main.post (runInMain);
        }
    }

    @Override
    public void onMapReady (GoogleMap googleMap)
    {
        Trip    activeTrip;
        boolean isActiveTrip;

        activeTrip = TripManager.getActiveTrip (getContext ());
        isActiveTrip = (_trip.equals (activeTrip));

        _map = googleMap;
        _map.setMyLocationEnabled (true);

        _mapHelper = new MapHelper (getContext ());
        _mapHelper.setMap (_map);
        _mapHelper.drawTrip (_trip, isActiveTrip);
    }

    public void setMapType (int mapType)
    {
        _map.setMapType (mapType);
    }
}
