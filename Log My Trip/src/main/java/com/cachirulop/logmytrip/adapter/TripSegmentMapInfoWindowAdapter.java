package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by dmagro on 03/11/2015.
 */
public class TripSegmentMapInfoWindowAdapter
        implements GoogleMap.InfoWindowAdapter
{
    private Context     _ctx;
    private TripSegment _segment;

    public TripSegmentMapInfoWindowAdapter (Context ctx)
    {
        _ctx = ctx;
    }

    public TripSegment getSegment ()
    {
        return _segment;
    }

    public void setSegment (TripSegment segment)
    {
        _segment = segment;
    }

    @Override
    public View getInfoWindow (Marker marker)
    {
        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate (R.layout.trip_segment_map_info_window, null);
    }

    @Override
    public View getInfoContents (Marker marker)
    {
        return null;
    }
}
