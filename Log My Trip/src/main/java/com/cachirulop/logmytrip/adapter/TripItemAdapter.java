package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.util.FormatHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripItemAdapter
        extends RecyclerView.Adapter
{

    Context _ctx;
    List<Trip> _items;

    private SparseBooleanArray _selectedItems;
    private boolean            _actionMode;
    private OnTripItemClickListener _onTripItemClickListener;

    public TripItemAdapter (Context ctx)
    {
        _ctx = ctx;
        _items = TripManager.LoadTrips (_ctx);

        _onTripItemClickListener = null;

        _selectedItems = new SparseBooleanArray ();
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
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (
                Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate (R.layout.triplist_item, parent, false);

        return new ViewHolder (_ctx, this, rowView);
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        // Set data into the view.
        Trip t;
        int imgId;

        t = _items.get (position);
        imgId = R.mipmap.ic_trip_status_saved;

        if (SettingsManager.isLogTrip (_ctx)) {
            Trip active;

            active = TripManager.getActiveTrip (_ctx);
            if (active != null && t.equals (active)) {
                imgId = R.mipmap.ic_trip_status_logging;
            }
        }

        TripItemAdapter.ViewHolder vh;

        vh = (TripItemAdapter.ViewHolder) holder;

        vh.getStatus ()
          .setImageResource (imgId);
        vh.getDescription ()
          .setText (t.getDescription ());
        vh.getDuration ()
          .setText (String.format ("%s - %s", FormatHelper.formatDuration (t.computeTotalTime ()),
                                   FormatHelper.formatDistance (t.computeTotalDistance ())));
        vh.getDate ()
          .setText (FormatHelper.formatDate (_ctx, t.getTripDate ()));
        vh.getTime ()
          .setText (FormatHelper.formatTime (_ctx, t.getTripDate ()));

        vh.setOnTripItemClickListener (_onTripItemClickListener);

        vh.itemView.setActivated (_selectedItems.get (position, false));

        // Drawable background;
        int background;

        if (_actionMode && isSelected (vh.getLayoutPosition ())) {
            background = R.color.default_background;
        }
        else {
            background = R.color.cardview_light_background;
        }

        ((CardView) vh.itemView).setCardBackgroundColor (_ctx.getResources ()
                                                             .getColor (background));
        // vh.getContainer ().setBackground (background);
        //background.jumpToCurrentState ();
    }

    public boolean isSelected (int pos)
    {
        return _selectedItems.get (pos, false);
    }

    @Override
    public long getItemId (int position)
    {
        return _items.get (position)
                     .getId ();
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
        if (current != null) {
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

    public Trip getItem (int position)
    {
        return _items.get (position);
    }

    public void reloadTrips ()
    {
        _items.clear ();
        _items = TripManager.LoadTrips (_ctx);
        notifyDataSetChanged ();
    }

    public interface OnTripItemClickListener
    {
        void onTripItemLongClick (View v, int position);

        void onTripItemClick (View v, int position);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////
    // ViewHolder class
    /////////////////////////////////////////////////////////////////////////////////////////////

    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                       View.OnLongClickListener
    {
        private Context        _ctx;
        private TripItemAdapter _adapter;
        private ImageView _status;
        private TextView  _description;
        private TextView  _duration;
        private TextView  _date;
        private TextView  _time;
        private RelativeLayout _container;

        private OnTripItemClickListener _onTripItemClickListener;

        public ViewHolder (Context ctx, TripItemAdapter adapter, View parent)
        {
            super (parent);

            _ctx = ctx;
            _adapter = adapter;

            parent.setClickable (true);
            parent.setLongClickable (true);

            parent.setOnClickListener (this);
            parent.setOnLongClickListener (this);

            _container = (RelativeLayout) parent.findViewById (R.id.tripListItemContainer);
            _status = (ImageView) parent.findViewById (R.id.ivTripItemStatus);
            _description = (TextView) parent.findViewById (R.id.tvTripItemDescription);
            _duration = (TextView) parent.findViewById (R.id.tvTripItemDuration);
            _date = (TextView) parent.findViewById (R.id.tvTripItemDate);
            _time = (TextView) parent.findViewById (R.id.tvTripItemDatetime);

            _onTripItemClickListener = null;

            _container.setClickable (true);
            _container.setLongClickable (true);

            _container.setOnClickListener (this);
            _container.setOnLongClickListener (this);
        }

        public View getContainer ()
        {
            return _container;
        }

        public ImageView getStatus ()
        {
            return _status;
        }

        public TextView getDescription ()
        {
            return _description;
        }

        public TextView getDuration ()
        {
            return _duration;
        }

        public TextView getDate ()
        {
            return _date;
        }

        public TextView getTime ()
        {
            return _time;
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
            //            getContainer ().setBackground (ContextCompat.getDrawable (_adapter._ctx,
            //                                                                      R.drawable.trip_list_selector_actionmode));
            _adapter.toggleSelection (this.getLayoutPosition ());

            if (_onTripItemClickListener != null) {
                _onTripItemClickListener.onTripItemLongClick (v, this.getAdapterPosition ());
            }

            return false;
        }
    }

}
