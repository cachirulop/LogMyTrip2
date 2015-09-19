package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
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

        boolean bluetooth;
        boolean logs;

        bluetooth = SettingsManager.getAutoStartLog (this);
        logs = SettingsManager.isLogTrip (this);

        synchronized (_lckReceiver) {
            if (bluetooth) {
                registerBluetoothReceiver ();
            }
            else {
                unregisterBluetoothReceiver ();
            }
        }

        if (bluetooth || logs) {
            startForegroundService (bluetooth, logs);
        }
        else {
            stopForegroundService ();
        }

        if (intent.hasExtra (LogMyTripService.EXTRA_SERVICE_MESSAGE_HANDLER)) {
            Messenger messenger = (Messenger) intent.getExtras ()
                                                    .get (LogMyTripService.EXTRA_SERVICE_MESSAGE_HANDLER);
            Message msg = Message.obtain ();

            // msg.obj = _currentTrip;

            try {
                messenger.send (msg);
            }
            catch (android.os.RemoteException e1) {
                Log.w (getClass ().getName (), "Exception sending message", e1);
            }
        }

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

    private void unregisterBluetoothReceiver ()
    {
        if (_btReceiver != null) {
            unregisterReceiver (_btReceiver);

            _btReceiver = null;
        }
    }

    private void startForegroundService (boolean bluetooth, boolean logTrip)
    {
        Notification note;
        CharSequence contentText;

        contentText = this.getText (R.string.notif_ContentWaitingBluetooth);

        // TODO: Specify the correct icon
        note = NotifyManager.createNotification (this, contentText);

        startForeground (NotifyManager.NOTIFICATION_ID, note);
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
