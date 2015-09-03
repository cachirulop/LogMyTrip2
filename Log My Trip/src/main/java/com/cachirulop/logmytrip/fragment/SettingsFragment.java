
package com.cachirulop.logmytrip.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.ServiceManager;

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

    public void onSharedPreferenceChanged (SharedPreferences sharedPreferences,
                                           String key)
    {
        // if (SettingsManager.KEY_PREF_AUTO_START_LOG.equals (key)) {
        //    if (SettingsManager.getAutoStartLog (this.getActivity ())) {
                ServiceManager.startStopService (this.getActivity ());
        //    }
        //    else {
        //        ServiceManager.stopBluetoothService (this.getActivity ());
        //    }
        //}
    }

    @Override
    public void onResume ()
    {
        super.onResume ();
        getPreferenceManager ().getSharedPreferences ().registerOnSharedPreferenceChangeListener (this);
    }

    @Override
    public void onPause ()
    {
        super.onPause ();
        getPreferenceScreen ().getSharedPreferences ().unregisterOnSharedPreferenceChangeListener (this);
    }
}