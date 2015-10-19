package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.view.TripSegmentViewHolder;
import com.cachirulop.logmytrip.view.TripSummaryViewHolder;
import com.google.android.gms.maps.GoogleMap;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripStatisticsAdapter
        extends RecyclerView.Adapter
{

    private final int ITEM_TYPE_TRIP    = 0;
    private final int ITEM_TYPE_SEGMENT = 1;
    Context  _ctx;
    Trip     _trip;
    Fragment _parentFragment;
    private SparseBooleanArray      _selectedItems;
    private boolean                 _actionMode;
    private OnTripItemClickListener _onTripItemClickListener;
    private int _mapType;

    public TripStatisticsAdapter (Context ctx, Fragment parentFragment, Trip trip)
    {
        _ctx = ctx;
        _trip = trip;
        _parentFragment = parentFragment;

        _onTripItemClickListener = null;

        _selectedItems = new SparseBooleanArray ();

        _mapType = GoogleMap.MAP_TYPE_NORMAL;
    }

    public OnTripItemClickListener getOnTripSegmentItemClickListener ()
    {
        return _onTripItemClickListener;
    }

    public void setOnTripSegmentItemClickListener (OnTripItemClickListener listener)
    {
        _onTripItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView = null;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (
                Context.LAYOUT_INFLATER_SERVICE);

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
                ((TripSummaryViewHolder) holder).bindView (_parentFragment, _trip, position);
                break;

            case ITEM_TYPE_SEGMENT:
                TripSegment segment;

                segment = _trip.getSegments ()
                               .get (position - 1);
                ((TripSegmentViewHolder) holder).bindView (_parentFragment, segment, position);
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

        numSegments = _trip.getSegments ()
                           .size ();
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

    public void toggleSelection (int pos)
    {
        if (_selectedItems.get (pos, false)) {
            _selectedItems.delete (pos);
        }
        else {
            _selectedItems.put (pos, true);
        }

        notifyItemChanged (pos);
    }

    //
    //    public void clearSelections ()
    //    {
    //        _selectedItems.clear ();
    //
    //        notifyDataSetChanged ();
    //    }
    //
    //    public int getSelectedItemCount ()
    //    {
    //        return _selectedItems.size ();
    //    }
    //
    //    public List<Trip> getSelectedItems ()
    //    {
    //        List<Trip> result;
    //
    //        result = new ArrayList<Trip> (_selectedItems.size ());
    //
    //        for (int i = 0 ; i < _selectedItems.size () ; i++) {
    //            result.add (_items.get (_selectedItems.keyAt (i)));
    //        }
    //
    //        return result;
    //    }
    //
    public boolean isActionMode ()
    {
        return _actionMode;
    }

    //
    //    public void setActionMode (boolean selectionMode)
    //    {
    //        this._actionMode = selectionMode;
    //        this.notifyDataSetChanged ();
    //    }
    //
    //    public void removeItem (Trip t)
    //    {
    //        int pos;
    //
    //        pos = _items.indexOf (t);
    //        if (pos != -1) {
    //            _items.remove (t);
    //            notifyItemChanged (pos);
    //        }
    //    }
    //
    //    public Trip getItem (int position)
    //    {
    //        return _items.get (position);
    //    }
    //
    public interface OnTripItemClickListener
    {
        void onTripItemLongClick (View v, int position);

        void onTripItemClick (View v, int position);
    }

}
