package com.cachirulop.logmytrip.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.cachirulop.logmytrip.LogMyTripApplication;
import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.helper.GoogleDriveHelper;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.preferences.GoogleAccountSelector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

public class SettingsFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
                   GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener
{
    GoogleApiClient _client;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource (R.xml.preferences);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState)
    {
        super.onActivityCreated (savedInstanceState);

        ListPreference p;
        Context        ctx;

        ctx = getActivity ();

        p = (ListPreference) findPreference (SettingsManager.KEY_PREF_GPS_TIME_INTERVAL);
        p.setValue (Integer.toString (SettingsManager.getGpsTimeInterval (ctx)));

        p = (ListPreference) findPreference (SettingsManager.KEY_PREF_GPS_DISTANCE_INTERVAL);
        p.setValue (Integer.toString (SettingsManager.getGpsDistanceInterval (ctx)));

        p = (ListPreference) findPreference (SettingsManager.KEY_PREF_GPS_ACCURACY);
        p.setValue (Integer.toString (SettingsManager.getGpsAccuracy (ctx)));

        p = (ListPreference) findPreference (SettingsManager.KEY_PREF_AUTO_START_MODE);
        if (SettingsManager.isAutoStartOnConnect (ctx)) {
            p.setValue ("0");
        }
        else {
            p.setValue ("1");
        }

        setPrefsDependencies ();

        if (SettingsManager.isAutoSyncGoogleDrive (ctx)) {
            selectAutoSyncGoogleAccount ();
        }

        getView ().setFitsSystemWindows (true);
    }

    public void setPrefsDependencies ()
    {
        Preference pref;

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_START_LOG_ALWAYS);
        pref.setEnabled (!SettingsManager.isAutoStartLogBluetooth (this.getActivity ()));

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_START_LOG_BLUETOOTH);
        pref.setEnabled (!SettingsManager.isAutoStartLogAlways (this.getActivity ()));
    }

    private void selectAutoSyncGoogleAccount ()
    {
        Preference pref;
        String     account;

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_SYNC_GDRIVE_ACCOUNT);

        account = SettingsManager.getAutoSyncGoogleDriveAccount (getActivity ());

        if (SettingsManager.isAutoSyncGoogleDrive (getActivity ()) && "".equals (account)) {
            CharSequence[] accounts;

            accounts = GoogleAccountSelector.getGoogleAccountList (getActivity ());
            if (accounts.length == 0) {
                connectToDrive ();
            }
            else if (accounts.length == 1) {
                SettingsManager.setAutoSyncGoogleAccount (getActivity (), accounts[0].toString ());
            }
            else {
                ((GoogleAccountSelector) pref).show ();
            }
        }
    }

    private void connectToDrive ()
    {
        _client = GoogleDriveHelper.createClient (getActivity (), this, this);
        _client.connect ();
    }

    public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key)
    {
        if (SettingsManager.KEY_PREF_AUTO_START_LOG_BLUETOOTH.equals (key)) {
            if (SettingsManager.isAutoStartLogBluetooth (this.getActivity ())) {
                ServiceManager.startBluetooth (this.getActivity ());
            }
            else {
                ServiceManager.stopBluetooth (this.getActivity ());
            }

            setPrefsDependencies ();
        }
        else if (SettingsManager.KEY_PREF_AUTO_START_LOG_ALWAYS.equals (key)) {
            setPrefsDependencies ();
        }
        else if (SettingsManager.KEY_PREF_AUTO_SYNC_GDRIVE.equals (key)) {
            if (SettingsManager.isAutoSyncGoogleDrive (this.getActivity ())) {
                selectAutoSyncGoogleAccount ();
            }
            else {
                SettingsManager.setAutoSyncGoogleAccount (getActivity (), "");
            }
        }
        else if (SettingsManager.KEY_PREF_AUTO_SYNC_GDRIVE_ACCOUNT.equals (key)) {
            updateAutoSyncGoogleAccount ();
        }
    }

    private void updateAutoSyncGoogleAccount ()
    {
        Preference pref;
        String     account;

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_SYNC_GDRIVE_ACCOUNT);
        account = SettingsManager.getAutoSyncGoogleDriveAccount (getActivity ());
        if ("".equals (account)) {
            pref.setSummary ("");
        }
        else {
            pref.setSummary (String.format (getActivity ().getString (R.string.pref_autoSyncGoogleDriveAccountSummary),
                                            account));
        }
    }

    @Override
    public void onResume ()
    {
        super.onResume ();
        getPreferenceManager ().getSharedPreferences ()
                               .registerOnSharedPreferenceChangeListener (this);
    }

    @Override
    public void onPause ()
    {
        super.onPause ();
        getPreferenceScreen ().getSharedPreferences ()
                              .unregisterOnSharedPreferenceChangeListener (this);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if (requestCode == GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                LogMyTripApplication.runInMainThread (getActivity (), new Runnable ()
                {
                    @Override
                    public void run ()
                    {
                        selectAutoSyncGoogleAccount ();
                    }
                });
            }
        }
    }


    @Override
    public void onConnected (Bundle bundle)
    {
        selectAutoSyncGoogleAccount ();
        _client.disconnect ();
        _client = null;
    }

    @Override
    public void onConnectionSuspended (int i)
    {
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult)
    {
        if (connectionResult.hasResolution ()) {
            try {
                connectionResult.startResolutionForResult (getActivity (),
                                                           GoogleDriveHelper.RESOLVE_CONNECTION_REQUEST_CODE);
            }
            catch (IntentSender.SendIntentException e) {
            }
        }
        else {
            GooglePlayServicesUtil.getErrorDialog (connectionResult.getErrorCode (),
                                                   getActivity (),
                                                   0).show ();
        }
    }


    private String getAccountName ()
    {
        String accountName = "";

        AccountManager manager = (AccountManager) getActivity ().getSystemService (Activity.ACCOUNT_SERVICE);
        Account[]      list    = manager.getAccounts ();
        for (Account account : list) {
            if (account.type.equalsIgnoreCase ("com.google")) {
                accountName = account.name;
                break;
            }
        }

        return accountName;
    }

}