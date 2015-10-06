package com.cachirulop.logmytrip.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.receiver.AddressResultReceiver;
import com.cachirulop.logmytrip.service.FetchAddressService;

/**
 * Created by dmagro on 06/10/2015.
 */
public class TripSummaryViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener
{
    private TripStatisticsAdapter _adapter;
    private TextView              _description;
    private TextView              _locationFrom;
    private TextView              _locationTo;
    private Context               _ctx;
    private AddressResultReceiver _addressFromReceiver;
    private AddressResultReceiver _addressToReceiver;

    private TripStatisticsAdapter.OnTripItemClickListener _onTripItemClickListener;

    public TripSummaryViewHolder (TripStatisticsAdapter adapter, View parent, Context ctx)
    {
        super (parent);

        _adapter = adapter;
        _ctx = ctx;

        parent.setClickable (true);
        parent.setLongClickable (true);

        parent.setOnClickListener (this);
        parent.setOnLongClickListener (this);

        _description = (TextView) parent.findViewById (R.id.tvTripSummaryDescription);
        _locationFrom = (TextView) parent.findViewById (R.id.tvTripSummaryLocationFrom);
        _locationTo = (TextView) parent.findViewById (R.id.tvTripSummaryLocationTo);

        _addressFromReceiver = new AddressResultReceiver (new Handler (), getLocationFrom ());
        _addressToReceiver = new AddressResultReceiver (new Handler (), getLocationTo ());

        _onTripItemClickListener = null;
    }

    public TextView getLocationFrom ()
    {
        return _locationFrom;
    }

    public TextView getLocationTo ()
    {
        return _locationTo;
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

    public void bindView (Fragment parentFragment, Trip trip, int position)
    {
        getDescription ().setText (trip.getDescription ());

        TripLocation l;

        l = trip.getStartLocation ();
        if (l != null) {
            startAddressService (_addressFromReceiver, l.toLocation ());
        }

        l = trip.getEndLocation ();
        if (l != null) {
            startAddressService (_addressToReceiver, l.toLocation ());
        }
    }

    public TextView getDescription ()
    {
        return _description;
    }

    private void startAddressService (AddressResultReceiver receiver, Location location)
    {
        Intent intent = new Intent (_ctx, FetchAddressService.class);

        intent.putExtra (FetchAddressService.RECEIVER, receiver);
        intent.putExtra (FetchAddressService.LOCATION, location);

        _ctx.startService (intent);
    }
}