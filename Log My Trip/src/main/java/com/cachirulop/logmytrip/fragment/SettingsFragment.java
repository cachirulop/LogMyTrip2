package com.cachirulop.logmytrip.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class SettingsFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
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
        if (SettingsManager.isAutostartOnConnect (ctx)) {
            p.setValue ("0");
        }
        else {
            p.setValue ("1");
        }

        setPrefsDependencies ();
    }

    public void setPrefsDependencies ()
    {
        Preference pref;

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_START_LOG_ALWAYS);
        pref.setEnabled (!SettingsManager.isAutoStartLogBluetooth (this.getActivity ()));

        pref = findPreference (SettingsManager.KEY_PREF_AUTO_START_LOG_BLUETOOTH);
        pref.setEnabled (!SettingsManager.isAutoStartLogAlways (this.getActivity ()));
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
}