package com.cachirulop.logmytrip.entity;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by david on 23/09/15.
 */
public class TripSegment
{
    private List<TripLocation> _locations = new ArrayList<> ();

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
        double   result;
        Location previous;

        previous = null;
        result = 0;
        for (TripLocation l : _locations) {
            Location current;

            current = l.toLocation ();
            if (previous != null) {
                result += previous.distanceTo (current);
            }

            previous = current;
        }

        return result;
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
}
