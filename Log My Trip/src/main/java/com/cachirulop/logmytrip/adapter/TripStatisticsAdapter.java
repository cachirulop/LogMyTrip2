package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripStatisticsAdapter
        extends RecyclerView.Adapter
{

    private final int ITEM_TYPE_TRIP    = 0;
    private final int ITEM_TYPE_SEGMENT = 1;
    Context _ctx;
    Trip    _trip;
    private SparseBooleanArray      _selectedItems;
    private boolean                 _actionMode;
    private OnTripItemClickListener _onTripItemClickListener;

    public TripStatisticsAdapter (Context ctx, Trip trip)
    {
        _ctx = ctx;
        _trip = trip;

        _onTripItemClickListener = null;

        _selectedItems = new SparseBooleanArray ();
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

                return new TripSummaryViewHolder (this, rowView);

            case ITEM_TYPE_SEGMENT:
                rowView = inflater.inflate (R.layout.trip_segment, parent, false);

                return new TripSegmentViewHolder (this, rowView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        // Set data into the view.
        int imgId;

        imgId = R.mipmap.ic_trip_status_saved;

        switch (getItemViewType (position)) {
            case ITEM_TYPE_TRIP:
                ((TripStatisticsAdapter.TripSummaryViewHolder) holder).bindView (_trip);
                break;

            case ITEM_TYPE_SEGMENT:
                ((TripStatisticsAdapter.TripSegmentViewHolder) holder).bindView (
                        _trip.getSegments ()
                             .get (position - 1));
                break;
        }

        // vh.setOnTripItemClickListener (_onTripItemClickListener);
        // vh.itemView.setActivated (_selectedItems.get (position, false));

        //        Drawable background;
        //
        //        if (_actionMode) {
        //            background = ContextCompat.getDrawable (_ctx, R.drawable.trip_list_selector_actionmode);
        //        }
        //        else {
        //            background = ContextCompat.getDrawable (_ctx, R.drawable.trip_list_selector);
        //        }
        //
        //        vh.itemView.setBackground (background);
        //        background.jumpToCurrentState ();
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
        return _trip.getSegments ()
                    .size () + 1;
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


    /////////////////////////////////////////////////////////////////////////////////////////////
    // ViewHolders classes
    /////////////////////////////////////////////////////////////////////////////////////////////

    public static class TripSummaryViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                       View.OnLongClickListener
    {
        private TripStatisticsAdapter _adapter;
        private TextView              _description;

        private OnTripItemClickListener _onTripItemClickListener;

        public TripSummaryViewHolder (TripStatisticsAdapter adapter, View parent)
        {
            super (parent);

            _adapter = adapter;

            parent.setClickable (true);
            parent.setLongClickable (true);

            parent.setOnClickListener (this);
            parent.setOnLongClickListener (this);

            _description = (TextView) parent.findViewById (R.id.tvTripSummaryDescription);

            _onTripItemClickListener = null;
        }

        public OnTripItemClickListener getOnTripItemClickListener ()
        {
            return _onTripItemClickListener;
        }

        public void setOnTripItemClickListener (OnTripItemClickListener listener)
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
            itemView.setBackground (ContextCompat.getDrawable (_adapter._ctx,
                                                               R.drawable.trip_list_selector_actionmode));

            _adapter.toggleSelection (this.getLayoutPosition ());

            if (_onTripItemClickListener != null) {
                _onTripItemClickListener.onTripItemLongClick (v, this.getAdapterPosition ());
            }

            return false;
        }

        public void bindView (Trip trip)
        {
            getDescription ().setText (trip.getDescription ());
        }

        public TextView getDescription ()
        {
            return _description;
        }
    }

    public static class TripSegmentViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                       View.OnLongClickListener
    {
        private TripStatisticsAdapter _adapter;
        private TextView              _description;
        private MapView               _mapView;

        private OnTripItemClickListener _onTripItemClickListener;

        public TripSegmentViewHolder (TripStatisticsAdapter adapter, View parent)
        {
            super (parent);

            _adapter = adapter;

            parent.setClickable (true);
            parent.setLongClickable (true);

            parent.setOnClickListener (this);
            parent.setOnLongClickListener (this);

            _description = (TextView) parent.findViewById (R.id.tvTripSegmentDescription);
            _mapView = (MapView) parent.findViewById (R.id.mvTripSegmentMap);
            _mapView.onCreate (null);

            _onTripItemClickListener = null;
        }

        public OnTripItemClickListener getOnTripItemClickListener ()
        {
            return _onTripItemClickListener;
        }

        public void setOnTripItemClickListener (OnTripItemClickListener listener)
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
            itemView.setBackground (ContextCompat.getDrawable (_adapter._ctx,
                                                               R.drawable.trip_list_selector_actionmode));

            _adapter.toggleSelection (this.getLayoutPosition ());

            if (_onTripItemClickListener != null) {
                _onTripItemClickListener.onTripItemLongClick (v, this.getAdapterPosition ());
            }

            return false;
        }

        public void bindView (TripSegment tripSegment)
        {
            getDescription ().setText (String.format ("%d", tripSegment.getLocations ()
                                                                       .size ()));

            GoogleMap map;

            // Gets to GoogleMap from the MapView and does initialization stuff
            map = _mapView.getMap ();
            map.getUiSettings ()
               .setMyLocationButtonEnabled (false);
            map.setMyLocationEnabled (true);
        }

        public TextView getDescription ()
        {
            return _description;
        }
    }


}
