package com.cachirulop.logmytrip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class MainActivity
        extends AppCompatActivity
{
    private final static int ACTIVITY_RESULT_SETTINGS = 0;

    private MainFragment _mainFragment;
    private MenuItem     _menuAutoStartLog;

    private BroadcastReceiver _onBluetoothStartReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateMenuItemState ();
        }
    };

    private BroadcastReceiver _onBluetoothStopReceiver = new BroadcastReceiver ()
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
        setContentView (R.layout.activity_main);

        if (SettingsManager.isAutoStartLog (this)) {
            ServiceManager.startBluetooth (this);
        }

        // Configure action bar
        ActionBar bar;

        bar = getSupportActionBar ();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled (true);
            bar.setLogo (R.mipmap.ic_launcher);
            bar.setIcon (R.mipmap.ic_launcher);
            bar.setTitle (R.string.app_name);
        }

        // gets the main fragment
        _mainFragment = (MainFragment) getSupportFragmentManager ().findFragmentById (R.id.fMainFragment);
    }

    @Override
    protected void onResume ()
    {
        super.onResume ();

        LogMyTripBroadcastManager.registerBluetoothStartReceiver (this, _onBluetoothStartReceiver);
        LogMyTripBroadcastManager.registerBluetoothStopReceiver (this, _onBluetoothStopReceiver);

        updateMenuItemState ();
    }

    @Override
    protected void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (this, _onBluetoothStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (this, _onBluetoothStopReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater ().inflate (R.menu.main, menu);

        _menuAutoStartLog = menu.findItem (R.id.action_auto_start_log);

        updateMenuItemState ();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_settings:
                showPreferences ();
                return true;

            case R.id.action_auto_start_log:
                if (SettingsManager.isAutoStartLog (this)) {
                    ServiceManager.stopBluetooth (this);
                }
                else {
                    ServiceManager.startBluetooth (this);
                }

                updateMenuItemState ();

                return true;

            case R.id.action_import_db:
                LogMyTripDataHelper.importDB (this);
                _mainFragment.reloadTrips ();
                return true;

            case R.id.action_export_db:
                LogMyTripDataHelper.exportDB (this);
                return true;

            default:
                return super.onOptionsItemSelected (item);
        }
    }

    private void showPreferences ()
    {
        startActivityForResult (new Intent (this, SettingsActivity.class),
                                ACTIVITY_RESULT_SETTINGS);
    }

    private void updateMenuItemState ()
    {
        if (_menuAutoStartLog != null) {
            _menuAutoStartLog.setChecked (SettingsManager.isAutoStartLog (this));
        }
    }
}
