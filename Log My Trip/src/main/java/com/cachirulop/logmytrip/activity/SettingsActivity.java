package com.cachirulop.logmytrip.activity;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.cachirulop.logmytrip.LogMyTripBackupAgent;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.fragment.SettingsFragment;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;

public class SettingsActivity
        extends AppCompatActivity
{
    SettingsFragment _settingsFragment;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);

        setContentView (R.layout.activity_settings);

        // Set a Toolbar to replace the ActionBar.
        Toolbar   toolbar;
        ActionBar ab;

        toolbar = (Toolbar) findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);

        ab = getSupportActionBar ();
        ab.setDisplayHomeAsUpEnabled (true);

        _settingsFragment = new SettingsFragment ();

        // Display the fragment as the main content.
        getFragmentManager ().beginTransaction ()
                             .replace (R.id.flContent, _settingsFragment)
                             .commit ();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy ();

        BackupManager.dataChanged (LogMyTripBackupAgent.class.getPackage ().getName ());
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult (requestCode, resultCode, data);

        if (requestCode == GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE) {
            _settingsFragment.onActivityResult (requestCode, resultCode, data);
        }
    }

}
