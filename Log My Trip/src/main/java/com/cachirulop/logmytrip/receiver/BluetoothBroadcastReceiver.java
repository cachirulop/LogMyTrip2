
package com.cachirulop.logmytrip.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.cachirulop.logmytrip.manager.ServiceManager;

public class BluetoothBroadcastReceiver
        extends BroadcastReceiver
{

    @Override
    public void onReceive (Context ctx,
                           Intent intent)
    {
        String action;
        BluetoothDevice device;

        action = intent.getAction ();
        device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

        Toast.makeText (ctx,
                        "Bluetooth broadcast receive: action: " + action + ", device: " + device.getAddress () + " - " + device.getName (),
                        Toast.LENGTH_SHORT).show ();

        // TODO: Test if the device is configured
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals (action)) {
            Toast.makeText (ctx, "Device disconnected", Toast.LENGTH_LONG).show ();
            
            ServiceManager.stopSaveTrip (ctx);
        }
        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals (action)) {
            Toast.makeText (ctx, "Device connected", Toast.LENGTH_LONG).show ();

            ServiceManager.startSaveTrip (ctx);
        }        
    }
}