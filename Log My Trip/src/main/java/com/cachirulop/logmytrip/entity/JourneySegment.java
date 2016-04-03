package com.cachirulop.logmytrip.entity;

import android.content.Context;

import com.cachirulop.logmytrip.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by david on 23/09/15.
 */
public class JourneySegment
        implements Serializable
{
    private List<Location> _locations = new ArrayList<> ();

    private Journey _journey;

    public JourneySegment (Journey journey)
    {
        _journey = journey;
    }

    public String getTitle (Context ctx)
    {
        return String.format (ctx.getString (R.string.title_segment_num), getIndex () + 1);
    }

    public Journey getJourney ()
    {
        return _journey;
    }

    public void setJourney (Journey journey)
    {
        _journey = journey;
    }

    public int getIndex ()
    {
        return _journey.getSegments ().indexOf (this);
    }

    public List<Location> getLocations ()
    {
        return _locations;
    }

    public void setLocations (List<Location> locations)
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
        android.location.Location previous;
        double   result;

        previous = null;
        result = 0;
        for (Location l : _locations) {
            android.location.Location current;

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
        Location result;

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
        Location result;

        result = getStartLocation ();
        if (result == null) {
            return 0;
        }
        else {
            return result.getLocationTime ();
        }
    }

    public Location getEndLocation ()
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

    public Location getStartLocation ()
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

    public void addLocation (Location location)
    {
        _locations.add (location);
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JourneySegment)) {
            return false;
        }

        JourneySegment segment = (JourneySegment) o;

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
        float result;

        result = 0;

        for (Location l : _locations) {
            if (l.getSpeed () > result) {
                result = l.getSpeed ();
            }
        }

        // Convert m/s to km/h
        return result *= 3.6f;
    }

    public float computeMediumSpeed ()
    {
        float result;

        result = 0;

        for (Location l : _locations) {
            result += l.getSpeed ();
        }

        // Convert m/s to km/h
        return (result / _locations.size ()) * 3.6f;
    }
}
