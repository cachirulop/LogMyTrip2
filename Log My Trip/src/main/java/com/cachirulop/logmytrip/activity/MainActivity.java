
package com.cachirulop.logmytrip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class MainActivity
        extends AppCompatActivity
{
    private final static int ACTIVITY_RESULT_SETTINGS = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Inflate the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start the log service
        startService(new Intent(this, LogMyTripService.class));

        // Configure action bar
        ActionBar bar;

        bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowHomeEnabled(true);
            bar.setLogo(R.drawable.ic_launcher);
            bar.setIcon(R.drawable.ic_launcher);
            bar.setTitle(R.string.app_name);
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main,
                                    menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_settings:
                showPreferences ();
                return true;

            default:
                return super.onOptionsItemSelected (item);
        }
    }

    private void showPreferences ()
    {
        startActivityForResult(new Intent(this,
                        SettingsActivity.class),
                ACTIVITY_RESULT_SETTINGS);
    }
}
