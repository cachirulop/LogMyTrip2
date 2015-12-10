package com.cachirulop.logmytrip.activity;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cachirulop.logmytrip.LogMyTripBackupAgent;
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

        _settingsFragment = new SettingsFragment ();

        // Display the fragment as the main content.
        getFragmentManager ().beginTransaction ().replace (android.R.id.content, _settingsFragment)
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
