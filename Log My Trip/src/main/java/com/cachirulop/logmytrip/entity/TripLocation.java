package com.cachirulop.logmytrip.entity;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
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
    private float _accuracy;
    private float _bearing;

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
}
