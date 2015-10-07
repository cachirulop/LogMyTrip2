package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.receiver.LocationReceiver;
import com.cachirulop.logmytrip.util.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LogMyTripService
        extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener
{
    public static final String BROADCAST_ACTION_SAVE_TRIP_START = "com.cachirulop.logmytrip.saveTripStatusChange.start";
    public static final String BROADCAST_ACTION_SAVE_TRIP_STOP  = "com.cachirulop.logmytrip.saveTripStatusChange.stop";
    public static final String BROADCAST_EXTRA_TRIP             = "com.cachirulop.logmytrip.saveTripStatusChange.trip";
    PendingIntent _pendingIntent;
    private GoogleApiClient _apiClient;
    private LocationRequest _locationRequest;
    private Trip                              _currentTrip                  = null;
    private List<OnTripLocationSavedListener> _onTripLocationSavedListeners = new ArrayList<> ();
    private LogMyTripServiceBinder _binder = new LogMyTripServiceBinder ();

    @Override
    public void onCreate ()
    {
        super.onCreate ();

        initLocation ();
    }

    private void initLocation ()
    {
        _locationRequest = LocationRequest.create ();
        _locationRequest.setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY);
        _locationRequest.setInterval (SettingsManager.getGpsTimeInterval (this));
        _locationRequest.setFastestInterval (SettingsManager.getGpsTimeInterval (this));
        _locationRequest.setSmallestDisplacement (SettingsManager.getGpsDistanceInterval (this));

        ensureLocationClient ();
    }

    private void ensureLocationClient ()
    {
        if (_apiClient == null) {
            _apiClient = new GoogleApiClient.Builder (this).addConnectionCallbacks (this)
                                                           .addOnConnectionFailedListener (this)
                                                           .addApi (LocationServices.API)
                                                           .build ();
        }
    }

    @Override
    public void onDestroy ()
    {
        if (_apiClient != null) {
            stopLog ();
            stopForegroundService ();
        }

        super.onDestroy ();
    }

    private void stopLog ()
    {
        sendBroadcastMessage (BROADCAST_ACTION_SAVE_TRIP_STOP);

        ensureLocationClient ();
        if (_apiClient.isConnected () || _apiClient.isConnecting ()) {
            // LocationServices.FusedLocationApi.removeLocationUpdates (_apiClient, this);
            Intent intent;
            PendingIntent locationIntent;

            intent = new Intent (this, LocationReceiver.class);

            locationIntent = PendingIntent.getBroadcast (getApplicationContext (), 0, intent,
                                                         PendingIntent.FLAG_CANCEL_CURRENT);

            LocationServices.FusedLocationApi.removeLocationUpdates (_apiClient, locationIntent);
        }

        if (_currentTrip != null) {
            _currentTrip = null;
        }
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
    }

    private void sendBroadcastMessage (String action)
    {
        Intent intent;

        intent = new Intent (action);
        intent.putExtra (BROADCAST_EXTRA_TRIP, _currentTrip);

        LocalBroadcastManager.getInstance (this)
                             .sendBroadcast (intent);
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId)
    {
        if (!isGooglePlayServicesAvailable ()) {
            ToastHelper.showLong (this, getString (R.string.msg_GooglePlayServicesUnavailable));

            return Service.START_NOT_STICKY;
        }
        else {
            ToastHelper.showDebug (this, "LogMyTripService.onStartCommand: starting service");
        }

        super.onStartCommand (intent, flags, startId);

        boolean logs;

        startLog ();
        startForegroundService ();

        return START_STICKY;
    }

    private boolean isGooglePlayServicesAvailable ()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable (this);

        return (ConnectionResult.SUCCESS == resultCode);
    }

    private void startLog ()
    {
        ensureLocationClient ();
        if (!_apiClient.isConnected () && !_apiClient.isConnecting ()) {
            _apiClient.connect ();
        }
    }
/*
    @Override
    public void onLocationChanged (Location loc)
    {
        final TripLocation tl;
        final Context      ctx;

        ctx = this;

        if (isValidLocation (loc)) {
            tl = new TripLocation (loc);

            new Thread ()
            {
                public void run ()
                {
                    TripManager.saveTripLocation (ctx, tl);

                    for (OnTripLocationSavedListener l : _onTripLocationSavedListeners) {
                        l.onTripLocationSaved (tl);
                    }
                }
            }.start ();

            ToastHelper.showShortDebug (this, "LogMyTripService.onLocationChanged: " +
                    loc.getLatitude () + "-.-" +
                    loc.getLongitude ());
        }
        else {
            ToastHelper.showShortDebug (this,
                                        "LogMyTripService.onLocationChanged: ignoring location, bad accuracy");
        }

    }
*/
    //    private boolean isValidLocation (Location location)
    //    {
    //        return (location.hasAccuracy () && location.getAccuracy () <= SettingsManager.getGpsAccuracy (
    //                this)) && (location != null && Math.abs (
    //                location.getLatitude ()) <= 90 && Math.abs (location.getLongitude ()) <= 180);
    //    }
    //

    private void startForegroundService ()
    {
        Notification note;
        CharSequence contentText;

        _currentTrip = TripManager.getTodayTrip (this);
        if (_currentTrip == null) {
            _currentTrip = TripManager.createTodayTrip (this);
        }

        sendBroadcastMessage (BROADCAST_ACTION_SAVE_TRIP_START);

        contentText = String.format (this.getText (R.string.notif_ContentSavingTrip)
                                         .toString (), _currentTrip.getDescription ());

        note = NotifyManager.createSavingTrip (this, contentText);

        startForeground (NotifyManager.NOTIFICATION_SAVING_TRIP, note);
    }

    public void registerTripLocationSavedListener (OnTripLocationSavedListener listener)
    {
        _onTripLocationSavedListeners.add (listener);
    }

    public void unregisterTripLocationSavedListener (OnTripLocationSavedListener listener)
    {
        _onTripLocationSavedListeners.remove (listener);
    }

    @Override
    public void onConnectionFailed (ConnectionResult arg0)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected (Bundle arg0)
    {
        Intent        intent;
        PendingIntent locationIntent;

        intent = new Intent (this, LocationReceiver.class);
        // intent.putExtra (LocationReceiver.KEY_CURRENT_TRIP_ID, _currentTrip.getId ());

        locationIntent = PendingIntent.getBroadcast (getApplicationContext (), 0, intent,
                                                     PendingIntent.FLAG_CANCEL_CURRENT);

        LocationServices.FusedLocationApi.requestLocationUpdates (_apiClient, _locationRequest,
                                                                  locationIntent);
    }

    @Override
    public void onConnectionSuspended (int i)
    {

    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return _binder;
    }

    public interface OnTripLocationSavedListener
    {
        void onTripLocationSaved (TripLocation tl);
    }

    public class LogMyTripServiceBinder
            extends Binder
    {
        public LogMyTripService getService ()
        {
            return LogMyTripService.this;
        }
    }

}
