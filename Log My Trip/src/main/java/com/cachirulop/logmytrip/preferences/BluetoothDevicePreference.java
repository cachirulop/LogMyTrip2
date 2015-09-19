package com.cachirulop.logmytrip.preferences;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import com.cachirulop.logmytrip.R;

import java.util.Set;

public class BluetoothDevicePreference
        extends MultiSelectListPreference
{

    public BluetoothDevicePreference (Context context)
    {
        this (context, null);
    }

    public BluetoothDevicePreference (Context context, AttributeSet attrs)
    {
        super (context, attrs);

        CharSequence[] entries;
        CharSequence[] entryValues;
        BluetoothAdapter bta;

        bta = BluetoothAdapter.getDefaultAdapter ();
        if (bta != null) {
            Set<BluetoothDevice> pairedDevices;
            int i;

            // TODO: Show dialog when no paired devices found

            pairedDevices = bta.getBondedDevices ();
            entries = new CharSequence[pairedDevices.size ()];
            entryValues = new CharSequence[pairedDevices.size ()];
            i = 0;
            for (BluetoothDevice dev : pairedDevices) {
                entries[i] = dev.getName ();
                if (entries[i].toString ()
                              .equals ("")) {
                    entries[i] = dev.getAddress ();
                }

                entryValues[i] = dev.getAddress ();

                i++;
            }
        }
        else {
            entries = new CharSequence[1];
            entryValues = new CharSequence[1];
            entries[0] = context.getText (R.string.pref_bluetoothNotFound);
            entryValues[0] = entries[0];
        }

        setEntries (entries);
        setEntryValues (entryValues);
    }
}
