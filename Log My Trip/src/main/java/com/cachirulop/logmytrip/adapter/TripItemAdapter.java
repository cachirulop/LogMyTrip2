package com.cachirulop.logmytrip.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class TripItemAdapter extends RecyclerView.Adapter {

    Context _ctx;
    List<Trip> _items;

    SparseBooleanArray _selectedItems;
    private boolean _actionMode;

    public TripItemAdapter(Context ctx, List<Trip> items) {
        _ctx = ctx;
        _items = items;

        _selectedItems = new SparseBooleanArray();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.lv_trips_item, parent, false);

        return new ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Set data into the view.
        Trip t;
        int imgId;

        t = _items.get(position);
        imgId = R.mipmap.ic_trip_status_saved;

        if (SettingsManager.isLogTrip(_ctx)) {
            if (t.equals(TripManager.getCurrentTrip(_ctx))) {
                imgId = R.mipmap.ic_trip_status_saving;
            }
        }

        TripItemAdapter.ViewHolder vh;

        vh = (TripItemAdapter.ViewHolder) holder;

        vh.getStatus().setImageResource(imgId);
        vh.getDescription().setText(t.getDescription());
        vh.getDuration().setText("10:10:10 - 100Km");
        vh.getDate().setText(new SimpleDateFormat("dd/MM/yyyy").format(t.getTripDate()));
        vh.getTime().setText(new SimpleDateFormat("hh:mm:ss").format(t.getTripDate()));

        vh.itemView.setActivated(_selectedItems.get(position, false));

        Drawable background;

        if (_actionMode) {
            background = ContextCompat.getDrawable(_ctx, R.drawable.trip_list_selector_actionmode);
        } else {
            background = ContextCompat.getDrawable(_ctx, R.drawable.trip_list_selector);
        }

        vh.itemView.setBackground(background);
        background.jumpToCurrentState();
    }

    @Override
    public long getItemId(int position) {
        return _items.get(position).getId();
    }

    @Override
    public int getItemCount() {
        if (_items == null) {
            return 0;
        } else {
            return _items.size();
        }
    }

    public void updateTripStatus() {
        Trip current;
        int position;

        current = TripManager.getCurrentTrip(_ctx);
        position = _items.indexOf(current);
        if (position == -1) {
            _items.add(0, current);

            notifyItemInserted(0);
        } else {
            notifyItemChanged(position);
        }
    }

    public void toggleSelection(int pos) {
        if (_selectedItems.get(pos, false)) {
            _selectedItems.delete(pos);
        } else {
            _selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        _selectedItems.clear();

        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return _selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(_selectedItems.size());

        for (int i = 0; i < _selectedItems.size(); i++) {
            items.add(_selectedItems.keyAt(i));
        }

        return items;
    }

    public boolean isActionMode() {
        return _actionMode;
    }

    public void setActionMode(boolean selectionMode) {
        this._actionMode = selectionMode;
/*
        // Update existing holders
        for (int i = 0; i < this.getItemCount(); i++) {
            Drawable background;
            ViewHolder h;

            h = getHolder(i);

            if (h != null) {
                if (_actionMode) {
                    background = ContextCompat.getDrawable(_ctx, R.drawable.trip_list_selector_actionmode);
                } else {
                    background = ContextCompat.getDrawable(_ctx, R.drawable.trip_list_selector);
                }

                h.itemView.setBackground(background);
                background.jumpToCurrentState();

                h.itemView.setActivated(_selectedItems.get(h.getAdapterPosition(), false));
            }
        }
*/
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    // ViewHolder class
    /////////////////////////////////////////////////////////////////////////////////////////////

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView _status;
        private TextView _description;
        private TextView _duration;
        private TextView _date;
        private TextView _time;

        public ViewHolder(View parent) {
            super(parent);

            parent.setClickable(true);
            parent.setLongClickable(true);

            _status = (ImageView) parent.findViewById(R.id.ivTripItemStatus);
            _description = (TextView) parent.findViewById(R.id.tvTripItemDescription);
            _duration = (TextView) parent.findViewById(R.id.tvTripItemDuration);
            _date = (TextView) parent.findViewById(R.id.tvTripItemDate);
            _time = (TextView) parent.findViewById(R.id.tvTripItemDatetime);
        }

        public ImageView getStatus() {
            return _status;
        }

        public TextView getDescription() {
            return _description;
        }

        public TextView getDuration() {
            return _duration;
        }

        public TextView getDate() {
            return _date;
        }

        public TextView getTime() {
            return _time;
        }
    }

}
