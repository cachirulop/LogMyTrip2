package com.cachirulop.logmytrip.activity;

import android.app.backup.BackupManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cachirulop.logmytrip.fragment.SettingsFragment;
import com.cachirulop.logmytrip.helper.LogHelper;

public class SettingsActivity
        extends AppCompatActivity
{
    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager ().beginTransaction ()
                             .replace (android.R.id.content, new SettingsFragment ())
                             .commit ();
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy ();

        LogHelper.d ("*** SettingsActivity.onDestroy: calling backup");

        // BackupManager.dataChanged ("com.cachirulop.logmytrip");
        new BackupManager (this).dataChanged ();
    }
}
