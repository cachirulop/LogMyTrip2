package com.cachirulop.logmytrip.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.TripDetailActivity;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.service.LogMyTripService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainFragment
        extends Fragment
        implements TripItemAdapter.OnTripItemClickListener,
                   ActionMode.Callback
{
    private RecyclerView         _recyclerView;
    private TripItemAdapter      _adapter;
    private ActionMode           _actionMode;
    private FloatingActionButton _fabSaveTrip;
    private Context              _ctx;
    private BroadcastReceiver _onSaveTripStateChangeReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            String action;
            Trip   trip;

            trip = (Trip) intent.getSerializableExtra (LogMyTripService.BROADCAST_EXTRA_TRIP);
            action = intent.getAction ();
            switch (action) {
                case LogMyTripService.BROADCAST_ACTION_SAVE_TRIP_START:
                    updateSavingStatus (trip);
                    break;

                case LogMyTripService.BROADCAST_ACTION_SAVE_TRIP_STOP:
                    updateSavingStatus (trip);
                    break;
            }

            Log.d (MainFragment.class.getCanonicalName (), "Action: " + action);
        }
    };

    public MainFragment ()
    {
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        if (getView () != null) {
            // Init context
            _ctx = getActivity ();

            // Recyclerview
            _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvTrips);
            _recyclerView.setLayoutManager (new LinearLayoutManager (_ctx));
            _recyclerView.setHasFixedSize (true);

            _recyclerView.setItemAnimator (new DefaultItemAnimator ());

            loadTrips ();
            updateActionBarSubtitle ();

            // Save button
            _fabSaveTrip = (FloatingActionButton) getView ().findViewById (R.id.fabSaveTrip);
            _fabSaveTrip.setOnClickListener (new View.OnClickListener ()
            {
                @Override
                public void onClick (View v)
                {
                    onSaveTripClick (v);
                }
            });

            if (SettingsManager.isLogTrip (_ctx)) {
                _fabSaveTrip.setImageResource (android.R.drawable.ic_media_pause);
            }
            else {
                _fabSaveTrip.setImageResource (R.mipmap.ic_button_save);
            }

            // Receive the broadcast of the LogMyTripService class
            LocalBroadcastManager.getInstance (_ctx)
                                 .registerReceiver (_onSaveTripStateChangeReceiver,
                                                    new IntentFilter (
                                                            LogMyTripService.BROADCAST_ACTION_SAVE_TRIP_START));
            LocalBroadcastManager.getInstance (_ctx)
                                 .registerReceiver (_onSaveTripStateChangeReceiver,
                                                    new IntentFilter (
                                                            LogMyTripService.BROADCAST_ACTION_SAVE_TRIP_STOP));
        }
    }

    /**
     * Load existing trips
     */
    private void loadTrips ()
    {
        List<Trip> trips;

        if (getView () != null) {
            trips = TripManager.LoadTrips (_ctx);

            _adapter = new TripItemAdapter (_ctx, trips);
            _adapter.setOnTripItemClickListener (this);
            _recyclerView.setAdapter (_adapter);
        }
    }

    private void updateActionBarSubtitle ()
    {
        ActionBar bar;

        bar = ((AppCompatActivity) getActivity ()).getSupportActionBar ();
        bar.setSubtitle (
                _ctx.getString (R.string.main_activity_subtitle, _adapter.getItemCount ()));
    }

    private void onSaveTripClick (View v)
    {
        if (SettingsManager.isLogTrip (_ctx)) {
            ServiceManager.stopSaveTrip (_ctx);

            _fabSaveTrip.setImageResource (R.mipmap.ic_button_save);
        }
        else {
            ServiceManager.startSaveTrip (_ctx);

            _fabSaveTrip.setImageResource (android.R.drawable.ic_media_pause);

            updateActionBarSubtitle ();
        }
    }

    public void updateSavingStatus (Trip currentTrip)
    {
        int position;

        _recyclerView.smoothScrollToPosition (0);
        _adapter.updateTripStatus ();
    }

    public RecyclerView getRecyclerView ()
    {
        return _recyclerView;
    }

    @Override
    public void onTripItemLongClick (View v, int position)
    {
        if (_actionMode != null) {
            return;
        }

        _actionMode = getView ().startActionMode (this);

        updateActionModeTitle ();
    }

    private void updateActionModeTitle ()
    {
        _actionMode.setTitle (
                getString (R.string.selected_count, _adapter.getSelectedItemCount ()));
    }

    /*  ActionMode.Callback implementation */

    @Override
    public void onTripItemClick (View view, int position)
    {
        if (_actionMode != null) {
            updateActionModeTitle ();
        }
        else {
            Intent i;

            i = new Intent (_ctx, TripDetailActivity.class);
            i.putExtra (TabMapFragment.ARG_PARAM_TRIP, _adapter.getItem (position));

            startActivity (i);
        }
    }

    @Override
    public boolean onCreateActionMode (ActionMode mode, Menu menu)
    {
        _adapter.setActionMode (true);

        MenuInflater inflater = mode.getMenuInflater ();
        inflater.inflate (R.menu.menu_trip_actionmode, menu);

        _fabSaveTrip.hide ();

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

            case R.id.action_export_selected_trip:
                exportSelectedTrips ();

                return true;
            default:
                return false;
        }
    }

    private void deleteSelectedTrips ()
    {
        List<Trip> selectedItems = _adapter.getSelectedItems ();

        for (Trip t : selectedItems) {
            TripManager.deleteTrip (_ctx, t);
            _adapter.removeItem (t);
        }

        _actionMode.finish ();
    }

    private void exportSelectedTrips ()
    {
        List<Trip> selectedItems = _adapter.getSelectedItems ();

        for (Trip t : selectedItems) {
            exportTrip (t);
        }

        _actionMode.finish ();
    }

    private void exportTrip (final Trip t)
    {
        File folder;
        final String filename;

        folder = new File (Environment.getExternalStorageDirectory () + "/" + _ctx.getText (
                R.string.app_name));

        if (!folder.exists ()) {
            folder.mkdir ();
        }

        filename = folder.toString () + "/" + DateFormat.format ("yyyy-MM-dd",
                                                                 t.getTripDate ()) + ".csv";

        // show waiting screen
        final ProgressDialog progDialog;

        progDialog = ProgressDialog.show (_ctx, getString (R.string.app_name),
                                          _ctx.getString (R.string.exporting_trips), true);

        Log.d (MainFragment.class.getCanonicalName (), "Writing file: " + filename);

        final Handler handler = new Handler ()
        {
            @Override
            public void handleMessage (Message msg)
            {
            }
        };

        new Thread ()
        {
            public void run ()
            {
                try {
                    FileWriter fw = new FileWriter (filename);

                    writeFileLine (fw, new String[]{ "Trip ID", "Date", "Time", "Description" });
                    writeFileLine (fw, new String[]{ String.format ("%d", t.getId ()),
                                                     DateFormat.getMediumDateFormat (_ctx)
                                                               .format (t.getTripDate ()),
                                                     DateFormat.format ("HH:mm:ss",
                                                                        t.getTripDate ())
                                                               .toString (),
                                                     t.getDescription () });

                    writeFileLine (fw, new String[]{ " " });

                    writeFileLine (fw,
                                   new String[]{ "Loc. ID", "Trip ID", "Date", "Time", "Latitude",
                                                 "Longitude", "Altitude", "Speed", "Accuracy",
                                                 "Bearing", "Time as number" });
                    for (TripLocation l : t.getLocations ()) {
                        writeFileLine (fw, new String[]{ String.format ("%d", l.getId ()),
                                                         String.format ("%d", l.getIdTrip ()),
                                                         DateFormat.getMediumDateFormat (_ctx)
                                                                   .format (
                                                                           l.getLocationTimeAsDate ()),
                                                         DateFormat.format ("HH:mm:ss",
                                                                            l.getLocationTimeAsDate ())
                                                                   .toString (),
                                                         String.format ("%f", l.getLatitude ()),
                                                         String.format ("%f", l.getLongitude ()),
                                                         String.format ("%f", l.getAltitude ()),
                                                         String.format ("%f", l.getSpeed ()),
                                                         String.format ("%f", l.getAccuracy ()),
                                                         String.format ("%f", l.getBearing ()),
                                                         String.format ("%d",
                                                                        l.getLocationTime ()) });
                    }

                    fw.flush ();
                    fw.close ();
                }
                catch (Exception e) {
                    Log.d (MainFragment.class.getCanonicalName (),
                           "Error writing file: " + e.getLocalizedMessage ());
                }

                handler.sendEmptyMessage (0);
                progDialog.dismiss ();
            }
        }.start ();
    }

    private void writeFileLine (FileWriter fw, String[] values)
            throws IOException
    {
        for (String s : values) {
            fw.append (s);
            fw.append (";");
        }

        fw.append ("\n");
    }

    @Override
    public void onDestroy ()
    {
        LocalBroadcastManager.getInstance (_ctx)
                             .unregisterReceiver (_onSaveTripStateChangeReceiver);

        super.onDestroy ();
    }

    @Override
    public void onDestroyActionMode (ActionMode mode)
    {
        _actionMode = null;
        _adapter.setActionMode (false);
        _adapter.clearSelections ();
        _fabSaveTrip.show ();

        updateActionBarSubtitle ();
    }
}
