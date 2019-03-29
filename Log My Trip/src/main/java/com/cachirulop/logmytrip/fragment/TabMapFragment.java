package com.cachirulop.logmytrip.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.Location;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.helper.MapHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedJourneyHolder;
import com.cachirulop.logmytrip.manager.SettingsManager;
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
    private Journey   _journey;
    private boolean _zoomin = false;

    private static final int ZOOM_LEVEL = 16;

    private BroadcastReceiver _onNewLocationReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            android.location.Location l;
            Location                  tl;

            l = LogMyTripBroadcastManager.getLocation (intent);
            tl = new Location (l);

            _journey.addLocation (tl);

            if (_map.getCameraPosition ().zoom == ZOOM_LEVEL) {
                _zoomin = false;

                _map.animateCamera (CameraUpdateFactory.newLatLng (new LatLng (l.getLatitude (),
                                                                               l.getLongitude ())));
            }
            else {
                if (!_zoomin) {
                    _zoomin = true;
                    _map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (l.getLatitude (),
                                                                                       l.getLongitude ()),
                                                                           ZOOM_LEVEL));
                }
            }

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

        _journey = SelectedJourneyHolder.getInstance ().getSelectedJourney ();
        JourneyManager.loadJourneySegments (getContext (), _journey);
    }

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        try {
            View v;

            LogHelper.d ("TabMapFragment.onCreateView: Inflando el mapa");

            v = inflater.inflate (R.layout.fragment_tab_map, container, false);

            LogHelper.d ("TabMapFragment.onCreateView: Se ha inflado el mapa con éxito");

            return v;
        }
        catch (Exception e) {
            LogHelper.e ("Error loading map layout: " + e.getLocalizedMessage (), e);

            throw e;
        }
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        setUpMap ();
    }

    private void setUpMap ()
    {
        SupportMapFragment fragment;
        FragmentManager    manager;

        manager = getChildFragmentManager ();
        fragment = (SupportMapFragment) manager.findFragmentById (R.id.gmJourneyDetail);

        fragment.getMapAsync (this);
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

        if (_journey.getId () == SettingsManager.getCurrentJourneyId (getContext ())) {
            LogMyTripBroadcastManager.registerNewLocationReceiver (getContext (),
                                                                   _onNewLocationReceiver);
            LogMyTripBroadcastManager.registerProviderEnableChange (getContext (),
                                                                    _onProviderEnabledChange);
            LogMyTripBroadcastManager.registerStatusChange (getContext (), _onProviderStatusChange);
        }
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        if (_journey.getId () == SettingsManager.getCurrentJourneyId (getContext ())) {
            LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);
            LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onProviderEnabledChange);
            LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onProviderStatusChange);
        }
    }

    private void drawTrackMainThread ()
    {
        Activity parent;

        parent = getActivity ();
        if (parent != null) {
            LogMyTripApplication.runInMainThread (getContext (), new Runnable ()
            {
                @Override
                public void run ()
                {
                    _mapHelper.drawJourney (_journey, true);
                }
            });
        }
    }

    @SuppressLint ("MissingPermission")
    @Override
    public void onMapReady (GoogleMap googleMap)
    {
        boolean isActiveJourney;

        LogHelper.d ("*** Getting map");

        isActiveJourney = (_journey.getId () == SettingsManager.getCurrentJourneyId (getContext ()));

        _map = googleMap;
        _map.setMyLocationEnabled (true);
        _map.animateCamera (CameraUpdateFactory.zoomTo (ZOOM_LEVEL));

        LogHelper.d ("*** Map assigned");

        _mapHelper = new MapHelper (getContext ());
        _mapHelper.setMap (_map);
        _mapHelper.drawJourney (_journey, isActiveJourney);
    }

    public void setMapType (int mapType)
    {
        _map.setMapType (mapType);
    }
}
