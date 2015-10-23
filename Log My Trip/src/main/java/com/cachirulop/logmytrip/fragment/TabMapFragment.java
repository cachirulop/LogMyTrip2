package com.cachirulop.logmytrip.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.manager.LocationBroadcastManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.service.LogMyTripService;
import com.cachirulop.logmytrip.util.LogHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
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

    private BroadcastReceiver _onNewLocationReceiver   = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
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

                    msg = Snackbar.make (getView (), R.string.msg_gps_disabled,
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
            ArrayList<PolygonOptions> arrows;
            arrows = new ArrayList<> ();

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

                int i = 0;
                for (TripLocation p : points) {
                    LatLng current;

                    current = p.toLatLng ();

                    if ((i % 10) == 0) {
                        //                        PolygonOptions arrow;
                        //                        LatLng p1;
                        //                        LatLng p2;
                        //                        double factor = 0.0005;
                        //
                        //                        // p1 = new LatLng (current.latitude + factor, current.longitude - factor);
                        //                        // p2 = new LatLng (current.latitude + factor, current.longitude + factor);
                        //                        p1 = new LatLng (rotateLatitudeAround (current.latitude + factor, current.longitude - factor, p.getBearing (), current),
                        //                                         rotateLongitudeAround (current.latitude + factor, current.longitude - factor, p.getBearing (), current));
                        //                        p2 = new LatLng (rotateLatitudeAround (current.latitude + factor, current.longitude + factor, p.getBearing (), current),
                        //                                         rotateLongitudeAround (current.latitude + factor, current.longitude + factor, p.getBearing (), current));
                        //
                        //                        arrow = new PolygonOptions ();
                        //                        arrow.add (p1, current, p2);
                        //                        arrow.strokeWidth (5);
                        //                        arrow.strokeColor (Color.GRAY);
                        //                        arrow.fillColor (SEGMENT_COLORS[currentColor]);
                        //                        arrow.zIndex (10);
                        //
                        //                        arrows.add (arrow);


                        MarkerOptions arrowOptions;

                        //                        arrowOptions = new MarkerOptions ();
                        //                        arrowOptions.position (p.toLatLng ());
                        //                        arrowOptions.rotation (p.getBearing ());
                        //                        //arrowOptions.icon (BitmapDescriptorFactory.fromResource (R.mipmap.map_track_arrow));
                        //                        //                        arrowOptions.icon (BitmapDescriptorFactory.fromBitmap (
                        //                        //                                getArrow (p.getBearing ()).getBitmap ()));
                        //                        _map.addMarker (arrowOptions);

                        arrowOptions = new MarkerOptions ();
                        arrowOptions.flat (true);
                        arrowOptions.position (p.toLatLng ());
                        arrowOptions.rotation (p.getBearing () + 180);
                        arrowOptions.icon (
                                BitmapDescriptorFactory.fromResource (R.mipmap.map_track_arrow));
                        //                        arrowOptions.icon (BitmapDescriptorFactory.fromBitmap (
                        //                                getArrow (p.getBearing ()).getBitmap ()));
                        _map.addMarker (arrowOptions);
                    }

                    track.add (current);
                    builder.include (current);

                    i++;
                }

                Polyline route;
                PolylineOptions routeOptions;
                Polyline border;
                PolylineOptions borderOptions;

                routeOptions = new PolylineOptions ();
                routeOptions.width (5);
                routeOptions.color (SEGMENT_COLORS[currentColor]);
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
                currentColor = currentColor % SEGMENT_COLORS.length;
                currentIndex++;
            }

            for (PolygonOptions p : arrows) {
                _map.addPolygon (p);
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


    public double rotateLatitudeAround (double lat, double lon, double angle, LatLng center)
    {
        lat = center.latitude + (Math.cos (
                Math.toRadians (angle)) * (lat - center.latitude) - Math.sin (
                Math.toRadians (angle)) * (lon - center.longitude));

        return lat;
    }

    public double rotateLongitudeAround (double lat, double lon, double angle, LatLng center)
    {
        lon = center.longitude + (Math.sin (
                Math.toRadians (angle)) * (lat - center.latitude) + Math.cos (
                Math.toRadians (angle)) * (lon - center.longitude));

        return lon;
    }

    public BitmapDrawable getArrow (float angle)
    {
        Bitmap arrowBitmap = BitmapFactory.decodeResource (getContext ().getResources (),
                                                           R.mipmap.map_track_arrow);
        // Create blank bitmap of equal size
        Bitmap canvasBitmap = arrowBitmap.copy (Bitmap.Config.ARGB_8888, true);
        canvasBitmap.eraseColor (0x00000000);

        // Create canvas
        Canvas canvas = new Canvas (canvasBitmap);

        // Create rotation matrix
        Matrix rotateMatrix = new Matrix ();
        rotateMatrix.setRotate (angle, canvas.getWidth () / 2, canvas.getHeight () / 2);

        // Draw bitmap onto canvas using matrix
        canvas.drawBitmap (arrowBitmap, rotateMatrix, null);

        return new BitmapDrawable (getContext ().getResources (), canvasBitmap);
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
