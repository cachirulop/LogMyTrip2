package com.cachirulop.logmytrip.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.cachirulop.logmytrip.helper.LogHelper;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.fragment.IMainFragment;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity
{
    private static final int REQUEST_PERMISSIONS = 1002;

    private DrawerLayout          _drawer;
    private ActionBarDrawerToggle _drawerToggle;
    private NavigationView        _nvDrawer;
    private Switch                _swAutoStartLog;

    private IMainFragment _mainFragment;

    private BroadcastReceiver _onBluetoothStartReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateAutoStartLogSwitch ();
        }
    };

    private BroadcastReceiver _onBluetoothStopReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            updateAutoStartLogSwitch ();
        }
    };

    @Override
    protected void onPause ()
    {
        super.onPause ();

        LogMyTripBroadcastManager.unregisterReceiver (this, _onBluetoothStartReceiver);
        LogMyTripBroadcastManager.unregisterReceiver (this, _onBluetoothStopReceiver);
    }

    @Override
    protected void onResume ()
    {
        super.onResume ();

        LogMyTripBroadcastManager.registerBluetoothStartReceiver (this, _onBluetoothStartReceiver);
        LogMyTripBroadcastManager.registerBluetoothStopReceiver (this, _onBluetoothStopReceiver);

        updateAutoStartLogSwitch ();
    }

    private void updateAutoStartLogSwitch ()
    {
        if (_swAutoStartLog != null) {
            disableAutoStartLogSwitchEvent ();

            _swAutoStartLog.setChecked (SettingsManager.isAutoStartLogBluetooth (this));

            enableAutoStartLogSwitchEvent ();
        }
    }

    @Override
    public void onBackPressed ()
    {
        if (_drawer.isDrawerOpen (Gravity.LEFT)) {
            _drawer.closeDrawer (Gravity.LEFT);
        }
        else {
            super.onBackPressed ();
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        try {
            initPermissions ();

            // Inflate the view
            super.onCreate (savedInstanceState);
            setContentView (R.layout.activity_main);

            if (SettingsManager.isAutoStartLogBluetooth (this)) {
                ServiceManager.startBluetooth (this);
            }

            // Set a Toolbar to replace the ActionBar.
            Toolbar toolbar;

            toolbar = findViewById (R.id.toolbar);
            setSupportActionBar (toolbar);

            toolbar.setLogo (R.mipmap.ic_launcher);
            toolbar.setTitle (R.string.app_name);

            // Drawer
            _drawer = findViewById (R.id.drawer_layout);
            _drawerToggle = new ActionBarDrawerToggle (this,
                                                       _drawer,
                                                       toolbar,
                                                       R.string.drawer_open,
                                                       R.string.drawer_close);
            _drawer.addDrawerListener (_drawerToggle);

            _nvDrawer = findViewById (R.id.nvDrawer);

            setupDrawerContent ();

            loadFragment (R.id.action_journeys);
        }
        catch (RuntimeException e) {
            LogHelper.d ("Exception onCreate: " + e.getLocalizedMessage ());

            throw e;
        }
    }

    @Override
    protected void onPostCreate (Bundle savedInstanceState)
    {
        super.onPostCreate (savedInstanceState);

        _drawerToggle.syncState ();
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig)
    {
        super.onConfigurationChanged (newConfig);

        _drawerToggle.onConfigurationChanged (newConfig);
    }

    private void setupDrawerContent ()
    {
        Menu     mnu;
        MenuItem autoStartLog;

        mnu = _nvDrawer.getMenu ();
        if (mnu != null) {
            autoStartLog = mnu.findItem (R.id.action_auto_start_log);
            if (autoStartLog != null) {
                _swAutoStartLog = autoStartLog.getActionView ().findViewById (R.id.switch1);
                enableAutoStartLogSwitchEvent ();
            }
        }

        _nvDrawer.setNavigationItemSelectedListener (new NavigationView.OnNavigationItemSelectedListener ()
        {
            @Override
            public boolean onNavigationItemSelected (@NonNull MenuItem menuItem)
            {
                onDrawerMenuOptionsItemSelected (menuItem);
                return true;
            }
        });
    }

    private void enableAutoStartLogSwitchEvent ()
    {
        _swAutoStartLog.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener ()
        {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
            {
                toggleAutoStartLog ();
            }
        });
    }

    private void disableAutoStartLogSwitchEvent ()
    {
        _swAutoStartLog.setOnCheckedChangeListener (null);
    }

    private void toggleAutoStartLog ()
    {
        if (SettingsManager.isAutoStartLogBluetooth (this)) {
            ServiceManager.stopBluetooth (this);
        }
        else {
            ServiceManager.startBluetooth (this);
        }
    }

    private void loadFragment (int itemId)
    {
        Fragment fragment;
        Class    fragmentClass = null;

        switch (itemId) {
            case R.id.action_journeys:
                fragmentClass = MainFragment.class;
                break;

            case R.id.action_settings:
                showPreferences ();
                break;

            default:
                fragmentClass = MainFragment.class;
                break;
        }

        if (fragmentClass != null) {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager ();

                fragment = (Fragment) fragmentClass.newInstance ();
                _mainFragment = (IMainFragment) fragment;

                fragmentManager.beginTransaction ().replace (R.id.flContent, fragment).commit ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }

    public void onDrawerMenuOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case android.R.id.home:
                _drawer.openDrawer (GravityCompat.START);
                break;

            case R.id.action_journeys:
            case R.id.action_trips:
            case R.id.action_settings:
                item.setChecked (true);
                setTitle (item.getTitle ());
                loadFragment (item.getItemId ());

                _drawer.closeDrawers ();

                break;

            case R.id.action_auto_start_log:

                // this makes an infinite loop !!!!!
                _swAutoStartLog.toggle ();
                // toggleAutoStartLog ();
                // updateAutoStartLogSwitch ();

                break;

            case R.id.action_import_db:
                LogMyTripDataHelper.importDB (this);
                _mainFragment.reloadData ();

                _drawer.closeDrawers ();

                break;

            case R.id.action_export_db:
                LogMyTripDataHelper.exportDB (this);

                _drawer.closeDrawers ();

                break;
        }
    }

    private void showPreferences ()
    {
        startActivity (new Intent (this, SettingsActivity.class));
    }

    private void initPermissions ()
    {
        ArrayList<String> permissions;

        permissions = new ArrayList<> ();

        fillPermissionList (permissions, Manifest.permission.BLUETOOTH);
        fillPermissionList (permissions, Manifest.permission.INTERNET);
        fillPermissionList (permissions, Manifest.permission.ACCESS_NETWORK_STATE);
        fillPermissionList (permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        fillPermissionList (permissions, Manifest.permission.ACCESS_COARSE_LOCATION);
        fillPermissionList (permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        fillPermissionList (permissions, Manifest.permission.GET_ACCOUNTS);
        fillPermissionList (permissions, Manifest.permission.RECEIVE_BOOT_COMPLETED);
        fillPermissionList (permissions, Manifest.permission.FOREGROUND_SERVICE);

        if (permissions.size () > 0) {
            String [] ps;

            ps = new String[permissions.size ()];

            ActivityCompat.requestPermissions (this,
                                               permissions.toArray (ps), REQUEST_PERMISSIONS);
        }
    }

    private void fillPermissionList (ArrayList<String> permissions, String permission)
    {
        if (ContextCompat.checkSelfPermission (this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissions.add (permission);
        }
    }
}
