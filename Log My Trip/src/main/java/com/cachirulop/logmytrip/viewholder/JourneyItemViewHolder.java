package com.cachirulop.logmytrip.viewholder;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.JourneyItemAdapter;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.fragment.RecyclerViewItemClickListener;
import com.cachirulop.logmytrip.helper.FormatHelper;
import com.cachirulop.logmytrip.manager.SettingsManager;

/**
 * Created by dmagro on 19/10/2015.
 */
public class JourneyItemViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener,
                   View.OnLongClickListener
{
    private Context            _ctx;
    private JourneyItemAdapter _adapter;
    private ImageView          _status;
    private TextView           _title;
    private TextView           _description;
    private TextView           _duration;
    private TextView           _date;
    private TextView           _time;
    private RelativeLayout     _container;

    private RecyclerViewItemClickListener _onJourneyItemClickListener;

    public JourneyItemViewHolder (Context ctx, JourneyItemAdapter adapter, View parent)
    {
        super (parent);

        _ctx = ctx;
        _adapter = adapter;

        parent.setClickable (true);
        parent.setLongClickable (true);

        parent.setOnClickListener (this);
        parent.setOnLongClickListener (this);

        _container = (RelativeLayout) parent.findViewById (R.id.journeyListItemContainer);
        _status = (ImageView) parent.findViewById (R.id.ivJourneyItemStatus);
        _title = (TextView) parent.findViewById (R.id.tvJourneyItemTitle);
        _description = (TextView) parent.findViewById (R.id.tvJourneyItemDescription);
        _duration = (TextView) parent.findViewById (R.id.tvJourneyItemDuration);
        _date = (TextView) parent.findViewById (R.id.tvJourneyItemDate);
        _time = (TextView) parent.findViewById (R.id.tvJourneyItemDatetime);

        _onJourneyItemClickListener = null;

        _container.setClickable (true);
        _container.setLongClickable (true);

        _container.setOnClickListener (this);
        _container.setOnLongClickListener (this);
    }

    public View getContainer ()
    {
        return _container;
    }

    public RecyclerViewItemClickListener getOnJourneyItemClickListener ()
    {
        return _onJourneyItemClickListener;
    }

    public void setOnJourneyItemClickListener (RecyclerViewItemClickListener listener)
    {
        _onJourneyItemClickListener = listener;
    }

    @Override
    public void onClick (View v)
    {
        if (_adapter.isActionMode ()) {
            _adapter.toggleSelection (this.getLayoutPosition ());
        }

        if (_onJourneyItemClickListener != null) {
            _onJourneyItemClickListener.onRecyclerViewItemClick (v, this.getAdapterPosition ());
        }
    }

    @Override
    public boolean onLongClick (View v)
    {
        _adapter.toggleSelection (this.getLayoutPosition ());

        if (_onJourneyItemClickListener != null) {
            _onJourneyItemClickListener.onRecyclerViewItemLongClick (v, this.getAdapterPosition ());
        }

        return false;
    }

    public void bindView (Journey journey,
                          boolean activated,
                          int background,
                          RecyclerViewItemClickListener listener)
    {
        int imgId;

        imgId = R.mipmap.ic_journey_status_saved;

        if (SettingsManager.isLogJourney (_ctx)) {
            if (journey.getId () == SettingsManager.getCurrentJourneyId (_ctx)) {
                imgId = R.mipmap.ic_status_logging;
            }
        }

        getStatus ().setImageResource (imgId);
        getTitle ().setText (journey.getTitle ());

        if (journey.getDescription () != null && !"".equals (journey.getDescription ())) {
            getDescription ().setVisibility (View.VISIBLE);
            getDescription ().setText (journey.getDescription ());
        }
        else {
            getDescription ().setVisibility (View.GONE);
        }

        getDuration ().setText (journey.getSummary (_ctx));
        getDate ().setText (FormatHelper.formatDate (_ctx, journey.getJouneyDate ()));
        getTime ().setText (FormatHelper.formatTime (_ctx, journey.getJouneyDate ()));

        setOnJourneyItemClickListener (listener);

        itemView.setActivated (activated);

        ((CardView) itemView).setCardBackgroundColor (_ctx.getResources ().getColor (background));
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
