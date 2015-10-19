package com.cachirulop.logmytrip.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.manager.LocationBroadcastManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.service.LogMyTripService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class TabMapFragment
        extends Fragment
        implements OnMapReadyCallback
{
    private static final int[] SEGMENT_COLORS = new int[]{ Color.RED, Color.BLUE, Color.GREEN,
                                                           Color.MAGENTA, Color.YELLOW };
    private GoogleMap _map;
    private Trip      _trip;
    private LogMyTripService _service = null;

    private BroadcastReceiver _onNewLocationReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            drawTrackMainThread ();
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
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
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

        ((SupportMapFragment) getChildFragmentManager ().findFragmentById (
                R.id.gmTripDetail)).getMapAsync (this);
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
    }

    @Override
    public void onPause ()
    {
        super.onPause ();

        LocationBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);
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
                    drawTrack ();
                }
            };

            main.post (runInMain);
        }
    }

    private void drawTrack ()
    {
        final LatLngBounds.Builder builder;
        List<LatLng>      track;
        List<TripSegment> segments;
        CameraUpdate      camera;
        int                        currentColor;

        if (_trip != null) {
            boolean isActiveTrip;
            Trip activeTrip;
            int lastSegmentIndex;
            int currentIndex;

            activeTrip = TripManager.getActiveTrip (getContext ());
            isActiveTrip = (_trip.equals (activeTrip));

            segments = _trip.getSegments (true);
            lastSegmentIndex = segments.size () - 1;
            currentIndex = 0;

            currentColor = 0;
            builder = new LatLngBounds.Builder ();

            for (TripSegment s : segments) {
                List<TripLocation> points;
                MarkerOptions markerOptions;
                boolean showCurrentPosition;

                showCurrentPosition = (isActiveTrip) && (currentIndex == lastSegmentIndex);
                points = s.getLocations ();

                // TODO: set markeroptions title

                // Start mark
                markerOptions = new MarkerOptions ();
                markerOptions.position (points.get (0)
                                              .toLatLng ());
                _map.addMarker (markerOptions);

                // End mark
                if (!showCurrentPosition) {
                    markerOptions = new MarkerOptions ();
                    markerOptions.position (points.get (points.size () - 1)
                                                  .toLatLng ());
                    _map.addMarker (markerOptions);
                }

                track = new ArrayList<LatLng> ();

                for (TripLocation p : points) {
                    LatLng current;

                    current = p.toLatLng ();

                    track.add (current);
                    builder.include (current);
                }

                Polyline route;
                PolylineOptions routeOptions;
                Polyline border;
                PolylineOptions borderOptions;

                routeOptions = new PolylineOptions ();
                routeOptions.width (5);
                routeOptions.color (SEGMENT_COLORS[currentColor % SEGMENT_COLORS.length]);
                routeOptions.geodesic (true);

                borderOptions = new PolylineOptions ();
                borderOptions.width (10);
                borderOptions.color (Color.GRAY);
                borderOptions.geodesic (true);

                border = _map.addPolyline (borderOptions);
                route = _map.addPolyline (routeOptions);

                route.setPoints (track);
                border.setPoints (track);

                currentColor++;
                currentIndex++;
            }

            if (_trip.getSegments ()
                     .size () > 0 && !isActiveTrip && !SettingsManager.isLogTrip (getContext ())) {
                _map.setOnCameraChangeListener (new GoogleMap.OnCameraChangeListener ()
                {

                    @Override
                    public void onCameraChange (CameraPosition arg0)
                    {
                        // Move camera.
                        _map.moveCamera (
                                CameraUpdateFactory.newLatLngBounds (builder.build (), 50));

                        // Remove listener to prevent position reset on camera move.
                        _map.setOnCameraChangeListener (null);
                    }
                });
            }
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

        if (isActiveTrip && SettingsManager.isLogTrip (getContext ())) {
            _map.setOnMyLocationChangeListener (new GoogleMap.OnMyLocationChangeListener ()
            {
                @Override
                public void onMyLocationChange (Location location)
                {
                    _map.animateCamera (CameraUpdateFactory.newLatLngZoom (
                            new LatLng (location.getLatitude (), location.getLongitude ()), 20));
                }
            });
        }

        drawTrack ();
    }

    public void setMapType (int mapType)
    {
        _map.setMapType (mapType);
    }
}
