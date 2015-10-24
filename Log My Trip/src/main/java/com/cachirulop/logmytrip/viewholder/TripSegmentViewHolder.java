package com.cachirulop.logmytrip.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.dialog.ConfirmDialog;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.manager.TripManager;
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

public class TripSegmentViewHolder
        extends RecyclerView.ViewHolder
{
    private TripStatisticsAdapter _adapter;

    private Toolbar _toolbar;
    private TextView _locationFrom;
    private TextView _locationTo;
    private TextView _startDate;
    private TextView _endDate;
    private TextView _startTime;
    private TextView _endTime;
    private TextView _totalDistance;
    private TextView _totalTime;
    private TextView _maxSpeed;
    private TextView _mediumSpeed;

    private FrameLayout           _mapFrame;
    private TripSegment           _segment;
    private Context               _ctx;
    private int _mapType;

    public TripSegmentViewHolder (TripStatisticsAdapter adapter, View parent, Context ctx, int mapType)
    {
        super (parent);

        _adapter = adapter;
        _ctx = ctx;
        _mapType = mapType;

        parent.setClickable (false);
        parent.setLongClickable (false);

        _mapFrame = (FrameLayout) parent.findViewById (R.id.flMapSegment);

        _locationFrom = (TextView) parent.findViewById (R.id.tvTripSegmentLocationFrom);
        _locationTo = (TextView) parent.findViewById (R.id.tvTripSegmentLocationTo);

        _startDate = (TextView) parent.findViewById (R.id.tvTripSegmentStartDate);
        _endDate = (TextView) parent.findViewById (R.id.tvTripSegmentEndDate);
        _startTime = (TextView) parent.findViewById (R.id.tvTripSegmentStartTime);
        _endTime = (TextView) parent.findViewById (R.id.tvTripSegmentEndTime);
        _totalDistance = (TextView) parent.findViewById (R.id.tvTripSegmentTotalDistance);
        _totalTime = (TextView) parent.findViewById (R.id.tvTripSegmentTotalTime);
        _maxSpeed = (TextView) parent.findViewById (R.id.tvTripSegmentMaxSpeed);
        _mediumSpeed = (TextView) parent.findViewById (R.id.tvTripSegmentMediumSpeed);

        _toolbar = (Toolbar) parent.findViewById (R.id.tbSegmentToolbar);
    }

    public void bindView (final TripSegment tripSegment, int position)
    {
        _segment = tripSegment;  // To get locations

        TripLocation l;

        _toolbar.setTitle (String.format (_ctx.getString (R.string.title_segment_num), position));
        _toolbar.inflateMenu (R.menu.menu_segment_actionmode);
        _toolbar.setOnMenuItemClickListener (new Toolbar.OnMenuItemClickListener ()
        {
            @Override
            public boolean onMenuItemClick (MenuItem item)
            {
                switch (item.getItemId ()) {
                    case R.id.action_delete_segment:
                        ConfirmDialog dlg;

                        dlg = new ConfirmDialog (R.string.title_delete, R.string.msg_delete_confirm)
                        {
                            @Override
                            public void onOkClicked ()
                            {
                                _adapter.removeItem (tripSegment);
                                TripManager.deleteSegment (_ctx, tripSegment);
                            }
                        };

                        dlg.show (((FragmentActivity) _ctx).getSupportFragmentManager (),
                                  "deleteTrip");
                }

                return true;
            }
        });

        l = _segment.getStartLocation ();
        if (l != null) {
            getLocationFrom ().setText (l.toString ());
            getStartDate ().setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            getStartTime ().setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (),
                                                                               getLocationFrom ()),
                                              l.toLocation ());
        }
        else {
            getLocationFrom ().setText ("");
            getStartDate ().setText ("");
            getStartTime ().setText ("");
        }

        l = _segment.getEndLocation ();
        if (l != null) {
            getLocationTo ().setText (l.toString ());
            getEndDate ().setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            getEndTime ().setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (),
                                                                               getLocationTo ()),
                                              l.toLocation ());
        }
        else {
            getLocationTo ().setText ("");
            getEndDate ().setText ("");
            getEndTime ().setText ("");
        }

        getTotalDistance ().setText (
                FormatHelper.formatDistance (_segment.computeTotalDistance ()));
        getTotalTime ().setText (FormatHelper.formatDuration (_segment.computeTotalTime ()));
        getMaxSpeed ().setText (FormatHelper.formatSpeed (_segment.computeMaxSpeed ()));
        getMediumSpeed ().setText (FormatHelper.formatSpeed (_segment.computeMediumSpeed ()));

        // Map
        GoogleMapOptions options = new GoogleMapOptions ();
        MapView          mapView;

        options.liteMode (true);

        mapView = new MapView (_ctx, options);
        mapView.onCreate (null);
        mapView.getMapAsync (new MapReadyCallback ());
        mapView.getMap ()
               .setMapType (_mapType);

        _mapFrame.addView (mapView);
    }

    public TextView getLocationFrom ()
    {
        return _locationFrom;
    }

    public TextView getStartDate ()
    {
        return _startDate;
    }

    public TextView getStartTime ()
    {
        return _startTime;
    }

    public TextView getLocationTo ()
    {
        return _locationTo;
    }

    public TextView getEndDate ()
    {
        return _endDate;
    }

    public TextView getEndTime ()
    {
        return _endTime;
    }

    public TextView getTotalDistance ()
    {
        return _totalDistance;
    }

    public TextView getTotalTime ()
    {
        return _totalTime;
    }

    public TextView getMaxSpeed ()
    {
        return _maxSpeed;
    }

    public TextView getMediumSpeed ()
    {
        return _mediumSpeed;
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

        track = new ArrayList<> ();

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
    }

    public Context getCtx ()
    {
        return _ctx;
    }

    public void setCtx (Context ctx)
    {
        _ctx = ctx;
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
