package com.cachirulop.logmytrip.viewholder;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.JourneyStatisticsAdapter;
import com.cachirulop.logmytrip.dialog.ConfirmDialog;
import com.cachirulop.logmytrip.entity.JourneySegment;
import com.cachirulop.logmytrip.entity.Location;
import com.cachirulop.logmytrip.helper.FormatHelper;
import com.cachirulop.logmytrip.helper.MapHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.receiver.AddressResultReceiver;
import com.cachirulop.logmytrip.service.FetchAddressService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class JourneySegmentViewHolder
        extends RecyclerView.ViewHolder
{
    private JourneyStatisticsAdapter _adapter;

    private Toolbar _toolbar;
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

    private FrameLayout    _mapFrame;
    private MapView        _mapView;
    private MapHelper      _mapHelper;
    private MapReadyCallback _mapCallback;
    private JourneySegment _segment;
    private Context        _ctx;
    private int            _mapType;

    public JourneySegmentViewHolder (JourneyStatisticsAdapter adapter,
                                     View parent,
                                     Context ctx,
                                     int mapType)
    {
        super (parent);

        _adapter = adapter;
        _ctx = ctx;
        _mapType = mapType;

        parent.setClickable (false);
        parent.setLongClickable (false);

        GoogleMapOptions options = new GoogleMapOptions ();

        options.liteMode (true);

        _mapHelper = new MapHelper (_ctx);

        _mapView = new MapView (_ctx, options);
        _mapView.onCreate (null);
        _mapView.setClickable (false);

        _mapCallback = new MapReadyCallback ();

        _mapFrame = (FrameLayout) parent.findViewById (R.id.flMapSegment);
        _mapFrame.setClickable (false);

        _mapFrame.addView (_mapView);

        _locationFrom = (TextView) parent.findViewById (R.id.tvJourneySegmentLocationFrom);
        _locationTo = (TextView) parent.findViewById (R.id.tvJourneySegmentLocationTo);

        _startDate = (TextView) parent.findViewById (R.id.tvJourneySegmentStartDate);
        _endDate = (TextView) parent.findViewById (R.id.tvJourneySegmentEndDate);
        _startTime = (TextView) parent.findViewById (R.id.tvJourneySegmentStartTime);
        _endTime = (TextView) parent.findViewById (R.id.tvJourneySegmentEndTime);
        _totalDistance = (TextView) parent.findViewById (R.id.tvJourneySegmentTotalDistance);
        _totalTime = (TextView) parent.findViewById (R.id.tvJourneySegmentTotalTime);
        _maxSpeed = (TextView) parent.findViewById (R.id.tvJourneySegmentMaxSpeed);
        _mediumSpeed = (TextView) parent.findViewById (R.id.tvJourneySegmentMediumSpeed);

        _toolbar = (Toolbar) parent.findViewById (R.id.tbSegmentToolbar);
        _toolbar.inflateMenu (R.menu.menu_segment_actionmode);
    }

    public void bindView (final JourneySegment journeySegment, int position)
    {
        Location l;

        _segment = journeySegment;  // To get locations

        _toolbar.setTitle (_segment.getTitle (_ctx));
        _toolbar.setOnMenuItemClickListener (new Toolbar.OnMenuItemClickListener ()
        {
            @Override
            public boolean onMenuItemClick (MenuItem item)
            {
                switch (item.getItemId ()) {
                    case R.id.action_delete_segment:
                        ConfirmDialog dlg;

                        dlg = new ConfirmDialog ();
                        dlg.setTitleId (R.string.title_delete);
                        dlg.setMessageId (R.string.msg_delete_confirm);
                        dlg.setListener (new ConfirmDialog.OnConfirmDialogListener ()
                        {
                            @Override
                            public void onPositiveButtonClick ()
                            {
                                _adapter.removeItem (journeySegment);
                                JourneyManager.deleteSegment (_ctx, journeySegment);
                            }

                            @Override
                            public void onNegativeButtonClick ()
                            {
                                // Do nothing
                            }
                        });

                        dlg.show (((FragmentActivity) _ctx).getSupportFragmentManager (),
                                  "deleteJourney");
                }

                return true;
            }
        });

        l = _segment.getStartLocation ();
        if (l != null) {
            getLocationFrom ().setText (l.toString ());
            getStartDate ().setText (FormatHelper.formatDate (_ctx, l.getLocationTimeAsDate ()));
            getStartTime ().setText (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));

            FetchAddressService.startService (_ctx,
                                              new AddressResultReceiver (new Handler (),
                                                                         getLocationFrom ()),
                                              l.toLocation ());
        }
        else {
            getLocationFrom ().setText ("");
            getStartDate ().setText ("");
            getStartTime ().setText ("");
        }

        l = _segment.getEndLocation ();
        if (l != null) {
            getLocationTo ().setText (l.toString ());
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

        getTotalDistance ().setText (FormatHelper.formatDistance (_segment.computeTotalDistance ()));
        getTotalTime ().setText (FormatHelper.formatDuration (_segment.computeTotalTime ()));
        getMaxSpeed ().setText (FormatHelper.formatSpeed (_segment.computeMaxSpeed ()));
        getMediumSpeed ().setText (FormatHelper.formatSpeed (_segment.computeMediumSpeed ()));

        _mapView.getMapAsync (_mapCallback);
    }

    public TextView getLocationFrom ()
    {
        return _locationFrom;
    }

    public TextView getStartDate ()
    {
        return _startDate;
    }

    public TextView getStartTime ()
    {
        return _startTime;
    }

    public TextView getLocationTo ()
    {
        return _locationTo;
    }

    public TextView getEndDate ()
    {
        return _endDate;
    }

    public TextView getEndTime ()
    {
        return _endTime;
    }

    public TextView getTotalDistance ()
    {
        return _totalDistance;
    }

    public TextView getTotalTime ()
    {
        return _totalTime;
    }

    public TextView getMaxSpeed ()
    {
        return _maxSpeed;
    }

    public TextView getMediumSpeed ()
    {
        return _mediumSpeed;
    }

    public Context getCtx ()
    {
        return _ctx;
    }

    public void setCtx (Context ctx)
    {
        _ctx = ctx;
    }

    private class MapReadyCallback
            implements OnMapReadyCallback
    {
        @Override
        public void onMapReady (GoogleMap googleMap)
        {
            googleMap.setMapType (_mapType);
            googleMap.clear ();
            googleMap.setOnMapClickListener (new GoogleMap.OnMapClickListener ()
            {
                @Override
                public void onMapClick (LatLng latLng)
                {
                    // Disable map click
                }
            });

            _mapHelper.setMap (googleMap);
            _mapHelper.drawSegment (_segment);
        }
    }
}
