package com.cachirulop.logmytrip.entity;

import android.content.Context;
import android.location.Location;

import com.cachirulop.logmytrip.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by david on 23/09/15.
 */
public class TripSegment
        implements Serializable
{
    private List<TripLocation> _locations = new ArrayList<> ();

    private double _totalDistance = -1;
    private float  _maxSpeed      = -1;
    private float  _mediumSpeed   = -1;
    private Trip _trip;

    public TripSegment (Trip trip)
    {
        _trip = trip;
    }

    public String getTitle (Context ctx)
    {
        return String.format (ctx.getString (R.string.title_segment_num), getIndex () + 1);
    }

    public Trip getTrip ()
    {
        return _trip;
    }

    public void setTrip (Trip trip)
    {
        _trip = trip;
    }

    public int getIndex ()
    {
        return _trip.getSegments ().indexOf (this);
    }

    public List<TripLocation> getLocations ()
    {
        return _locations;
    }

    public void setLocations (List<TripLocation> locations)
    {
        _locations = locations;
    }

    /**
     * Sum of the distance between all the locations in meters
     *
     * @return Distance in meters
     */
    public double computeTotalDistance ()
    {
        if (_totalDistance == -1) {
            Location previous;

            previous = null;
            _totalDistance = 0;
            for (TripLocation l : _locations) {
                Location current;

                current = l.toLocation ();
                if (previous != null) {
                    _totalDistance += previous.distanceTo (current);
                }

                previous = current;
            }
        }

        return _totalDistance;
    }

    /**
     * Milliseconds between the first and last location
     *
     * @return Value in milliseconds
     */
    public long computeTotalTime ()
    {
        return (getEndTime () - getSTartTime ());
    }

    public long getEndTime ()
    {
        TripLocation result;

        result = getEndLocation ();
        if (result == null) {
            return 0;
        }
        else {
            return result.getLocationTime ();
        }
    }

    public long getSTartTime ()
    {
        TripLocation result;

        result = getStartLocation ();
        if (result == null) {
            return 0;
        }
        else {
            return result.getLocationTime ();
        }
    }

    public TripLocation getEndLocation ()
    {
        if (_locations == null || _locations.size () == 0) {
            return null;
        }
        else if (_locations.size () == 1) {
            return getStartLocation ();
        }
        else {
            return _locations.get (_locations.size () - 1);
        }
    }

    public TripLocation getStartLocation ()
    {
        if (_locations == null || _locations.size () == 0) {
            return null;
        }
        else {
            return _locations.get (0);
        }
    }

    public Date getStartDate ()
    {
        return new Date (getSTartTime ());
    }

    public Date getEndDate ()
    {
        return new Date (getEndTime ());
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TripSegment)) {
            return false;
        }

        TripSegment segment = (TripSegment) o;

        return segment.hashCode () == this.hashCode ();

    }

    @Override
    public int hashCode ()
    {
        return _locations != null
               ? _locations.hashCode ()
               : 0;
    }

    public float computeMaxSpeed ()
    {
        if (_maxSpeed == -1) {
            _maxSpeed = 0;

            for (TripLocation l : _locations) {
                if (l.getSpeed () > _maxSpeed) {
                    _maxSpeed = l.getSpeed ();
                }
            }

            // Convert m/s to km/h
            _maxSpeed *= 3.6f;
        }

        return _maxSpeed;
    }

    public float computeMediumSpeed ()
    {
        if (_mediumSpeed == -1) {
            _mediumSpeed = 0;

            for (TripLocation l : _locations) {
                _mediumSpeed += l.getSpeed ();
            }

            // Convert m/s to km/h
            _mediumSpeed = (_mediumSpeed / _locations.size ()) * 3.6f;
        }

        return _mediumSpeed;
    }

    public void addLocation (TripLocation location)
    {
        _locations.add (location);
    }
}
