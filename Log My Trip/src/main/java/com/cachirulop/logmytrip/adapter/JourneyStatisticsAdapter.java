package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.JourneySegment;
import com.cachirulop.logmytrip.viewholder.JourneySegmentViewHolder;
import com.cachirulop.logmytrip.viewholder.JourneySummaryViewHolder;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class JourneyStatisticsAdapter
        extends RecyclerView.Adapter
{

    private final int ITEM_TYPE_JOURNEY = 0;
    private final int ITEM_TYPE_SEGMENT = 1;

    private Context _ctx;
    private Journey _journey;
    private int     _mapType;

    public JourneyStatisticsAdapter (Context ctx, Fragment parentFragment, Journey journey)
    {
        _ctx = ctx;
        _journey = journey;

        _mapType = GoogleMap.MAP_TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView = null;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        switch (viewType) {
            case ITEM_TYPE_JOURNEY:
                rowView = inflater.inflate (R.layout.journey_summary, parent, false);

                return new JourneySummaryViewHolder (this, rowView, _ctx);

            case ITEM_TYPE_SEGMENT:
                rowView = inflater.inflate (R.layout.journey_segment, parent, false);

                return new JourneySegmentViewHolder (this, rowView, _ctx, _mapType);
        }

        return null;
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        switch (getItemViewType (position)) {
            case ITEM_TYPE_JOURNEY:
                ((JourneySummaryViewHolder) holder).bindView (_journey, position);
                break;

            case ITEM_TYPE_SEGMENT:
                JourneySegment segment;

                segment = _journey.getSegments ().get (position - 1);
                ((JourneySegmentViewHolder) holder).bindView (segment, position);

                break;
        }
    }

    @Override
    public int getItemViewType (int position)
    {
        if (position == 0) {
            return ITEM_TYPE_JOURNEY;
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

        numSegments = _journey.getSegments ().size ();
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

    public void removeItem (JourneySegment t)
    {
        int               pos;
        List<JourneySegment> segments;

        segments = _journey.getSegments ();

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
