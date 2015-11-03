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
import com.cachirulop.logmytrip.manager.LocationBroadcastManager;
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
            Location l;

            l = LocationBroadcastManager.getLocation (intent);

            _map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (l.getLatitude (),
                                                                               l.getLongitude ()),
                                                                   17));

            drawTrackMainThread ();
        }
    };

    private BroadcastReceiver _onProviderEnabledChange = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (LocationBroadcastManager.hasProviderEnable (intent)) {
                boolean enabled;

                enabled = LocationBroadcastManager.getProviderEnable (intent);

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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip Trip to show the map.
     * @return A new instance of fragment TabMapFragment.
     */
    public static TabMapFragment newInstance (Trip trip)
    {
        TabMapFragment fragment;
        Bundle         args;

        args = new Bundle ();
        args.putSerializable (MainFragment.ARG_PARAM_TRIP, trip);

        fragment = new TabMapFragment ();
        fragment.setArguments (args);

        return fragment;
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
        if (getArguments () != null) {
            _trip = (Trip) getArguments ().getSerializable (MainFragment.ARG_PARAM_TRIP);
        }
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

        LocationBroadcastManager.registerNewLocationReceiver (getContext (),
                                                              _onNewLocationReceiver);
        LocationBroadcastManager.registerProviderEnableChange (getContext (),
                                                               _onProviderEnabledChange);
        LocationBroadcastManager.registerStatusChange (getContext (), _onProviderStatusChange);
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        LocationBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);
        LocationBroadcastManager.unregisterReceiver (getContext (), _onProviderEnabledChange);
        LocationBroadcastManager.unregisterReceiver (getContext (), _onProviderStatusChange);
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
        _mapHelper = new MapHelper (getContext ());
        _mapHelper.setMap (_map);
        _map.setMyLocationEnabled (true);

        //        if (isActiveTrip && SettingsManager.isLogTrip (getContext ())) {
        //            _map.setOnMyLocationChangeListener (new GoogleMap.OnMyLocationChangeListener ()
        //            {
        //                @Override
        //                public void onMyLocationChange (Location location)
        //                {
        //                    LogHelper.d ("*** onMapReady");
        //
        //                    _map.animateCamera (CameraUpdateFactory.newLatLngZoom (
        //                            new LatLng (location.getLatitude (), location.getLongitude ()), 50));
        //                }
        //            });
        //        }

        _mapHelper.drawTrip (_trip, isActiveTrip);
    }

    public void setMapType (int mapType)
    {
        _map.setMapType (mapType);
    }
}
