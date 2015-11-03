package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.viewholder.TripSegmentViewHolder;
import com.cachirulop.logmytrip.viewholder.TripSummaryViewHolder;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripStatisticsAdapter
        extends RecyclerView.Adapter
{

    private final int ITEM_TYPE_TRIP    = 0;
    private final int ITEM_TYPE_SEGMENT = 1;

    private Context _ctx;
    private Trip    _trip;
    private int _mapType;

    public TripStatisticsAdapter (Context ctx, Fragment parentFragment, Trip trip)
    {
        _ctx = ctx;
        _trip = trip;

        _mapType = GoogleMap.MAP_TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView = null;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        switch (viewType) {
            case ITEM_TYPE_TRIP:
                rowView = inflater.inflate (R.layout.trip_summary, parent, false);

                return new TripSummaryViewHolder (this, rowView, _ctx);

            case ITEM_TYPE_SEGMENT:
                rowView = inflater.inflate (R.layout.trip_segment, parent, false);

                return new TripSegmentViewHolder (this, rowView, _ctx, _mapType);
        }

        return null;
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        switch (getItemViewType (position)) {
            case ITEM_TYPE_TRIP:
                ((TripSummaryViewHolder) holder).bindView (_trip, position);
                break;

            case ITEM_TYPE_SEGMENT:
                TripSegment segment;

                segment = _trip.getSegments ().get (position - 1);
                ((TripSegmentViewHolder) holder).bindView (segment, position);

                break;
        }
    }

    @Override
    public int getItemViewType (int position)
    {
        if (position == 0) {
            return ITEM_TYPE_TRIP;
        }
        else {
            return ITEM_TYPE_SEGMENT;
        }
    }

    @Override
    public long getItemId (int position)
    {
        return position;
    }

    @Override
    public int getItemCount ()
    {
        int numSegments;

        numSegments = _trip.getSegments ().size ();
        if (numSegments <= 1) {
            // 1 or 0 segments, only show the summary
            return 1;
        }
        else {
            // Show the summary + segments
            return numSegments + 1;
        }
    }

    public void setMapType (int type)
    {
        _mapType = type;
        notifyDataSetChanged ();
    }

    public void removeItem (TripSegment t)
    {
        int               pos;
        List<TripSegment> segments;

        segments = _trip.getSegments ();

        pos = segments.indexOf (t);
        if (pos != -1) {
            segments.remove (t);
            notifyItemRemoved (pos + 1);

            if (segments.size () == 1) {
                notifyItemRemoved (2);
            }
        }
    }
}
