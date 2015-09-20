package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.receiver.BluetoothBroadcastReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;

/**
 * Created by dmagro on 18/09/2015.
 */
public class BluetoothService
        extends Service
{
    private final Object _lckReceiver = new Object ();
    private BluetoothBroadcastReceiver _btReceiver;

    @Override
    public void onCreate ()
    {
        super.onCreate ();
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy ();
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {

        ToastHelper.showDebug (this, "BluetoothService.onStartCommand: starting service");

        super.onStartCommand (intent, flags, startId);

        registerBluetoothReceiver ();
        // unregisterBluetoothReceiver ();

        startForegroundService ();
        // stopForegroundService ();

        return START_STICKY;
    }

    private void registerBluetoothReceiver ()
    {
        if (_btReceiver == null) {
            _btReceiver = new BluetoothBroadcastReceiver ();

            registerReceiver (_btReceiver,
                              new IntentFilter (BluetoothDevice.ACTION_ACL_DISCONNECTED));

            registerReceiver (_btReceiver, new IntentFilter (BluetoothDevice.ACTION_ACL_CONNECTED));
        }
    }

    private void startForegroundService ()
    {
        Notification note;
        CharSequence contentText;

        contentText = this.getText (R.string.notif_ContentWaitingBluetooth);

        note = NotifyManager.createWaitingBluetooth (this, contentText);

        startForeground (NotifyManager.NOTIFICATION_WAITING_BLUETOOTH, note);
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
