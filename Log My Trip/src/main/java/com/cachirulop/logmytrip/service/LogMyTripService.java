package com.cachirulop.logmytrip.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.manager.NotifyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;
import com.cachirulop.logmytrip.util.ToastHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LogMyTripService
        extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                   GoogleApiClient.OnConnectionFailedListener,
                   LocationListener
{
    public static final String EXTRA_SERVICE_MESSAGE_HANDLER = LogMyTripService.class.getCanonicalName () + ".HANDLER";

    private GoogleApiClient            _apiClient;
    private LocationRequest            _locationRequest;
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
        }

        super.onDestroy ();
    }

    private void stopLog ()
    {
        if (_currentTrip != null) {
            _currentTrip = null;
        }

        ensureLocationClient ();
        if (_apiClient.isConnected () || _apiClient.isConnecting ()) {
            LocationServices.FusedLocationApi.removeLocationUpdates (_apiClient, this);
        }
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

        logs = SettingsManager.isLogTrip (this);
        if (logs) {
            startLog ();
        }
        else {
            stopLog ();
        }

        if (logs) {
            startForegroundService ();
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

    private void startForegroundService ()
    {
        Notification note;
        CharSequence contentText;

        _currentTrip = TripManager.getCurrentTrip (this);

        contentText = String.format (this.getText (R.string.notif_ContentSavingTrip)
                                         .toString (), _currentTrip.getDescription ());

        note = NotifyManager.createSavingTrip (this, contentText);

        startForeground (NotifyManager.NOTIFICATION_SAVING_TRIP, note);
    }

    private void stopForegroundService ()
    {
        stopForeground (true);
        stopSelf ();
    }

    @Override
    public void onLocationChanged (Location loc)
    {
        final TripLocation tl;
        final Context      ctx;

        ctx = this;

        if (isValidLocation (loc)) {
            tl = convertLocation (loc);

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

    private boolean isValidLocation (Location location)
    {
        return (location.hasAccuracy () && location.getAccuracy () <= SettingsManager.getGpsAccuracy (
                this)) && (location != null && Math.abs (
                location.getLatitude ()) <= 90 && Math.abs (location.getLongitude ()) <= 180);
    }

    private TripLocation convertLocation (Location loc)
    {
        TripLocation result;

        result = new TripLocation ();
        result.setIdTrip (_currentTrip.getId ());

        if (loc.getTime () == 0L) {
            // Some devices don't set the time field
            result.setLocationTime (System.currentTimeMillis ());
        }
        else {
            result.setLocationTime (loc.getTime ());
        }

        result.setLatitude (loc.getLatitude ());
        result.setLongitude (loc.getLongitude ());
        result.setAltitude (loc.getAltitude ());
        result.setSpeed (loc.getSpeed ());
        result.setAccuracy (loc.getAccuracy ());
        result.setBearing (loc.getBearing ());

        return result;
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
        LocationServices.FusedLocationApi.requestLocationUpdates (_apiClient, _locationRequest,
                                                                  this);
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
