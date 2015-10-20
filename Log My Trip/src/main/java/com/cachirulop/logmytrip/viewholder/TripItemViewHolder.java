package com.cachirulop.logmytrip.viewholder;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.RecyclerViewItemClickListener;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.util.FormatHelper;

/**
 * Created by dmagro on 19/10/2015.
 */
public class TripItemViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener
{
    private Context         _ctx;
    private TripItemAdapter _adapter;
    private ImageView       _status;
    private TextView        _title;
    private TextView        _description;
    private TextView        _duration;
    private TextView        _date;
    private TextView        _time;
    private RelativeLayout  _container;

    private RecyclerViewItemClickListener _onTripItemClickListener;

    public TripItemViewHolder (Context ctx, TripItemAdapter adapter, View parent)
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
        _title = (TextView) parent.findViewById (R.id.tvTripItemTitle);
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

    public RecyclerViewItemClickListener getOnTripItemClickListener ()
    {
        return _onTripItemClickListener;
    }

    public void setOnTripItemClickListener (RecyclerViewItemClickListener listener)
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
            _onTripItemClickListener.onRecyclerViewItemClick (v, this.getAdapterPosition ());
        }
    }

    @Override
    public boolean onLongClick (View v)
    {
        _adapter.toggleSelection (this.getLayoutPosition ());

        if (_onTripItemClickListener != null) {
            _onTripItemClickListener.onRecyclerViewItemLongClick (v, this.getAdapterPosition ());
        }

        return false;
    }

    public void bindView (Trip trip, boolean activated, int background, RecyclerViewItemClickListener listener)
    {
        int imgId;

        imgId = R.mipmap.ic_trip_status_saved;

        if (SettingsManager.isLogTrip (_ctx)) {
            Trip active;

            active = TripManager.getActiveTrip (_ctx);
            if (active != null && trip.equals (active)) {
                imgId = R.mipmap.ic_trip_status_logging;
            }
        }

        getStatus ().setImageResource (imgId);
        getTitle ().setText (trip.getTitle ());

        if (trip.getDescription () != null) {
            getDescription ().setVisibility (View.VISIBLE);
            getDescription ().setText (trip.getDescription ());
        }
        else {
            getDescription ().setVisibility (View.GONE);
        }

        getDuration ().setText (
                String.format ("%s - %s", FormatHelper.formatDuration (trip.computeTotalTime ()),
                               FormatHelper.formatDistance (trip.computeTotalDistance ())));
        getDate ().setText (FormatHelper.formatDate (_ctx, trip.getTripDate ()));
        getTime ().setText (FormatHelper.formatTime (_ctx, trip.getTripDate ()));

        setOnTripItemClickListener (listener);

        itemView.setActivated (activated);

        ((CardView) itemView).setCardBackgroundColor (_ctx.getResources ()
                                                          .getColor (background));
    }

    public ImageView getStatus ()
    {
        return _status;
    }

    public TextView getTitle ()
    {
        return _title;
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

}
