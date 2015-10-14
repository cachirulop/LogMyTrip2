package com.cachirulop.logmytrip.entity;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class TripLocation
        implements Serializable
{
    private long  _id;
    private long  _idTrip;
    private long  _locationTime;
    private double _latitude;
    private double _longitude;
    private double _altitude;
    private float _speed;
    private float  _accuracy;
    private float  _bearing;
    private String _provider;

    public TripLocation ()
    {
    }

    public TripLocation (Trip trip, Location loc)
    {
        setIdTrip (trip.getId ());

        if (loc.getTime () == 0L) {
            // Some devices don't set the time field
            setLocationTime (System.currentTimeMillis ());
        }
        else {
            setLocationTime (loc.getTime ());
        }

        setLatitude (loc.getLatitude ());
        setLongitude (loc.getLongitude ());
        setAltitude (loc.getAltitude ());
        setSpeed (loc.getSpeed ());
        setAccuracy (loc.getAccuracy ());
        setBearing (loc.getBearing ());
        setProvider (loc.getProvider ());
    }

    public float getAccuracy ()
    {
        return _accuracy;
    }

    public void setAccuracy (float _accuracy)
    {
        this._accuracy = _accuracy;
    }

    public float getBearing ()
    {
        return _bearing;
    }

    public void setBearing (float _bearing)
    {
        this._bearing = _bearing;
    }

    /**
     * @return the id
     */
    public long getId ()
    {
        return _id;
    }

    /**
     * @param id the id to set
     */
    public void setId (long id)
    {
        this._id = id;
    }

    /**
     * @return the tripId
     */
    public long getIdTrip ()
    {
        return _idTrip;
    }

    /**
     * @param idTrip the tripId to set
     */
    public void setIdTrip (long idTrip)
    {
        this._idTrip = idTrip;
    }

    /**
     * @return the time
     */
    public long getLocationTime ()
    {
        return _locationTime;
    }

    /**
     * @param time the time to set
     */
    public void setLocationTime (long time)
    {
        this._locationTime = time;
    }

    public Date getLocationTimeAsDate ()
    {
        return new Date (_locationTime);
    }

    public Calendar getLocationTimeAsCalendar ()
    {
        Calendar result;

        result = Calendar.getInstance ();
        result.setTimeInMillis (_locationTime);

        return result;
    }

    /**
     * @return the altitude
     */
    public double getAltitude ()
    {
        return _altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude (double altitude)
    {
        this._altitude = altitude;
    }

    /**
     * @return the speed
     */
    public float getSpeed ()
    {
        return _speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed (float speed)
    {
        this._speed = speed;
    }

    public String getProvider ()
    {
        return _provider;
    }

    public void setProvider (String provider)
    {
        _provider = provider;
    }

    public LatLng toLatLng ()
    {
        LatLng result;

        result = new LatLng (this.getLatitude (), this.getLongitude ());

        return result;
    }

    /**
     * @return the latitude
     */
    public double getLatitude ()
    {
        return _latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude (double latitude)
    {
        this._latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude ()
    {
        return _longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude (double longitude)
    {
        this._longitude = longitude;
    }

    public Location toLocation ()
    {
        Location result;

        result = new Location (_provider);
        result.setAccuracy (_accuracy);
        result.setAltitude (_altitude);
        result.setBearing (_bearing);
        result.setLatitude (_latitude);
        result.setLongitude (_longitude);
        result.setSpeed (_speed);
        result.setTime (_locationTime);

        return result;
    }

    @Override
    public String toString ()
    {
        return String.format(" %.6f,%.6f", getLatitude (), getLongitude ());
    }
}
