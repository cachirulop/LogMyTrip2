package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.LogMyTripBroadcastManager;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.receiver.BluetoothBroadcastReceiver;

/**
 * Created by dmagro on 18/09/2015.
 */
public class BluetoothService
        extends Service
{
    private BluetoothBroadcastReceiver _btReceiver = null;

    private BroadcastReceiver _onTripLogReceiver = new BroadcastReceiver ()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            NotifyManager.updateWaitingBluetooth (BluetoothService.this, getNotificationText ());
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

        LogMyTripBroadcastManager.registerTripLogStartReceiver (this, _onTripLogReceiver);
        LogMyTripBroadcastManager.registerTripLogStopReceiver (this, _onTripLogReceiver);

        registerBluetoothReceiver ();
        startForegroundService ();

        LogMyTripBroadcastManager.sendStartBluetoothMessage (this);

        return START_STICKY;
    }

    @Override
    public void onDestroy ()
    {
        super.onDestroy ();

        LogMyTripBroadcastManager.sendStopBluetoothMessage (this);

        LogMyTripBroadcastManager.unregisterReceiver (this, _onTripLogReceiver);
        //LogMyTripBroadcastManager.unregisterReceiver (this, _onTripLogStopReceiver);

        unregisterBluetoothReceiver ();
        stopForegroundService ();
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
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

    private void unregisterBluetoothReceiver ()
    {
        if (_btReceiver != null) {
            unregisterReceiver (_btReceiver);

            _btReceiver = null;
        }
    }

    private void startForegroundService ()
    {
        Notification note;
        CharSequence contentText;

        contentText = getNotificationText ();

        note = NotifyManager.createWaitingBluetooth (this, contentText);

        startForeground (NotifyManager.NOTIFICATION_WAITING_BLUETOOTH, note);
    }

    private String getNotificationText ()
    {
        String result;
        String mode;
        String action;

        if (SettingsManager.isAutostartOnConnect (this)) {
            mode = this.getText (R.string.notif_ContentWaitingBluetooth_mode_connect).toString ();
        }
        else {
            mode = this.getText (R.string.notif_ContentWaitingBluetooth_mode_disconnect)
                       .toString ();
        }

        if (SettingsManager.isLogTrip (this)) {
            action = this.getText (R.string.notif_ContentWaitingBluetooth_action_stop).toString ();
        }
        else {
            action = this.getText (R.string.notif_ContentWaitingBluetooth_action_start).toString ();
        }

        return String.format (this.getText (R.string.notif_ContentWaitingBluetooth).toString (),
                              mode,
                              action);
    }
}
