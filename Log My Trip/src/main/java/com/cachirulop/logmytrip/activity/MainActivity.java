package com.cachirulop.logmytrip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class MainActivity
        extends AppCompatActivity
{
    private DrawerLayout          _drawer;
    private ActionBarDrawerToggle _drawerToggle;
    private Toolbar               _toolbar;
    private NavigationView        _nvDrawer;

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
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult (requestCode, resultCode, data);

        if (requestCode == GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE) {
            _mainFragment.onActivityResult (requestCode, resultCode, data);
        }
    }

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

        updateMenuItemState ();
    }

    private void updateMenuItemState ()
    {
        if (_menuAutoStartLog != null) {
            _menuAutoStartLog.setChecked (SettingsManager.isAutoStartLogBluetooth (this));
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
//        if (SettingsManager.isAutoSyncGoogleDrive (this)) {
//            SyncManager.syncDatabase (this);
//        }

        // Inflate the view
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        if (SettingsManager.isAutoStartLogBluetooth (this)) {
            ServiceManager.startBluetooth (this);
        }

        // Set a Toolbar to replace the ActionBar.
        _toolbar = (Toolbar) findViewById (R.id.toolbar);
        setSupportActionBar (_toolbar);

        _toolbar.setLogo (R.mipmap.ic_launcher);
        _toolbar.setTitle (R.string.app_name);

        // Drawer
        _drawer = (DrawerLayout) findViewById (R.id.drawer_layout);
        _drawerToggle = new ActionBarDrawerToggle (this,
                                                   _drawer,
                                                   _toolbar,
                                                   R.string.drawer_open,
                                                   R.string.drawer_close);
        _drawer.setDrawerListener (_drawerToggle);


        // Init the drawer content
        _nvDrawer = (NavigationView) findViewById (R.id.nvDrawer);
        setupDrawerContent ();

        loadFragment (R.id.action_journeys);

        // gets the main fragment
        // _mainFragment = (MainFragment) getSupportFragmentManager ().findFragmentById (R.id.fMainFragment);
    }

    private void setupDrawerContent ()
    {
        _nvDrawer.setNavigationItemSelectedListener (new NavigationView.OnNavigationItemSelectedListener ()
        {
            @Override
            public boolean onNavigationItemSelected (MenuItem menuItem)
            {
                selectMainContent (menuItem);
                return true;
            }
        });
    }

    public void selectMainContent (MenuItem menuItem)
    {
        loadFragment (menuItem.getItemId ());

        if (menuItem.getItemId () == R.id.action_journeys || menuItem.getItemId () == R.id.action_trips) {
            menuItem.setChecked (true);
            setTitle (menuItem.getTitle ());
        }

        _drawer.closeDrawers ();
    }

    private void loadFragment (int itemId)
    {
        Fragment fragment      = null;
        Class    fragmentClass = null;

        switch (itemId) {
            case R.id.action_journeys:
                fragmentClass = MainFragment.class;
                break;

            //            case R.id.nav_second_fragment:
            //                // fragmentClass = SecondFragment.class;
            //                break;
            //
            //            case R.id.nav_third_fragment:
            //                // fragmentClass = ThirdFragment.class;
            //                break;

            case R.id.action_settings:
                showPreferences ();
                break;

            default:
                fragmentClass = MainFragment.class;

        }

        if (fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance ();
            }
            catch (Exception e) {
                e.printStackTrace ();
            }

            FragmentManager fragmentManager = getSupportFragmentManager ();

            fragmentManager.beginTransaction ().replace (R.id.flContent, fragment).commit ();
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
        if (_drawerToggle.onOptionsItemSelected (item)) {
            return true;
        }

        switch (item.getItemId ()) {
            case android.R.id.home:
                _drawer.openDrawer (GravityCompat.START);
                return true;

            case R.id.action_settings:
                showPreferences ();
                return true;

            case R.id.action_auto_start_log:
                if (SettingsManager.isAutoStartLogBluetooth (this)) {
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
        startActivity (new Intent (this, SettingsActivity.class));
    }
}
