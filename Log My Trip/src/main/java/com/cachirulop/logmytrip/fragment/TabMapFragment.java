package com.cachirulop.logmytrip.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class TabMapFragment extends Fragment {
    public static final String ARG_PARAM_TRIP = "PARAMETER_TRIP";
    private GoogleMap _map;
    private Trip _trip;

    public TabMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param trip Trip to show the map.
     * @return A new instance of fragment TabMapFragment.
     */
    public static TabMapFragment newInstance(Trip trip) {
        TabMapFragment fragment;
        Bundle args;

        fragment = new TabMapFragment();
        args = new Bundle();

        args.putSerializable(ARG_PARAM_TRIP, trip);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _trip = (Trip) getArguments().getSerializable(ARG_PARAM_TRIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (_map == null) {
            // Try to obtain the map from the SupportMapFragment.
            FragmentManager fm;

            fm = getFragmentManager();

            _map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.gmTripDetail)).getMap();

            // Check if we were successful in obtaining the map.
            if (_map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        List<LatLng> track;
        List<TripLocation> points;
        final LatLngBounds.Builder builder;
        CameraUpdate camera;

        points = _trip.getLocations();

        track = new ArrayList<LatLng>();
        builder = new LatLngBounds.Builder();

        for (TripLocation p : points) {
            LatLng current;

            current = p.toLatLng();

            track.add(current);
            builder.include(current);
        }

        Polyline route;
        PolylineOptions routeOptions;
        Polyline border;
        PolylineOptions borderOptions;

        routeOptions = new PolylineOptions();
        routeOptions.width(5);
        routeOptions.color(Color.RED);
        routeOptions.geodesic(true);

        borderOptions = new PolylineOptions();
        borderOptions.width(10);
        borderOptions.color(Color.GRAY);
        borderOptions.geodesic(true);

        border = _map.addPolyline(borderOptions);
        route = _map.addPolyline(routeOptions);

        route.setPoints(track);
        border.setPoints(track);

        // camera = CameraUpdateFactory.newLatLngBounds(builder.build(), 25, 25, 5);
        // _map.animateCamera(camera);
        _map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                _map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));

                // Remove listener to prevent position reset on camera move.
                _map.setOnCameraChangeListener(null);
            }
        });
    }
}
