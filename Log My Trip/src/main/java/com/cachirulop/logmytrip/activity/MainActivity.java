package com.cachirulop.logmytrip.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.data.LogMyTripDataHelper;
import com.cachirulop.logmytrip.fragment.IMainFragment;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.InputStream;

public class MainActivity
        extends AppCompatActivity
{
    private final int REQUEST_CODE_GOOGLE_SIGN_API = 1001;

    private DrawerLayout          _drawer;
    private ActionBarDrawerToggle _drawerToggle;
    private Toolbar               _toolbar;
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
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult (requestCode, resultCode, data);

        switch (requestCode) {
            case GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE:
                _mainFragment.onMainActivityResult (requestCode, resultCode, data);
                break;

            case REQUEST_CODE_GOOGLE_SIGN_API:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent (data);

                loadUserInfo (result);

                break;
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

        updateAutoStartLogSwitch ();
    }

    private void updateAutoStartLogSwitch ()
    {
        if (_swAutoStartLog != null) {
            _swAutoStartLog.setChecked (SettingsManager.isAutoStartLogBluetooth (this));
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

        _nvDrawer = (NavigationView) findViewById (R.id.nvDrawer);

        setupDrawerContent ();
        loadFragment (R.id.action_journeys);

        requestUserInfo ();
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
                _swAutoStartLog = (Switch) autoStartLog.getActionView ()
                                                       .findViewById (R.id.switch1);
                _swAutoStartLog.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener ()
                {
                    @Override
                    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
                    {
                        toggleAutoStartLog ();
                    }
                });
            }
        }

        _nvDrawer.setNavigationItemSelectedListener (new NavigationView.OnNavigationItemSelectedListener ()
        {
            @Override
            public boolean onNavigationItemSelected (MenuItem menuItem)
            {
                onDrawerMenuOptionsItemSelected (menuItem);
                return true;
            }
        });
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
        Fragment fragment      = null;
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

    private void requestUserInfo ()
    {
        if (SettingsManager.isAutoSyncGoogleDrive (this)) {
            GoogleApiClient client;
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail ()
                    .setAccountName (SettingsManager.getAutoSyncGoogleDriveAccount (this))
                    .build ();

            client = new GoogleApiClient.Builder (this).enableAutoManage (this, null)
                                                       .addApi (Auth.GOOGLE_SIGN_IN_API, gso)
                                                       .build ();


            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent (client);
            startActivityForResult (signInIntent, REQUEST_CODE_GOOGLE_SIGN_API);
        }
    }

    private void loadUserInfo (GoogleSignInResult result)
    {
        if (result.isSuccess ()) {
            GoogleSignInAccount acct = result.getSignInAccount ();
            DownloadImageTask task;
            ImageView photo;
            TextView txt;

            photo = (ImageView) findViewById (R.id.iv_profile);

            task = new DownloadImageTask (photo);
            task.execute (acct.getPhotoUrl ().toString ());

            txt = (TextView) findViewById (R.id.tv_display_name);
            txt.setText (acct.getDisplayName ());

            txt = (TextView) findViewById (R.id.tv_email);
            txt.setText (acct.getEmail ());
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

                this makes an infinite loop !!!!!

                    toggleAutoStartLog ();
                updateAutoStartLogSwitch ();

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

    private class DownloadImageTask
            extends AsyncTask<String, Void, Bitmap>
    {
        ImageView _image;

        public DownloadImageTask (ImageView bmImage)
        {
            _image = bmImage;
        }

        protected Bitmap doInBackground (String... urls)
        {
            Bitmap bmp = null;

            try {
                InputStream in;

                in = new java.net.URL (urls[0]).openStream ();

                bmp = BitmapFactory.decodeStream (in);
            }
            catch (Exception e) {
                LogHelper.e ("Can't load image: " + e.getMessage ());
            }

            return bmp;
        }

        protected void onPostExecute (Bitmap result)
        {
            _image.setImageBitmap (result);
        }
    }
}
