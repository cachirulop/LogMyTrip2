package com.cachirulop.logmytrip.fragment;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.TripDetailActivity;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.dialog.ConfirmDialog;
import com.cachirulop.logmytrip.dialog.CustomViewDialog;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.helper.ExportHelper;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedTripHolder;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.service.LogMyTripService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainFragment
        extends Fragment
        implements RecyclerViewItemClickListener,
                   ActionMode.Callback,
                   GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener
{
    public static final int RESOLVE_CONNECTION_REQUEST_CODE = 10201;

    private GoogleApiClient _client;
    private boolean              _tripsLoaded;
    private boolean              _startLog;
    private RecyclerView         _recyclerView;
    private TripItemAdapter      _adapter;
    private ActionMode           _actionMode;
    private FloatingActionButton _fabTripLog;
    private boolean         _exportFormatIsGPX;
    public  BroadcastReceiver _onTripLogStopReceiver  = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            _adapter.stopTripLog (LogMyTripBroadcastManager.getTrip (intent));
            refreshFabTrip ();
        }
    };
    private BroadcastReceiver _onTripLogStartReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (_tripsLoaded) {
                autoStartLog ();
            }
            else {
                _startLog = true;
            }
        }
    };
    private BroadcastReceiver _onNewLocationReceiver  = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (_tripsLoaded) {
                _adapter.setItem (0, TripManager.getActiveTrip (getContext ()));
                _adapter.notifyItemChanged (0);
            }
        }
    };

    public MainFragment ()
    {
        _tripsLoaded = false;
        _startLog = false;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        if (getView () != null) {
            // Toolbar
            Toolbar toolbar;

            toolbar = (Toolbar) view.findViewById (R.id.fragment_main_toolbar);
            ((AppCompatActivity) getActivity ()).setSupportActionBar (toolbar);

            // Recyclerview
            _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvTrips);
            _recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));

            _recyclerView.setItemAnimator (new DefaultItemAnimator ());

            loadTrips ();
            updateActionBarSubtitle ();

            // Log button
            _fabTripLog = (FloatingActionButton) getView ().findViewById (R.id.fabTripLog);
            _fabTripLog.setOnClickListener (new View.OnClickListener ()
            {
                @Override
                public void onClick (View v)
                {
                    onTripLogClick (v);
                }
            });

            if (SettingsManager.isLogTrip (getContext ())) {
                // Ensure to start log service
                if (!LogMyTripService.isRunning ()) {
                    ServiceManager.startTripLog (getContext ());
                }
            }

            refreshFabTrip ();
        }
    }

    /**
     * Load existing trips
     */
    private void loadTrips ()
    {
        if (getView () != null) {
            _adapter = new TripItemAdapter (getContext (),
                                            new TripItemAdapter.TripItemAdapterListener ()
                                            {
                                                @Override
                                                public void onTripListLoaded ()
                                                {
                                                    updateActionBarSubtitle ();
                                                    _tripsLoaded = true;

                                                    if (_startLog) {
                                                        autoStartLog ();
                                                        _startLog = false;
                                                    }
                                                }
                                            });

            _adapter.setOnTripItemClickListener (this);
            _recyclerView.setAdapter (_adapter);
        }
    }

    private void updateActionBarSubtitle ()
    {
        ActionBar bar;

        bar = ((AppCompatActivity) getActivity ()).getSupportActionBar ();
        bar.setSubtitle (getContext ().getString (R.string.main_activity_subtitle,
                                                  _adapter.getItemCount ()));
    }

    private void onTripLogClick (View v)
    {
        if (SettingsManager.isLogTrip (getContext ())) {
            _recyclerView.scrollToPosition (0);

            ServiceManager.stopTripLog (getContext ());

            _fabTripLog.setImageResource (R.mipmap.ic_button_save);
        }
        else {
            ServiceManager.startTripLog (getContext ());
        }
    }

    private void refreshFabTrip ()
    {
        if (SettingsManager.isLogTrip (getContext ())) {
            _fabTripLog.setImageResource (android.R.drawable.ic_media_pause);
        }
        else {
            _fabTripLog.setImageResource (R.mipmap.ic_button_save);
        }
    }

    private void autoStartLog ()
    {
        _adapter.startTripLog ();
        startDetailActivity (_adapter.getItem (0));
    }

    private void startDetailActivity (Trip t)
    {
        Intent i;

        i = new Intent (getContext (), TripDetailActivity.class);

        SelectedTripHolder.getInstance ().setSelectedTrip (t);

        startActivity (i);
    }

    @Override
    public void onResume ()
    {
        _adapter.reloadTrips ();

        // Receive the broadcast of the LogMyTripService class
        LogMyTripBroadcastManager.registerTripLogStartReceiver (getContext (),
                                                                _onTripLogStartReceiver);
        LogMyTripBroadcastManager.registerTripLogStopReceiver (getContext (),
                                                               _onTripLogStopReceiver);

        LogMyTripBroadcastManager.registerNewLocationReceiver (getContext (),
                                                               _onNewLocationReceiver);

        refreshFabTrip ();

        super.onResume ();
    }

    @Override
    public void onPause ()
    {
        if (_actionMode == null) {
            _adapter.clearTrips ();
        }

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onTripLogStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onTripLogStopReceiver);

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);

        super.onPause ();
    }

    public void reloadTrips ()
    {
        if (_actionMode == null) {
            _adapter.reloadTrips ();
        }
    }

    public RecyclerView getRecyclerView ()
    {
        return _recyclerView;
    }

    @Override
    public void onRecyclerViewItemLongClick (View v, int position)
    {
        if (_actionMode != null) {
            return;
        }

        _actionMode = getView ().startActionMode (this);

        updateActionModeTitle ();
    }

    private void updateActionModeTitle ()
    {
        _actionMode.setTitle (getString (R.string.title_selected_count,
                                         _adapter.getSelectedItemCount ()));
    }

    @Override
    public void onRecyclerViewItemClick (View view, int position)
    {
        if (_actionMode != null) {
            updateActionModeTitle ();
        }
        else {
            startDetailActivity (_adapter.getItem (position));
        }
    }

    @Override
    public boolean onCreateActionMode (ActionMode mode, Menu menu)
    {
        _adapter.setActionMode (true);

        MenuInflater inflater = mode.getMenuInflater ();
        inflater.inflate (R.menu.menu_trip_actionmode, menu);

        _fabTripLog.hide ();

        return true;
    }

    @Override
    public boolean onPrepareActionMode (ActionMode mode, Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked (ActionMode mode, MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_delete_selected_trip:
                deleteSelectedTrips ();
                return true;

            case R.id.action_select_all_trips:
                selectAllTrips ();
                return true;

            case R.id.action_deselect_all_trips:
                deselectAllTrips ();
                return true;

            case R.id.action_export:
                exportTripsDialog ();
                return true;

            default:
                return false;
        }
    }

    private void deleteSelectedTrips ()
    {
        ConfirmDialog dlg;

        dlg = new ConfirmDialog (R.string.title_delete, R.string.msg_delete_confirm)
        {
            @Override
            public void onOkClicked ()
            {
                List<Trip> selectedItems = _adapter.getSelectedItems ();

                for (Trip t : selectedItems) {
                    TripManager.deleteTrip (getContext (), t);
                    _adapter.removeItem (t);
                }

                _actionMode.finish ();
            }
        };

        dlg.show (getActivity ().getSupportFragmentManager (), "deleteTrips");
    }

    private void selectAllTrips ()
    {
        _adapter.selectAllItems ();
    }

    private void deselectAllTrips ()
    {
        _adapter.deselectAllItems ();
    }

    private void exportTripsDialog ()
    {
        CustomViewDialog dlg;

        dlg = new CustomViewDialog (R.string.title_export, R.layout.dialog_export)
        {
            @Override
            public void bindData (View view)
            {
                RadioButton rb;

                rb = (RadioButton) view.findViewById (R.id.rbExportGPX);
                rb.setChecked (true);

                rb = (RadioButton) view.findViewById (R.id.rbExportLocalFile);
                rb.setChecked (true);
            }

            @Override
            public void onOkClicked (View view)
            {
                RadioButton rb;
                boolean     locationLocal;

                rb = (RadioButton) view.findViewById (R.id.rbExportGPX);
                _exportFormatIsGPX = rb.isChecked ();

                rb = (RadioButton) view.findViewById (R.id.rbExportLocalFile);
                locationLocal = rb.isChecked ();

                if (locationLocal) {
                    exportTrips (locationLocal);
                }
                else {
                    startGoogleDriveActivity ();
                }
            }
        };

        dlg.show (getActivity ().getSupportFragmentManager (), "exportTrips");
    }

    private void exportTrips (boolean locationLocal)
    {
        List<Trip> selectedItems = _adapter.getSelectedItems ();

        for (Trip t : selectedItems) {
            String track;
            String fileName;

            if (_exportFormatIsGPX) {
                track = TripManager.generateGPX (getContext (), t);
                fileName = getTrackFileName (t.getTripDate (), "gpx");
            }
            else {
                track = "Work in progress...";
                fileName = getTrackFileName (t.getTripDate (), "kml");
            }

            if (locationLocal) {
                ExportHelper.exportToFile (getContext (), track, fileName);
            }
            else {
                ExportHelper.exportToGoogleDrive (getContext (), track, fileName, _client);
            }
        }

        _actionMode.finish ();
    }

    private void startGoogleDriveActivity ()
    {
        _client = new GoogleApiClient.Builder (getContext ()).addApi (Drive.API)
                                                             .addScope (Drive.SCOPE_FILE)
                                                             .addConnectionCallbacks (this)
                                                             .addOnConnectionFailedListener (this)
                                                             .useDefaultAccount ()
                                                             .build ();
        _client.connect ();

    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        // super.onActivityResult (requestCode, resultCode, data);
        LogHelper.d ("*** onActivityResult: " + requestCode + "-.-" + resultCode);
        if (requestCode == RESOLVE_CONNECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                exportTrips (false);
            }
            else {
                LogHelper.d ("*** Cancel export");
            }
        }
    }

    private String getTrackFileName (Date trackDate, String extension)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd HHmmss");

        result = new StringBuilder ();
        result.append (dfFileName.format (trackDate) + "." + extension);

        return result.toString ();
    }

    @Override
    public void onDestroyActionMode (ActionMode mode)
    {
        _actionMode = null;
        _adapter.setActionMode (false);
        _adapter.deselectAllItems ();
        _fabTripLog.show ();

        updateActionBarSubtitle ();
    }

    @Override
    public void onConnected (Bundle bundle)
    {
        LogHelper.d ("*** onConnected");
        exportTrips (false);
    }

    @Override
    public void onConnectionSuspended (int i)
    {
        LogHelper.d ("*** onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult)
    {
        if (connectionResult.hasResolution ()) {
            try {
                connectionResult.startResolutionForResult (getActivity (),
                                                           RESOLVE_CONNECTION_REQUEST_CODE);
            }
            catch (IntentSender.SendIntentException e) {
            }
        }
        else {
            GooglePlayServicesUtil.getErrorDialog (connectionResult.getErrorCode (),
                                                   getActivity (),
                                                   0).show ();
        }
    }
}
