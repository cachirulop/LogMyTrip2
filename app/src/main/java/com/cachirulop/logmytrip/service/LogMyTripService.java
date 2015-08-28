
package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.BluetoothBroadcastReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LogMyTripService
        extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   LocationListener
{
    private static final long LOCATION_UPDATE_INTERVAL = 10000;
    private static final long LOCATION_UPDATE_FASTEST_INTERVAL = 5000;
    private final Object _lckReceiver = new Object();
    private GoogleApiClient            _apiClient;
    private LocationRequest            _locationRequest;
    private BluetoothBroadcastReceiver _btReceiver;
    private Trip                       _currentTrip                     = null;

    @Override
    public void onCreate ()
    {
        super.onCreate ();

        initLocation();
    }

    @Override
    public void onDestroy ()
    {
        if (_apiClient != null) {
            stopLog ();
        }

        super.onDestroy();
    }

    private void initLocation ()
    {
        _locationRequest = LocationRequest.create ();
        _locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //_locationRequest.setPriority (LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        _locationRequest.setInterval (LOCATION_UPDATE_INTERVAL);
        _locationRequest.setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);

        ensureLocationClient ();
    }

    private void ensureLocationClient ()
    {
        if (_apiClient == null) {
            _apiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();
        }
    }

    @Override
    public int onStartCommand (Intent intent,
                               int flags,
                               int startId)
    {

        if (!isGooglePlayServicesAvailable ()) {
            ToastHelper.showLong (this,
                                  getString (R.string.msg_GooglePlayServicesUnavailable));

            return Service.START_NOT_STICKY;
        }
        else {
            ToastHelper.showDebug (this,
                                   "LogMyTripService.onStartCommand: starting service");

        }

        super.onStartCommand (intent,
                              flags,
                              startId);

        boolean bluetooth;
        boolean logs;

        bluetooth = SettingsManager.getAutoStartLog (this);
        logs = SettingsManager.isLogTrip(this);

        synchronized (_lckReceiver) {
            if (bluetooth) {
                registerBluetoothReceiver ();
            }
            else {
                unregisterBluetoothReceiver ();
            }
        }

        if (logs) {
            startLog ();
        }
        else {
            stopLog ();
        }

        if (bluetooth || logs) {
            startForegroundService (bluetooth,
                                    logs);
        }
        else {
            stopForegroundService();
        }

        return START_STICKY;
    }

    private void startLog ()
    {
        ensureLocationClient ();
        if (!_apiClient.isConnected () && !_apiClient.isConnecting ()) {
            _apiClient.connect();
        }
    }

    private void stopLog ()
    {
        if (_currentTrip != null) {
            _currentTrip = null;
        }

        ensureLocationClient();
        if (_apiClient.isConnected () || _apiClient.isConnecting ()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(_apiClient, this);
        }
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf();
    }

    private void startForegroundService (boolean bluetooth,
                                         boolean logTrip)
    {
        Notification note;
        int contentId;

        if (bluetooth) {
            contentId = R.string.notif_ContentWaitingBluetooth;
        }
        else {
            contentId = R.string.notif_ContentSavingTrip;
        }

        // TODO: Specify the correct icon
        note = NotifyManager.createNotification (this,
                                                 contentId);

        startForeground (NotifyManager.NOTIFICATION_ID,
                         note);
    }

    private void registerBluetoothReceiver ()
    {
        if (_btReceiver == null) {
            _btReceiver = new BluetoothBroadcastReceiver ();

            registerReceiver (_btReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

            registerReceiver (_btReceiver,
                              new IntentFilter (BluetoothDevice.ACTION_ACL_CONNECTED));
        }
    }

    private void unregisterBluetoothReceiver ()
    {
        if (_btReceiver != null) {
            unregisterReceiver (_btReceiver);

            _btReceiver = null;
        }
    }

    private boolean isGooglePlayServicesAvailable ()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable (this);

        return (ConnectionResult.SUCCESS == resultCode);
    }

    @Override
    public void onLocationChanged (Location loc)
    {
        if (_currentTrip == null) {
            _currentTrip = TripManager.getCurrentTrip(this);
        }

        if (_currentTrip != null) {
            NotifyManager.showNotification(this, R.string.notif_ContentSavingTripWithDesc, _currentTrip.getDescription());
        }

        TripLocation tl;
        
        tl = convertLocation (loc);
        TripManager.saveTripLocation (this, tl);

        ToastHelper.showShortDebug (this,
                "LogMyTripService.onLocationChanged: " +
                        loc.getLatitude() + "-.-" +
                        loc.getLongitude());

    }
    
    private TripLocation convertLocation (Location loc)
    {
        TripLocation result;
        
        result = new TripLocation();
        result.setIdTrip (_currentTrip.getId ());
        result.setLocationTime (loc.getTime ());
        result.setLatitude (loc.getLatitude ());
        result.setLongitude (loc.getLongitude ());
        result.setAltitude (loc.getAltitude ());
        result.setSpeed (loc.getSpeed ());
        
        return result;
    }

    @Override
    public void onConnectionFailed (ConnectionResult arg0)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected (Bundle arg0)
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(_apiClient,
                _locationRequest,
                this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }

}
