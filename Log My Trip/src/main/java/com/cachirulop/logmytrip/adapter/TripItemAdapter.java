package com.cachirulop.logmytrip.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.RecyclerViewItemClickListener;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.viewholder.TripItemViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripItemAdapter
        extends RecyclerView.Adapter
{

    private Context    _ctx;
    private List<Trip> _items;

    private SparseBooleanArray            _selectedItems;
    private boolean                       _actionMode;
    private RecyclerViewItemClickListener _onTripItemClickListener;
    private TripItemAdapterListener _listener = null;

    public TripItemAdapter (Context ctx, TripItemAdapterListener listener)
    {
        _ctx = ctx;
        _listener = listener;

        loadTrips ();

        _onTripItemClickListener = null;

        _selectedItems = new SparseBooleanArray ();
    }

    public void loadTrips ()
    {
        final ProgressDialog progDialog;

        progDialog = ProgressDialog.show (_ctx,
                                          _ctx.getString (R.string.msg_loading_trips),
                                          null,
                                          true);

        new Thread ()
        {
            public void run ()
            {
                _items = TripManager.loadTrips (_ctx);

                onTripLoadedMainThread ();

                progDialog.dismiss ();
            }
        }.start ();
    }

    private void onTripLoadedMainThread ()
    {
        Handler  main;
        Runnable runInMain;

        main = new Handler (_ctx.getMainLooper ());

        runInMain = new Runnable ()
        {
            @Override
            public void run ()
            {
                notifyDataSetChanged ();

                if (_listener != null) {
                    _listener.onTripListLoaded ();
                }
            }
        };

        main.post (runInMain);
    }

    public RecyclerViewItemClickListener getOnTripItemClickListener ()
    {
        return _onTripItemClickListener;
    }

    public void setOnTripItemClickListener (RecyclerViewItemClickListener listener)
    {
        _onTripItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate (R.layout.triplist_item, parent, false);

        return new TripItemViewHolder (_ctx, this, rowView);
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        TripItemViewHolder vh;

        vh = (TripItemViewHolder) holder;

        // Drawable background;
        int background;

        if (_actionMode && isSelected (vh.getLayoutPosition ())) {
            background = R.color.default_background;
        }
        else {
            background = R.color.cardview_light_background;
        }

        // Set data into the view.
        vh.bindView (_items.get (position),
                     _selectedItems.get (position, false),
                     background,
                     _onTripItemClickListener);
    }

    public boolean isSelected (int pos)
    {
        return _selectedItems.get (pos, false);
    }

    @Override
    public long getItemId (int position)
    {
        return _items.get (position).getId ();
    }

    @Override
    public int getItemCount ()
    {
        if (_items == null) {
            return 0;
        }
        else {
            return _items.size ();
        }
    }

    public void startTripLog ()
    {
        Trip current;
        int position;

        current = TripManager.getActiveTrip (_ctx);
        if (current != null && _items != null) {
            position = _items.indexOf (current);
            if (position == -1) {
                _items.add (0, current);

                notifyItemInserted (0);
            }
            else {
                notifyItemChanged (position);
            }
        }
    }

    public void stopTripLog (Trip trip)
    {
        int position;

        position = _items.indexOf (trip);
        if (position != -1) {
            notifyItemChanged (position);
        }
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

    public void clearSelections ()
    {
        _selectedItems.clear ();

        notifyDataSetChanged ();
    }

    public int getSelectedItemCount ()
    {
        return _selectedItems.size ();
    }

    public List<Trip> getSelectedItems ()
    {
        List<Trip> result;

        result = new ArrayList<Trip> (_selectedItems.size ());

        for (int i = 0 ; i < _selectedItems.size () ; i++) {
            result.add (_items.get (_selectedItems.keyAt (i)));
        }

        return result;
    }

    public boolean isActionMode ()
    {
        return _actionMode;
    }

    public void setActionMode (boolean selectionMode)
    {
        this._actionMode = selectionMode;
        this.notifyDataSetChanged ();
    }

    public void removeItem (Trip t)
    {
        int pos;

        pos = _items.indexOf (t);
        if (pos != -1) {
            _items.remove (t);
            notifyItemChanged (pos);
        }
    }

    public void setItem (int position, Trip value)
    {
        _items.set (position, value);
    }

    public Trip getItem (int position)
    {
        return _items.get (position);
    }

    public void reloadTrips ()
    {
        if (_items != null) {
            _items.clear ();
        }

        loadTrips ();
    }

    public void clearTrips ()
    {
        if (_items != null) {
            _items.clear ();
        }
    }

    public interface TripItemAdapterListener
    {
        void onTripListLoaded ();
    }
}
