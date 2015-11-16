package com.cachirulop.logmytrip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.dialog.CustomViewDialog;
import com.cachirulop.logmytrip.dialog.ListDialog;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.TripDetailFragment;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedTripHolder;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.google.android.gms.maps.GoogleMap;

public class TripDetailActivity
        extends AppCompatActivity
{
    private TripDetailFragment _detailFragment;
    private Trip               _trip;
    private MenuItem           _menuStartStopLog;

    private BroadcastReceiver _onTripLogStopReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateMenuItemState ();
        }
    };

    private BroadcastReceiver _onTripLogStartReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateMenuItemState ();
        }
    };


    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Inflate the view
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_trip_detail);

        _trip = SelectedTripHolder.getInstance ().getSelectedTrip ();

        // Set the fragment content
        if (findViewById (R.id.tripDetailActivityContainer) != null) {
            if (savedInstanceState == null) {
                if (_trip != null) {
                    _detailFragment = new TripDetailFragment ();

                    _detailFragment.setArguments (getIntent ().getExtras ());

                    getSupportFragmentManager ().beginTransaction ()
                                                .add (R.id.tripDetailActivityContainer,
                                                      _detailFragment)
                                                .commit ();
                }
            }
        }
    }

    @Override
    protected void onResume ()
    {
        super.onResume ();

        LogMyTripBroadcastManager.registerTripLogStartReceiver (this, _onTripLogStartReceiver);
        LogMyTripBroadcastManager.registerTripLogStopReceiver (this, _onTripLogStopReceiver);
    }

    @Override
    protected void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (this, _onTripLogStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (this, _onTripLogStopReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        Trip     todayTrip;

        getMenuInflater ().inflate (R.menu.menu_trip_detail, menu);

        todayTrip = TripManager.getTodayTrip (this);
        _menuStartStopLog = menu.findItem (R.id.action_start_stop_log);
        if ((SettingsManager.getCurrentTripId (this) == _trip.getId ()) || (_trip.equals (todayTrip))) {
            _menuStartStopLog.setVisible (true);

            updateMenuItemState ();
        }
        else {
            _menuStartStopLog.setVisible (false);
        }

        return true;
    }

    private void updateMenuItemState ()
    {
        if (_menuStartStopLog != null) {
            if (SettingsManager.isLogTrip (this)) {
                _menuStartStopLog.setTitle (R.string.action_stop_log);
                _menuStartStopLog.setIcon (android.R.drawable.ic_media_pause);
            }
            else {
                _menuStartStopLog.setTitle (R.string.action_start_log);
                _menuStartStopLog.setIcon (android.R.drawable.ic_menu_save);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_start_stop_log:
                if (SettingsManager.isLogTrip (this)) {
                    ServiceManager.stopTripLog (this);
                }
                else {
                    ServiceManager.startTripLog (this);
                }

                updateMenuItemState ();

                return true;

            case R.id.action_map_type:
                selectMapType ();

                return true;

            case R.id.action_edit:
                editTrip ();

                return true;

            default:
                return super.onOptionsItemSelected (item);
        }
    }

    private void selectMapType ()
    {
        ListDialog dlg;

        dlg = new ListDialog (R.string.title_map_type, R.array.map_types,
                              _detailFragment.getMapType ())
        {
            @Override
            public void onSingleItemSelected (int selectedItem)
            {
                int type;

                switch (selectedItem) {
                    case 0:
                        type = GoogleMap.MAP_TYPE_NONE;
                        break;

                    case 1:
                        type = GoogleMap.MAP_TYPE_NORMAL;
                        break;

                    case 2:
                        type = GoogleMap.MAP_TYPE_SATELLITE;
                        break;

                    case 3:
                        type = GoogleMap.MAP_TYPE_TERRAIN;
                        break;

                    case 4:
                        type = GoogleMap.MAP_TYPE_HYBRID;
                        break;
                }

                _detailFragment.setMapType (selectedItem);
            }
        };

        dlg.show (getSupportFragmentManager (), "selectMapType");
    }

    private void editTrip ()
    {
        CustomViewDialog dlg;

        dlg = new CustomViewDialog (R.string.title_edit_trip, R.layout.dialog_edit_trip)
        {
            @Override
            public void bindData (View view)
            {
                EditText txt;

                txt = (EditText) view.findViewById (R.id.etEditTripTitle);
                txt.setText (_trip.getTitle ());

                txt = (EditText) view.findViewById (R.id.etEditTripDescription);
                txt.setText (_trip.getDescription ());
            }

            @Override
            public void onOkClicked (View view)
            {
                EditText txt;

                txt = (EditText) view.findViewById (R.id.etEditTripTitle);
                _trip.setTitle (txt.getText ().toString ());

                txt = (EditText) view.findViewById (R.id.etEditTripDescription);
                _trip.setDescription (txt.getText ().toString ());

                TripManager.updateTrip (TripDetailActivity.this, _trip);

                // TODO: Refresh the trip data
            }
        };

        dlg.show (getSupportFragmentManager (), "editTrip");
    }
}
