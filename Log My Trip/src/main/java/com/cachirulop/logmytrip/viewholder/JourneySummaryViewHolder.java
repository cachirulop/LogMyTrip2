package com.cachirulop.logmytrip.viewholder;

import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.JourneyStatisticsAdapter;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.entity.Location;
import com.cachirulop.logmytrip.helper.FormatHelper;
import com.cachirulop.logmytrip.receiver.AddressResultReceiver;
import com.cachirulop.logmytrip.service.FetchAddressService;

/**
 * Created by dmagro on 06/10/2015.
 */
public class JourneySummaryViewHolder
        extends RecyclerView.ViewHolder
{
    private JourneyStatisticsAdapter _adapter;

    private TextView _description;
    private TableRow _descriptionRow;
    private TextView _locationFrom;
    private TextView _locationTo;
    private TextView _startDate;
    private TextView _endDate;
    private TextView _startTime;
    private TextView _endTime;
    private TextView _totalDistance;
    private TextView _totalTime;
    private TextView _maxSpeed;
    private TextView _mediumSpeed;

    private Context               _ctx;
    private AddressResultReceiver _addressFromReceiver;
    private AddressResultReceiver _addressToReceiver;

    public JourneySummaryViewHolder (JourneyStatisticsAdapter adapter, View parent, Context ctx)
    {
        super (parent);

        _adapter = adapter;
        _ctx = ctx;

        parent.setClickable (false);
        parent.setLongClickable (false);

        _descriptionRow = (TableRow) parent.findViewById (R.id.tvJourneySummaryDescriptionRow);
        _description = (TextView) parent.findViewById (R.id.tvJourneySummaryDescription);

        _locationFrom = (TextView) parent.findViewById (R.id.tvJourneySummaryLocationFrom);
        _locationTo = (TextView) parent.findViewById (R.id.tvJourneySummaryLocationTo);

        _addressFromReceiver = new AddressResultReceiver (new Handler (), getLocationFrom ());
        _addressToReceiver = new AddressResultReceiver (new Handler (), getLocationTo ());

        _startDate = (TextView) parent.findViewById (R.id.tvJourneySummaryStartDate);
        _endDate = (TextView) parent.findViewById (R.id.tvJourneySummaryEndDate);
        _startTime = (TextView) parent.findViewById (R.id.tvJourneySummaryStartTime);
        _endTime = (TextView) parent.findViewById (R.id.tvJourneySummaryEndTime);
        _totalDistance = (TextView) parent.findViewById (R.id.tvJourneySummaryTotalDistance);
        _totalTime = (TextView) parent.findViewById (R.id.tvJourneySummaryTotalTime);
        _maxSpeed = (TextView) parent.findViewById (R.id.tvJourneySummaryMaxSpeed);
        _mediumSpeed = (TextView) parent.findViewById (R.id.tvJourneySummaryMediumSpeed);
    }

    public TextView getLocationFrom ()
    {
        return _locationFrom;
    }

    public TextView getLocationTo ()
    {
        return _locationTo;
    }

    public void setLocationTo (TextView locationTo)
    {
        _locationTo = locationTo;
    }

    public void setLocationFrom (TextView locationFrom)
    {
        _locationFrom = locationFrom;
    }

    public void bindView (Journey journey, int position)
    {
        Location l;

        if (journey.getDescription () != null && !"".equals (journey.getDescription ())) {
            _descriptionRow.setVisibility (View.VISIBLE);
            _description.setText (journey.getDescription ());
        }
        else {
            _descriptionRow.setVisibility (View.GONE);
        }

        getStartDate ().setText (FormatHelper.formatDate (_ctx, journey.getJouneyDate ()));
        getStartTime ().setText (FormatHelper.formatTime (_ctx, journey.getJouneyDate ()));

        l = journey.getStartLocation ();
        if (l != null) {
            if (getLocationFrom ().getText ().equals ("")) {
                getLocationFrom ().setText (l.toString ());
            }

            FetchAddressService.startService (_ctx,
                                              new AddressResultReceiver (new Handler (),
                                                                         getLocationFrom ()),
                                              l.toLocation ());
        }
        else {
            getLocationFrom ().setText ("");
        }

        l = journey.getEndLocation ();
        if (l != null) {
            if (getLocationTo ().getText ().equals ("")) {
                getLocationTo ().setText (l.toString ());
            }
            getEndDate ().setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            getEndTime ().setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx,
                                              new AddressResultReceiver (new Handler (),
                                                                         getLocationTo ()),
                                              l.toLocation ());
        }
        else {
            getLocationTo ().setText ("");
            getEndDate ().setText ("");
            getEndTime ().setText ("");
        }

        getTotalDistance ().setText (FormatHelper.formatDistance (journey.computeTotalDistance (_ctx)));
        getTotalTime ().setText (FormatHelper.formatDuration (journey.computeTotalTime (_ctx)));
        getMaxSpeed ().setText (FormatHelper.formatSpeed (journey.computeMaxSpeed ()));
        getMediumSpeed ().setText (FormatHelper.formatSpeed (journey.computeMediumSpeed ()));
    }

    public TextView getStartDate ()
    {
        return _startDate;
    }

    public TextView getStartTime ()
    {
        return _startTime;
    }

    public TextView getEndDate ()
    {
        return _endDate;
    }

    public void setEndDate (TextView endDate)
    {
        _endDate = endDate;
    }

    public TextView getEndTime ()
    {
        return _endTime;
    }

    public void setEndTime (TextView endTime)
    {
        _endTime = endTime;
    }

    public TextView getTotalDistance ()
    {
        return _totalDistance;
    }

    public void setTotalDistance (TextView totalDistance)
    {
        _totalDistance = totalDistance;
    }

    public TextView getTotalTime ()
    {
        return _totalTime;
    }

    public TextView getMaxSpeed ()
    {
        return _maxSpeed;
    }

    public void setMaxSpeed (TextView maxSpeed)
    {
        _maxSpeed = maxSpeed;
    }

    public TextView getMediumSpeed ()
    {
        return _mediumSpeed;
    }

    public void setMediumSpeed (TextView mediumSpeed)
    {
        _mediumSpeed = mediumSpeed;
    }

    public void setTotalTime (TextView totalTime)
    {
        _totalTime = totalTime;
    }

    public void setStartTime (TextView startTime)
    {
        _startTime = startTime;
    }

    public void setStartDate (TextView startDate)
    {
        _startDate = startDate;
    }

    public JourneyStatisticsAdapter getAdapter ()
    {
        return _adapter;
    }

    public void setAdapter (JourneyStatisticsAdapter adapter)
    {
        _adapter = adapter;
    }

    public AddressResultReceiver getAddressFromReceiver ()
    {
        return _addressFromReceiver;
    }

    public void setAddressFromReceiver (AddressResultReceiver addressFromReceiver)
    {
        _addressFromReceiver = addressFromReceiver;
    }

    public AddressResultReceiver getAddressToReceiver ()
    {
        return _addressToReceiver;
    }

    public void setAddressToReceiver (AddressResultReceiver addressToReceiver)
    {
        _addressToReceiver = addressToReceiver;
    }

    public Context getCtx ()
    {
        return _ctx;
    }

    public void setCtx (Context ctx)
    {
        _ctx = ctx;
    }

}