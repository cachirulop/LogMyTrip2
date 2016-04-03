package com.cachirulop.logmytrip.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

import java.util.Set;

public class BluetoothBroadcastReceiver
        extends BroadcastReceiver
{

    @Override
    public void onReceive (Context ctx, Intent intent)
    {
        String      action;
        BluetoothDevice device;
        boolean     isDeviceConfigured;
        Set<String> cfgDevices;

        action = intent.getAction ();
        device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);

        cfgDevices = SettingsManager.getBluetoothDeviceList (ctx);
        if (cfgDevices != null) {
            isDeviceConfigured = (cfgDevices.contains (device.getAddress ()));
        }
        else {
            isDeviceConfigured = false;
        }

        if (isDeviceConfigured) {
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals (action)) {
                if (SettingsManager.isAutoStartOnConnect (ctx)) {
                    ServiceManager.stopLog (ctx);
                }
                else {
                    ServiceManager.startLog (ctx);
                }
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals (action)) {
                if (SettingsManager.isAutoStartOnConnect (ctx)) {
                    ServiceManager.startLog (ctx);
                }
                else {
                    ServiceManager.stopLog (ctx);
                }
            }
        }
    }
}