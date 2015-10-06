package com.cachirulop.logmytrip.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
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

/**
 * Created by dmagro on 06/10/2015.
 */
public class TripSegmentViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener
{
    private TripStatisticsAdapter _adapter;
    private TextView              _description;
    private TextView              _distance;
    private FrameLayout           _mapFrame;
    private TripSegment           _segment;
    private Context               _ctx;

    private TripStatisticsAdapter.OnTripItemClickListener _onTripItemClickListener;

    public TripSegmentViewHolder (TripStatisticsAdapter adapter, View parent, Context ctx)
    {
        super (parent);

        _adapter = adapter;
        _ctx = ctx;

        parent.setClickable (true);
        parent.setLongClickable (true);

        parent.setOnClickListener (this);
        parent.setOnLongClickListener (this);

        _mapFrame = (FrameLayout) parent.findViewById (R.id.flMapSegment);
        _description = (TextView) parent.findViewById (R.id.tvTripSegmentDescription);
        _distance = (TextView) parent.findViewById (R.id.tvTripSegmentDistance);

        _onTripItemClickListener = null;
    }

    public TripStatisticsAdapter.OnTripItemClickListener getOnTripItemClickListener ()
    {
        return _onTripItemClickListener;
    }

    public void setOnTripItemClickListener (TripStatisticsAdapter.OnTripItemClickListener listener)
    {
        _onTripItemClickListener = listener;
    }

    @Override
    public void onClick (View v)
    {
        if (_adapter.isActionMode ()) {
            _adapter.toggleSelection (this.getLayoutPosition ());
        }

        if (_onTripItemClickListener != null) {
            _onTripItemClickListener.onTripItemClick (v, this.getAdapterPosition ());
        }
    }

    @Override
    public boolean onLongClick (View v)
    {
        itemView.setBackground (
                ContextCompat.getDrawable (_ctx, R.drawable.trip_list_selector_actionmode));

        _adapter.toggleSelection (this.getLayoutPosition ());

        if (_onTripItemClickListener != null) {
            _onTripItemClickListener.onTripItemLongClick (v, this.getAdapterPosition ());
        }

        return false;
    }

    public void bindView (Fragment parentFragment, TripSegment tripSegment, int position)
    {
        // General data
        _segment = tripSegment;  // To get locations
        _description.setText (String.format ("%s - %s (%s)", FormatHelper.formatTime (_ctx,
                                                                                      tripSegment.getStartDate ()),
                                             FormatHelper.formatTime (_ctx,
                                                                      tripSegment.getEndDate ()),
                                             FormatHelper.formatDuration (
                                                     tripSegment.computeTotalTime ())));
        _distance.setText (String.format ("%.3f", tripSegment.computeTotalDistance ()));

        // Map
        GoogleMapOptions options = new GoogleMapOptions ();
        MapView          mapView;

        options.liteMode (true);

        mapView = new MapView (_ctx, options);
        mapView.onCreate (null);
        mapView.getMapAsync (new MapReadyCallback ());

        _mapFrame.addView (mapView);
    }

    public TextView getDescription ()
    {
        return _description;
    }

    private void drawMap (final GoogleMap map)
    {
        final LatLngBounds.Builder builder;
        List<LatLng>               track;

        builder = new LatLngBounds.Builder ();

        List<TripLocation> points;
        MarkerOptions      markerOptions;

        points = _segment.getLocations ();

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

    private class MapReadyCallback
            implements OnMapReadyCallback
    {
        @Override
        public void onMapReady (GoogleMap googleMap)
        {
            drawMap (googleMap);
        }
    }

}
