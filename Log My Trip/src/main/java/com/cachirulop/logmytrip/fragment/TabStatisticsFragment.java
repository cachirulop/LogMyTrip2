package com.cachirulop.logmytrip.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.receiver.AddressResultReceiver;
import com.cachirulop.logmytrip.service.FetchAddressService;
import com.cachirulop.logmytrip.util.FormatHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class TabStatisticsFragment
        extends Fragment
{
    private RecyclerView          _recyclerView;
    private TripStatisticsAdapter _adapter;
    private Trip                  _trip;
    private Context               _ctx;

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
        TabStatisticsFragment fragment;
        Bundle                args;

        args = new Bundle ();
        args.putSerializable (MainFragment.ARG_PARAM_TRIP, trip);

        fragment = new TabStatisticsFragment ();
        fragment.setArguments (args);

        return fragment;
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
        // return inflater.inflate (R.layout.fragment_tab_statistics, container, false);
        return inflater.inflate (R.layout.fragment_tab_statistics, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        LinearLayout            detail;
        CardView                card;

        super.onViewCreated (view, savedInstanceState);

        _ctx = getActivity ();

        _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvSegments);
        _recyclerView.setLayoutManager (new LinearLayoutManager (_ctx));
        _recyclerView.setHasFixedSize (true);

        _recyclerView.setItemAnimator (new DefaultItemAnimator ());

        _adapter = new TripStatisticsAdapter (_ctx, this, _trip);
        _recyclerView.setAdapter (_adapter);

        //        // LinearLayout to add the cards
        //        detail = (LinearLayout) view.findViewById (R.id.llTripStatistics);
        //
        //        // Trip summary card
        //        card = (CardView) getLayoutInflater (savedInstanceState).inflate (R.layout.trip_summary,
        //                                                                          null);
        //
        //        fillSummaryCard (card);
        //        detail.addView (card);
        //
        //        // Segments cards
        //        for (TripSegment s : _trip.getSegments ()) {
        //            card = (CardView) getLayoutInflater (savedInstanceState).inflate (R.layout.trip_segment,
        //                                                                              null);
        //
        //            fillSegmentCard (card, s);
        //            detail.addView (card);
        //        }
    }

    private void fillSummaryCard (CardView card)
    {
        TextView     tv;
        TextView date;
        TextView time;
        TripLocation l;

        tv = (TextView) card.findViewById (R.id.tvTripSummaryLocationFrom);
        date = (TextView) card.findViewById (R.id.tvTripSummaryStartDate);
        time = (TextView) card.findViewById (R.id.tvTripSummaryStartTime);

        l = _trip.getStartLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            date.setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            time.setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
            date.setText ("");
            time.setText ("");
        }

        tv = (TextView) card.findViewById (R.id.tvTripSummaryLocationTo);
        date = (TextView) card.findViewById (R.id.tvTripSummaryEndDate);
        time = (TextView) card.findViewById (R.id.tvTripSummaryEndTime);

        l = _trip.getEndLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            date.setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            time.setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
            date.setText ("");
            time.setText ("");
        }

        tv = (TextView) card.findViewById (R.id.tvTripSummaryTotalDistance);
        tv.setText (FormatHelper.formatDistance (_trip.computeTotalDistance ()));

        tv = (TextView) card.findViewById (R.id.tvTripSummaryTotalTime);
        tv.setText (FormatHelper.formatDuration (_trip.computeTotalTime ()));

        tv = (TextView) card.findViewById (R.id.tvTripSummaryMaxSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMaxSpeed ()));

        tv = (TextView) card.findViewById (R.id.tvTripSummaryMediumSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMediumSpeed ()));
    }

    private void fillSegmentCard (CardView card, TripSegment s)
    {
        TextView     tv;
        TextView     date;
        TextView     time;
        TripLocation l;

        tv = (TextView) card.findViewById (R.id.tvTripSegmentLocationFrom);
        date = (TextView) card.findViewById (R.id.tvTripSegmentStartDate);
        time = (TextView) card.findViewById (R.id.tvTripSegmentStartTime);

        l = _trip.getStartLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            date.setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            time.setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
            date.setText ("");
            time.setText ("");
        }

        tv = (TextView) card.findViewById (R.id.tvTripSegmentLocationTo);
        date = (TextView) card.findViewById (R.id.tvTripSegmentEndDate);
        time = (TextView) card.findViewById (R.id.tvTripSegmentEndTime);

        l = _trip.getEndLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            date.setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            time.setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
            date.setText ("");
            time.setText ("");
        }

        tv = (TextView) card.findViewById (R.id.tvTripSegmentTotalDistance);
        tv.setText (FormatHelper.formatDistance (_trip.computeTotalDistance ()));

        tv = (TextView) card.findViewById (R.id.tvTripSegmentTotalTime);
        tv.setText (FormatHelper.formatDuration (_trip.computeTotalTime ()));

        tv = (TextView) card.findViewById (R.id.tvTripSegmentMaxSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMaxSpeed ()));

        tv = (TextView) card.findViewById (R.id.tvTripSegmentMediumSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMediumSpeed ()));

        // Map
        GoogleMapOptions options = new GoogleMapOptions ();
        MapView          mapView;
        FrameLayout      mapFrame;

        options.liteMode (true);

        mapView = new MapView (_ctx, options);
        mapView.onCreate (null);
        mapView.getMapAsync (new TripSegmentMapReadyCallback (s));

        mapFrame = (FrameLayout) card.findViewById (R.id.flMapSegment);
        mapFrame.addView (mapView);
    }

    private void drawSegmentMap (final GoogleMap map, TripSegment segment)
    {
        final LatLngBounds.Builder builder;
        List<LatLng>               track;

        builder = new LatLngBounds.Builder ();

        List<TripLocation> points;
        MarkerOptions      markerOptions;

        points = segment.getLocations ();

        markerOptions = new MarkerOptions ();
        markerOptions.position (points.get (0)
                                      .toLatLng ());

        // TODO: set markeroptions title

        map.addMarker (markerOptions);

        markerOptions = new MarkerOptions ();
        markerOptions.position (points.get (points.size () - 1)
                                      .toLatLng ());
        map.addMarker (markerOptions);

        track = new ArrayList<LatLng> ();

        for (TripLocation p : points) {
            LatLng current;

            current = p.toLatLng ();

            track.add (current);
            builder.include (current);
        }

        Polyline        route;
        PolylineOptions routeOptions;
        Polyline        border;
        PolylineOptions borderOptions;

        routeOptions = new PolylineOptions ();
        routeOptions.width (5);
        routeOptions.color (Color.RED);
        routeOptions.geodesic (true);

        borderOptions = new PolylineOptions ();
        borderOptions.width (10);
        borderOptions.color (Color.GRAY);
        borderOptions.geodesic (true);

        border = map.addPolyline (borderOptions);
        route = map.addPolyline (routeOptions);

        route.setPoints (track);
        border.setPoints (track);

        map.moveCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 0));

        //            map.setOnCameraChangeListener (new GoogleMap.OnCameraChangeListener ()
        //            {
        //
        //                @Override
        //                public void onCameraChange (CameraPosition arg0)
        //                {
        //                    // Move camera.
        //                    map.moveCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 20));
        //
        //                    // Remove listener to prevent position reset on camera move.
        //                    map.setOnCameraChangeListener (null);
        //                }
        //            });
    }

    private class TripSegmentMapReadyCallback
            implements OnMapReadyCallback
    {
        TripSegment _segment;

        public TripSegmentMapReadyCallback (TripSegment segment)
        {
            _segment = segment;
        }

        @Override
        public void onMapReady (GoogleMap googleMap)
        {
            drawSegmentMap (googleMap, _segment);
        }
    }
}
