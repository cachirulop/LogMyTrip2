package com.cachirulop.logmytrip.activity;

import android.app.backup.BackupManager;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.cachirulop.logmytrip.LogMyTripBackupAgent;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.fragment.SettingsFragment;

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
}
