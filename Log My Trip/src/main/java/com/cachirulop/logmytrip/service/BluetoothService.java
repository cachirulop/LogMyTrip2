package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.BluetoothBroadcastReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationServices;

/**
 * Created by dmagro on 18/09/2015.
 */
public class BluetoothService extends Service {
    private final Object _lckReceiver = new Object();
    private BluetoothBroadcastReceiver _btReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startId) {

        ToastHelper.showDebug(this,
                "BluetoothService.onStartCommand: starting service");


        super.onStartCommand(intent,
                flags,
                startId);

        boolean bluetooth;
        boolean logs;

        bluetooth = SettingsManager.getAutoStartLog(this);
        logs = SettingsManager.isLogTrip(this);

        synchronized (_lckReceiver) {
            if (bluetooth) {
                registerBluetoothReceiver();
            } else {
                unregisterBluetoothReceiver();
            }
        }

        if (bluetooth || logs) {
            startForegroundService(bluetooth,
                    logs);
        } else {
            stopForegroundService();
        }

        if (intent.hasExtra(LogMyTripService.EXTRA_SERVICE_MESSAGE_HANDLER)) {
            Messenger messenger = (Messenger) intent.getExtras().get(LogMyTripService.EXTRA_SERVICE_MESSAGE_HANDLER);
            Message msg = Message.obtain();

            // msg.obj = _currentTrip;

            try {
                messenger.send(msg);
            } catch (android.os.RemoteException e1) {
                Log.w(getClass().getName(), "Exception sending message", e1);
            }
        }

        return START_STICKY;
    }

    private void startLog() {
        ensureLocationClient();
        if (!_apiClient.isConnected() && !_apiClient.isConnecting()) {
            _apiClient.connect();
        }
    }

    private void stopLog() {
        if (_currentTrip != null) {
            _currentTrip = null;
        }

        ensureLocationClient();
        if (_apiClient.isConnected() || _apiClient.isConnecting()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(_apiClient, this);
        }
    }

    private void stopForegroundService() {
        stopForeground(true);
        stopSelf();
    }

    private void startForegroundService(boolean bluetooth,
                                        boolean logTrip) {
        Notification note;
        CharSequence contentText;

        _currentTrip = TripManager.getCurrentTrip(this);

        if (bluetooth) {
            contentText = this.getText(R.string.notif_ContentWaitingBluetooth);
        } else {
            contentText = String.format(this.getText(R.string.notif_ContentSavingTrip).toString(), _currentTrip.getDescription());
        }

        // TODO: Specify the correct icon
        note = NotifyManager.createNotification(this,
                contentText);

        startForeground(NotifyManager.NOTIFICATION_ID,
                note);
    }

    private void registerBluetoothReceiver() {
        if (_btReceiver == null) {
            _btReceiver = new BluetoothBroadcastReceiver();

            registerReceiver(_btReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

            registerReceiver(_btReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        }
    }

    private void unregisterBluetoothReceiver() {
        if (_btReceiver != null) {
            unregisterReceiver(_btReceiver);

            _btReceiver = null;
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        return (ConnectionResult.SUCCESS == resultCode);
    }

    @Override
    public void onLocationChanged(Location loc) {
        TripLocation tl;

        if (isValidLocation(loc)) {
            tl = convertLocation(loc);
            TripManager.saveTripLocation(this, tl);

            ToastHelper.showShortDebug(this,
                    "LogMyTripService.onLocationChanged: " +
                            loc.getLatitude() + "-.-" +
                            loc.getLongitude());
        } else {
            ToastHelper.showShortDebug(this,
                    "LogMyTripService.onLocationChanged: ignoring location, bad accuracy");
        }

    }

    private TripLocation convertLocation(Location loc) {
        TripLocation result;

        result = new TripLocation();
        result.setIdTrip(_currentTrip.getId());

        if (loc.getTime() == 0L) {
            // Some devices don't set the time field
            result.setLocationTime(System.currentTimeMillis());
        } else {
            result.setLocationTime(loc.getTime());
        }

        result.setLatitude(loc.getLatitude());
        result.setLongitude(loc.getLongitude());
        result.setAltitude(loc.getAltitude());
        result.setSpeed(loc.getSpeed());
        result.setAccuracy(loc.getAccuracy());
        result.setBearing(loc.getBearing());

        return result;
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected(Bundle arg0) {
        LocationServices.FusedLocationApi.requestLocationUpdates(_apiClient,
                _locationRequest,
                this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
