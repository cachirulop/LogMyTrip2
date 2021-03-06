package com.cachirulop.logmytrip.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.activity.JourneyDetailActivity;
import com.cachirulop.logmytrip.adapter.JourneyItemAdapter;
import com.cachirulop.logmytrip.dialog.ConfirmDialog;
import com.cachirulop.logmytrip.dialog.CustomViewDialog;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.helper.DialogHelper;
import com.cachirulop.logmytrip.helper.ExportHelper;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.SelectedJourneyHolder;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainFragment
        extends Fragment
        implements RecyclerViewItemClickListener,
                   ActionMode.Callback,
                   IMainFragment
{
    private boolean              _journeysLoaded;
    private boolean              _startLog;
    private RecyclerView         _recyclerView;
    private JourneyItemAdapter   _adapter;
    private ActionMode           _actionMode;
    private FloatingActionButton _fabLog;
    public BroadcastReceiver _onLogStopReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            _adapter.stopLog (LogMyTripBroadcastManager.getTrip (intent));
            refreshFabLog ();
        }
    };

    private boolean _exportFormatIsGPX;
    private BroadcastReceiver _onLogStartReceiver    = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (_journeysLoaded) {
                autoStartLog ();
            }
            else {
                _startLog = true;
            }
        }
    };

    private BroadcastReceiver _onNewLocationReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            if (_journeysLoaded) {
                // _adapter.setItem (0, JourneyManager.getActiveJourney (getContext ()));
                // TODO: Refresh the statistics
                                Journey current;

                                current = _adapter.getItem (0);

                                current.computeLiveStatistics (getContext ());

                                _adapter.notifyItemChanged (0);
            }
        }
    };

    public MainFragment ()
    {
        _journeysLoaded = false;
        _startLog = false;
    }

    public void reloadJourneys ()
    {
        if (_actionMode == null) {
            _adapter.reloadItems ();
        }
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

    private void startDetailActivity (Journey t)
    {
        Intent i;

        i = new Intent (getContext (), JourneyDetailActivity.class);

        SelectedJourneyHolder.getInstance ().setSelectedJourney (t);

        startActivity (i);
    }

    @Override
    public boolean onCreateActionMode (ActionMode mode, Menu menu)
    {
        _adapter.setActionMode (true);

        MenuInflater inflater = mode.getMenuInflater ();
        inflater.inflate (R.menu.menu_journey_actionmode, menu);

        _fabLog.hide ();

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
            case R.id.action_delete_selected_journey:
                deleteSelectedJourneys ();
                return true;

            case R.id.action_select_all_journeys:
                selectAllJourneys ();
                return true;

            case R.id.action_deselect_all_journeys:
                deselectAllJourneys ();
                return true;

            case R.id.action_export:
                exportJourneysDialog ();
                return true;

            default:
                return false;
        }
    }

    private void deleteSelectedJourneys ()
    {
        ConfirmDialog dlg;

        try {

            dlg = new ConfirmDialog ();
            dlg.setTitleId (R.string.title_delete);
            dlg.setMessageId (R.string.msg_delete_confirm);
            dlg.setListener (new ConfirmDialog.OnConfirmDialogListener ()
            {
                @Override
                public void onPositiveButtonClick ()
                {
                    List<Journey> selectedItems = _adapter.getSelectedItems ();

                    for (Journey t : selectedItems) {
                        JourneyManager.deleteJourney (getContext (), t);
                        _adapter.removeItem (t);
                    }

                    _actionMode.finish ();
                }

                @Override
                public void onNegativeButtonClick ()
                {
                    // Do nothing
                }
            });
/*
            dlg.setOnOkClickListener (new DialogInterface.OnClickListener ()
            {
                @Override
                public void onClick (DialogInterface dialog, int which)
                {
                    List<Journey> selectedItems = _adapter.getSelectedItems ();

                    LogHelper.d ("Borrando el journey");

                    for (Journey t : selectedItems) {
                        JourneyManager.deleteJourney (getContext (), t);
                        _adapter.removeItem (t);
                    }

                    _actionMode.finish ();

                    LogHelper.d ("Borrando el journey HECHO");
                }
            });
*/

            dlg.show (getActivity ().getSupportFragmentManager (), "deleteJourneys");
        }
        catch (Exception e) {
            LogHelper.d ("Error showing ConfirmDialog: " + e.getLocalizedMessage ());
        }
    }

    private void selectAllJourneys ()
    {
        _adapter.selectAllItems ();
    }

    private void deselectAllJourneys ()
    {
        _adapter.deselectAllItems ();
    }

    private void exportJourneysDialog ()
    {
        final CustomViewDialog dlg;

        dlg = new CustomViewDialog ();

        dlg.setTitleId (R.string.title_export);
        dlg.setViewId (R.layout.dialog_export);
        dlg.setListener (new CustomViewDialog.OnCustomDialogListener ()
        {
            @Override
            public void onPositiveButtonClick ()
            {
                RadioButton rb;

                rb = (RadioButton) dlg.getCustomView ().findViewById (R.id.rbExportGPX);
                _exportFormatIsGPX = rb.isChecked ();

                exportJourneys ();
            }

            @Override
            public void onNegativeButtonClick ()
            {
                // do nothing
            }

            @Override
            public void bindData (View v)
            {
                RadioButton rb;

                rb = (RadioButton) dlg.getCustomView ().findViewById (R.id.rbExportGPX);
                rb.setChecked (true);
            }
        });

        dlg.show (getActivity ().getSupportFragmentManager (), "exportJourneys");
    }

    private void exportJourneys ()
    {
        final ProgressDialog progDialog;
        final Context        ctx;

        ctx = getContext ();

        progDialog = ProgressDialog.show (getContext (),
                                          getContext ().getString (R.string.msg_exporting_journeys),
                                          null,
                                          true);

        new Thread ()
        {
            public void run ()
            {
                List<Journey> selectedItems = _adapter.getSelectedItems ();
                ExportHelper.IExportHelperListener listener;

                listener = new ExportHelper.IExportHelperListener ()
                {
                    @Override
                    public void onExportSuccess (final String fileName)
                    {
                        LogMyTripApplication.runInMainThread (ctx, new Runnable ()
                        {
                            @Override
                            public void run ()
                            {
                                progDialog.setMessage (String.format (ctx.getString (R.string.text_file_exported),
                                                                      fileName));
                            }
                        });
                    }

                    @Override
                    public void onExportFails (int messageId, Object... formatArgs)
                    {
                        DialogHelper.showErrorDialogMainThread (ctx,
                                                                R.string.title_export,
                                                                messageId,
                                                                formatArgs);
                    }
                };

                for (Journey t : selectedItems) {
                    String track;
                    String fileName = null;

                    if (_exportFormatIsGPX) {
                        fileName = getTrackFileName (t.getJouneyDate (), "gpx");
                    }
                    else {
                        fileName = getTrackFileName (t.getJouneyDate (), "kml");
                    }

                    ExportHelper.exportToFile (ctx, t, fileName, listener);
                }

                LogMyTripApplication.runInMainThread (ctx, new Runnable ()
                {
                    @Override
                    public void run ()
                    {
                        _actionMode.finish ();
                    }
                });

                progDialog.dismiss ();
            }
        }.start ();
    }

    private String getTrackFileName (Date trackDate, String extension)
    {
        StringBuilder result;
        DateFormat    dfFileName;

        dfFileName = new SimpleDateFormat ("yyyy-MM-dd HHmmss");

        result = new StringBuilder ();
        result.append (dfFileName.format (trackDate)).append (".").append (extension);

        return result.toString ();
    }

    @Override
    public void onDestroyActionMode (ActionMode mode)
    {
        _actionMode = null;
        _adapter.setActionMode (false);
        _adapter.deselectAllItems ();
        _fabLog.show ();

        updateActionBarSubtitle ();
    }

    private void updateActionBarSubtitle ()
    {
        ActionBar bar;

        bar = ((AppCompatActivity) getActivity ()).getSupportActionBar ();
        bar.setSubtitle (getContext ().getString (R.string.main_activity_subtitle,
                                                  _adapter.getItemCount ()));
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
            // Recyclerview
            _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvJourneys);
            _recyclerView.setLayoutManager (new LinearLayoutManager (getContext ()));

            _recyclerView.setItemAnimator (new DefaultItemAnimator ());

            loadJourneys ();
            updateActionBarSubtitle ();

            // Log button
            _fabLog = (FloatingActionButton) getView ().findViewById (R.id.fabLog);
            _fabLog.setOnClickListener (new View.OnClickListener ()
            {
                @Override
                public void onClick (View v)
                {
                    onFabLogClick (v);
                }
            });

            if (SettingsManager.isLogJourney (getContext ())) {
                // Ensure to start log service
                // if (!LogMyTripService.isRunning ()) {
                    ServiceManager.startLog (getContext ());
                // }
            }

            refreshFabLog ();
        }
    }

    @Override
    public void onResume ()
    {
        _adapter.reloadItems ();

        // Receive the broadcast of the LogMyTripService class
        LogMyTripBroadcastManager.registerLogStartReceiver (getContext (), _onLogStartReceiver);
        LogMyTripBroadcastManager.registerLogStopReceiver (getContext (), _onLogStopReceiver);

        LogMyTripBroadcastManager.registerNewLocationReceiver (getContext (),
                                                               _onNewLocationReceiver);

        refreshFabLog ();

        super.onResume ();
    }

    @Override
    public void onPause ()
    {
        if (_actionMode == null) {
            _adapter.clearJourneys ();
            _journeysLoaded = false;
        }

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onLogStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onLogStopReceiver);

        LogMyTripBroadcastManager.unregisterReceiver (getContext (), _onNewLocationReceiver);

        super.onPause ();
    }

    /**
     * Load existing journeys
     */
    private void loadJourneys ()
    {
        if (getView () != null) {
            _adapter = new JourneyItemAdapter (getContext (),
                                               new JourneyItemAdapter.JourneyItemAdapterListener ()
                                            {
                                                @Override
                                                public void onJourneyListLoaded ()
                                                {
                                                    updateActionBarSubtitle ();
                                                    _journeysLoaded = true;

                                                    if (_startLog) {
                                                        autoStartLog ();
                                                        _startLog = false;
                                                    }
                                                }
                                            });

            _adapter.setOnJourneyItemClickListener (this);
            _recyclerView.setAdapter (_adapter);
        }
    }

    private void onFabLogClick (View v)
    {
        if (SettingsManager.isLogJourney (getContext ())) {
            _recyclerView.scrollToPosition (0);

            ServiceManager.stopLog (getContext ());

            _fabLog.setImageResource (R.mipmap.ic_button_save);
        }
        else {
            ServiceManager.startLog (getContext ());
            _fabLog.setImageResource (android.R.drawable.ic_media_pause);
        }
    }

    private void refreshFabLog ()
    {
        if (SettingsManager.isLogJourney (getContext ())) {
            _fabLog.setImageResource (android.R.drawable.ic_media_pause);
        }
        else {
            _fabLog.setImageResource (R.mipmap.ic_button_save);
        }
    }

    private void autoStartLog ()
    {
        _adapter.startLog ();
        startDetailActivity (_adapter.getItem (0));
    }

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
        onActivityResult (requestCode, resultCode, data);
    }

    @Override
    public void reloadData ()
    {
        reloadJourneys ();
    }
}
