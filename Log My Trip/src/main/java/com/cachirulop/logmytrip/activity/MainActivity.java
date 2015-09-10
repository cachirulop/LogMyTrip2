
package com.cachirulop.logmytrip.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.service.LogMyTripService;

public class MainActivity
        extends Activity
{
    private final static int ACTIVITY_RESULT_SETTINGS = 0;

    private MainFragment _frgMain;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            _frgMain.updateSavingStatus((Trip) msg.obj);
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Inflate the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            _frgMain = new MainFragment();
            getFragmentManager ().beginTransaction ().add (R.id.container,
                    _frgMain).commit();
        }

        // Start the log service
        startService(new Intent(this, LogMyTripService.class));
    }

    @Override
    protected void onStart ()
    {
        super.onStart();
    }

    @Override
    protected void onStop ()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main,
                                    menu);

        MenuItem save;

        save = menu.findItem(R.id.action_save);
        if (save != null) {
            if (SettingsManager.isLogTrip(this)) {
                save.setIcon(android.R.drawable.ic_media_pause);
            } else {
                save.setIcon(android.R.drawable.ic_menu_save);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_settings:
                showPreferences ();
                return true;

            case R.id.action_save:
                if (SettingsManager.isLogTrip(this)) {
                    ServiceManager.stopSaveTrip(this, handler);
                    item.setIcon(android.R.drawable.ic_menu_save);
                } else {
                    ServiceManager.startSaveTrip(this, handler);
                    item.setIcon(android.R.drawable.ic_media_pause);
                }

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
