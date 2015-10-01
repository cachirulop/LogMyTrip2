package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
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

    public TripStatisticsAdapter (Context ctx, Fragment parentFragment, Trip trip)
    {
        _ctx = ctx;
        _trip = trip;
        _parentFragment = parentFragment;

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
        switch (getItemViewType (position)) {
            case ITEM_TYPE_TRIP:
                ((TripStatisticsAdapter.TripSummaryViewHolder) holder).bindView (_ctx,
                                                                                 _parentFragment,
                                                                                 _trip, position);
                break;

            case ITEM_TYPE_SEGMENT:
                ((TripStatisticsAdapter.TripSegmentViewHolder) holder).bindView (_ctx,
                                                                                 _parentFragment,
                        _trip.getSegments ()
                             .get (position - 1), position);
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
        private boolean _bound = false;

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

        public void bindView (Context ctx, Fragment parentFragment, Trip trip, int position)
        {
            if (!_bound) {
                getDescription ().setText (trip.getDescription ());
            }
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
        private FrameLayout _mapFrame;
        private TripSegment _segment;
        private boolean _bound = false;

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
            _mapFrame = (FrameLayout) parent.findViewById (R.id.flMapSegment);

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

        public void bindView (Context ctx, Fragment parentFragment, TripSegment tripSegment, int position)
        {
            if (!_bound) {
                _bound = true;

                _segment = tripSegment;
                getDescription ().setText (String.format ("%d", tripSegment.getLocations ()
                                                                           .size ()));

    /*
                FrameLayout frame; //  = new FrameLayout(mContext);

                frame = new FrameLayout (ctx);
                frame.setId (10000 * position); //you have to set unique id

                int height = (int) TypedValue.applyDimension (TypedValue.COMPLEX_UNIT_DIP, 170,
                                                              ctx.getResources ()
                                                                 .getDisplayMetrics ());
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, height);
                frame.setLayoutParams(layoutParams);

                _mapFrame.addView (frame);

                GoogleMapOptions options = new GoogleMapOptions();
                options.liteMode (true);
                SupportMapFragment mapFrag = SupportMapFragment.newInstance (options);

                //Create the the class that implements OnMapReadyCallback and set up your map
                mapFrag.getMapAsync (new MapReadyCallback ());

                FragmentManager fm =  parentFragment.getChildFragmentManager ();
                fm.beginTransaction().add(frame.getId(), mapFrag).commit();
    */
                GoogleMapOptions options = new GoogleMapOptions ();
                options.liteMode (true);
                // SupportMapFragment mapFrag = SupportMapFragment.newInstance (options);
                MapView mapView;

                mapView = new MapView (ctx, options);
                mapView.onCreate (null);
                mapView.getMapAsync (new MapReadyCallback ());

                //Create the the class that implements OnMapReadyCallback and set up your map
                // mapFrag.getMapAsync (new MapReadyCallback ());

                _mapFrame.addView (mapView);

                Log.d (TripStatisticsAdapter.class.getCanonicalName (),
                       "*************** view binded");

                // FragmentManager fm =  parentFragment.getChildFragmentManager ();
                // fm.beginTransaction().add(frame.getId(), mapFrag).commit();
            }

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
}
