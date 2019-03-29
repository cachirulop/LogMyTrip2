package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.LogMyTripNotificationManager;
import com.cachirulop.logmytrip.receiver.BluetoothBroadcastReceiver;

/**
 * Created by dmagro on 18/09/2015.
 */
public class BluetoothService
        extends Service
{
    private BluetoothBroadcastReceiver _btReceiver = null;

    private BroadcastReceiver _onLogReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            LogMyTripNotificationManager.updateWaitingBluetooth (BluetoothService.this);
        }
    };

    @Override
    public void onCreate ()
    {
        super.onCreate ();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        super.onStartCommand (intent, flags, startId);

        LogMyTripBroadcastManager.registerLogStartReceiver (this, _onLogReceiver);
        LogMyTripBroadcastManager.registerLogStopReceiver (this, _onLogReceiver);

        registerBluetoothReceiver ();
        startForegroundService ();

        LogMyTripBroadcastManager.sendStartBluetoothMessage (this);

        return START_STICKY;
    }

    private void registerBluetoothReceiver ()
    {
        if (_btReceiver == null) {
            _btReceiver = new BluetoothBroadcastReceiver ();

            registerReceiver (_btReceiver, new IntentFilter (BluetoothDevice.ACTION_ACL_DISCONNECTED));
            registerReceiver (_btReceiver, new IntentFilter (BluetoothDevice.ACTION_ACL_CONNECTED));
        }
    }

    private void startForegroundService ()
    {
        Notification note;

        try {
            note = LogMyTripNotificationManager.createWaitingBluetooth (this);

            startForeground (LogMyTripNotificationManager.NOTIFICATION_WAITING_BLUETOOTH, note);
        }
        catch (Exception e) {
            LogHelper.e ("Error on startForegroundService: " + e.getLocalizedMessage (), e);
        }
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy ();

        LogMyTripBroadcastManager.sendStopBluetoothMessage (this);

        LogMyTripBroadcastManager.unregisterReceiver (this, _onLogReceiver);

        unregisterBluetoothReceiver ();
        stopForegroundService ();
    }

    private void unregisterBluetoothReceiver ()
    {
        if (_btReceiver != null) {
            unregisterReceiver (_btReceiver);

            _btReceiver = null;
        }
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }
}
