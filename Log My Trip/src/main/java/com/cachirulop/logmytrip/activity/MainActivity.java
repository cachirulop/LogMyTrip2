package com.cachirulop.logmytrip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class MainActivity
        extends AppCompatActivity
{
    private final static int ACTIVITY_RESULT_SETTINGS = 0;

    private MainFragment _mainFragment;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Inflate the view
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        // Start the log service
        // startService (new Intent (this, LogMyTripService.class));
        if (SettingsManager.isLogTrip (this)) {
            ServiceManager.startTripLog (this);
        }

        if (SettingsManager.isAutoStartLog (this)) {
            ServiceManager.startBluetooth (this);
        }

        // Configure action bar
        ActionBar bar;

        bar = getSupportActionBar ();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled (true);
            bar.setLogo (R.drawable.ic_launcher);
            bar.setIcon (R.drawable.ic_launcher);
            bar.setTitle (R.string.app_name);
        }

        // gets the main fragment
        _mainFragment = (MainFragment) getSupportFragmentManager ().findFragmentById (
                R.id.fMainFragment);
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        getMenuInflater ().inflate (R.menu.main, menu);

        MenuItem item;

        item = menu.findItem (R.id.action_auto_start_log);
        item.setChecked (SettingsManager.isAutoStartLog (this));

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
                    item.setChecked (false);
                    ServiceManager.stopBluetooth (this);
                }
                else {
                    item.setChecked (true);
                    ServiceManager.startBluetooth (this);
                }
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
}
