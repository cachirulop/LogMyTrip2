package com.cachirulop.logmytrip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.dialog.CustomViewDialog;
import com.cachirulop.logmytrip.dialog.ListDialog;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.fragment.JourneyDetailFragment;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.manager.SelectedJourneyHolder;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.google.android.gms.maps.GoogleMap;

public class JourneyDetailActivity
        extends AppCompatActivity
{
    private JourneyDetailFragment _detailFragment;
    private Journey               _journey;
    private MenuItem              _menuStartStopLog;

    private BroadcastReceiver _onLogStopReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateMenuItemState ();
        }
    };

    private BroadcastReceiver _onLogStartReceiver = new BroadcastReceiver ()
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
        super.onCreate (savedInstanceState);

        // Inflate the view
        setContentView (R.layout.activity_journey_detail);

        _journey = SelectedJourneyHolder.getInstance ().getSelectedJourney ();

        // Set the fragment content
        if (findViewById (R.id.journeyDetailActivityContainer) != null) {
            if (savedInstanceState == null) {
                if (_journey != null) {
                    _detailFragment = new JourneyDetailFragment ();

                    _detailFragment.setArguments (getIntent ().getExtras ());

                    getSupportFragmentManager ().beginTransaction ()
                                                .add (R.id.journeyDetailActivityContainer,
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

        LogMyTripBroadcastManager.registerLogStartReceiver (this, _onLogStartReceiver);
        LogMyTripBroadcastManager.registerLogStopReceiver (this, _onLogStopReceiver);
    }

    @Override
    protected void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (this, _onLogStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (this, _onLogStopReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        Journey todayJourney;

        getMenuInflater ().inflate (R.menu.menu_journey_detail, menu);

        todayJourney = JourneyManager.getTodayJourney (this);
        _menuStartStopLog = menu.findItem (R.id.action_start_stop_log);
        if ((SettingsManager.getCurrentJourneyId (this) == _journey.getId ()) || (_journey.equals (
                todayJourney))) {
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
            if (SettingsManager.isLogJourney (this)) {
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
                startStopLogJourney ();

                return true;

            case R.id.action_map_type:
                selectMapType ();

                return true;

            case R.id.action_edit:
                editJourney ();

                return true;

            default:
                return super.onOptionsItemSelected (item);
        }
    }

    private void startStopLogJourney ()
    {
        if (SettingsManager.isLogJourney (this)) {
            ServiceManager.stopLog (this);
        }
        else {
            ServiceManager.startLog (this);
        }

        updateMenuItemState ();
    }

    private void selectMapType ()
    {
        ListDialog dlg;

        dlg = new ListDialog ();
        dlg.setTitleId (R.string.title_map_type);
        dlg.setArrayId (R.array.map_types);
        dlg.setDefaultItem (_detailFragment.getMapType ());
        dlg.setSingleChoice (true);
        dlg.setListener (new ListDialog.OnListDialogListener ()
        {
            @Override
            public void onNegativeButtonClick ()
            {
                // do nothing
            }

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

            @Override
            public void onMultipleItemSelected (boolean[] selectedItems)
            {
                // Do nothing, is single choice
            }
        });

        dlg.show (getSupportFragmentManager (), "selectMapType");
    }

    private void editJourney ()
    {
        final CustomViewDialog dlg;

        dlg = new CustomViewDialog ();
        dlg.setTitleId (R.string.title_edit_journey);
        dlg.setMessageId (R.layout.dialog_edit_journey);
        dlg.setListener (new CustomViewDialog.OnCustomDialogListener ()
        {
            @Override
            public void onPositiveButtonClick ()
            {
                EditText txt;
                View view;

                view = dlg.getCustomView ();

                txt = (EditText) view.findViewById (R.id.etEditJourneyTitle);
                _journey.setTitle (txt.getText ().toString ());

                txt = (EditText) view.findViewById (R.id.etEditJourneyDescription);
                _journey.setDescription (txt.getText ().toString ());

                JourneyManager.updateJourney (JourneyDetailActivity.this, _journey);

                _detailFragment.setToolbarTitle (_journey.getTitle ());

                if (SettingsManager.isLogJourney (JourneyDetailActivity.this) && (SettingsManager.getCurrentJourneyId (
                        JourneyDetailActivity.this) == _journey.getId ())) {
                    LogMyTripNotificationManager.updateLogging (JourneyDetailActivity.this,
                                                                _journey);
                }
            }

            @Override
            public void onNegativeButtonClick ()
            {
                // do nothing
            }

            @Override
            public void bindData (View v)
            {
                EditText txt;

                txt = (EditText) v.findViewById (R.id.etEditJourneyTitle);
                txt.setText (_journey.getTitle ());

                txt = (EditText) v.findViewById (R.id.etEditJourneyDescription);
                txt.setText (_journey.getDescription ());
            }
        });

        dlg.show (getSupportFragmentManager (), "editJourney");
    }
}
