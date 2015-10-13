package com.cachirulop.logmytrip.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
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

/**
 * Created by dmagro on 06/10/2015.
 */
public class TripSegmentViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener
{
    private TripStatisticsAdapter _adapter;

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
    private AddressResultReceiver _addressFromReceiver;
    private AddressResultReceiver _addressToReceiver;

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

        _locationFrom = (TextView) parent.findViewById (R.id.tvTripSegmentLocationFrom);
        _locationTo = (TextView) parent.findViewById (R.id.tvTripSegmentLocationTo);

        _addressFromReceiver = new AddressResultReceiver (new Handler (), getLocationFrom ());
        _addressToReceiver = new AddressResultReceiver (new Handler (), getLocationTo ());

        _startDate = (TextView) parent.findViewById (R.id.tvTripSegmentStartDate);
        _endDate = (TextView) parent.findViewById (R.id.tvTripSegmentEndDate);
        _startTime = (TextView) parent.findViewById (R.id.tvTripSegmentStartTime);
        _endTime = (TextView) parent.findViewById (R.id.tvTripSegmentEndTime);
        _totalDistance = (TextView) parent.findViewById (R.id.tvTripSegmentTotalDistance);
        _totalTime = (TextView) parent.findViewById (R.id.tvTripSegmentTotalTime);
        _maxSpeed = (TextView) parent.findViewById (R.id.tvTripSegmentMaxSpeed);
        _mediumSpeed = (TextView) parent.findViewById (R.id.tvTripSegmentMediumSpeed);

        _onTripItemClickListener = null;
    }

    public TextView getLocationFrom ()
    {
        return _locationFrom;
    }

    public void setLocationFrom (TextView locationFrom)
    {
        _locationFrom = locationFrom;
    }

    public TextView getLocationTo ()
    {
        return _locationTo;
    }

    public void setLocationTo (TextView locationTo)
    {
        _locationTo = locationTo;
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
        _segment = tripSegment;  // To get locations

        // General data
        TripLocation l;

        l = _segment.getStartLocation ();
        if (l != null) {
            getLocationFrom ().setText (l.toString ());
            FetchAddressService.startService (_ctx, _addressFromReceiver, l.toLocation ());
        }
        else {
            getLocationFrom ().setText ("");
        }

        l = _segment.getEndLocation ();
        if (l != null) {
            getLocationTo ().setText (l.toString ());
            FetchAddressService.startService (_ctx, _addressToReceiver, l.toLocation ());
        }
        else {
            getLocationTo ().setText ("");
        }

        getStartDate ().setText (FormatHelper.formatDate (_ctx, _segment.getStartLocation ()
                                                                        .getLocationTimeAsDate ()));
        getEndDate ().setText (FormatHelper.formatDate (_ctx, _segment.getEndLocation ()
                                                                      .getLocationTimeAsDate ()));
        getStartTime ().setText (FormatHelper.formatTime (_ctx, _segment.getStartLocation ()
                                                                        .getLocationTimeAsDate ()));
        getEndTime ().setText (FormatHelper.formatTime (_ctx, _segment.getEndLocation ()
                                                                      .getLocationTimeAsDate ()));

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

        _mapFrame.addView (mapView);
    }

    public TextView getStartDate ()
    {
        return _startDate;
    }

    public TextView getEndDate ()
    {
        return _endDate;
    }

    public void setEndDate (TextView endDate)
    {
        _endDate = endDate;
    }

    public TextView getStartTime ()
    {
        return _startTime;
    }

    public TextView getEndTime ()
    {
        return _endTime;
    }

    public void setEndTime (TextView endTime)
    {
        _endTime = endTime;
    }

    public TextView getTotalDistance ()
    {
        return _totalDistance;
    }

    public void setTotalDistance (TextView totalDistance)
    {
        _totalDistance = totalDistance;
    }

    public TextView getTotalTime ()
    {
        return _totalTime;
    }

    public TextView getMaxSpeed ()
    {
        return _maxSpeed;
    }

    public void setMaxSpeed (TextView maxSpeed)
    {
        _maxSpeed = maxSpeed;
    }

    public TextView getMediumSpeed ()
    {
        return _mediumSpeed;
    }

    public void setMediumSpeed (TextView mediumSpeed)
    {
        _mediumSpeed = mediumSpeed;
    }

    public void setTotalTime (TextView totalTime)
    {
        _totalTime = totalTime;
    }

    public void setStartTime (TextView startTime)
    {
        _startTime = startTime;
    }

    public void setStartDate (TextView startDate)
    {
        _startDate = startDate;
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

    public TripStatisticsAdapter getAdapter ()
    {
        return _adapter;
    }

    public void setAdapter (TripStatisticsAdapter adapter)
    {
        _adapter = adapter;
    }

    public AddressResultReceiver getAddressFromReceiver ()
    {
        return _addressFromReceiver;
    }

    public void setAddressFromReceiver (AddressResultReceiver addressFromReceiver)
    {
        _addressFromReceiver = addressFromReceiver;
    }

    public AddressResultReceiver getAddressToReceiver ()
    {
        return _addressToReceiver;
    }

    public void setAddressToReceiver (AddressResultReceiver addressToReceiver)
    {
        _addressToReceiver = addressToReceiver;
    }

    public Context getCtx ()
    {
        return _ctx;
    }

    public void setCtx (Context ctx)
    {
        _ctx = ctx;
    }

    public FrameLayout getMapFrame ()
    {
        return _mapFrame;
    }

    public void setMapFrame (FrameLayout mapFrame)
    {
        _mapFrame = mapFrame;
    }

    public TripSegment getSegment ()
    {
        return _segment;
    }

    public void setSegment (TripSegment segment)
    {
        _segment = segment;
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
