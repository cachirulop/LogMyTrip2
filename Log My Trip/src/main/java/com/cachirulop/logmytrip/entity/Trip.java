package com.cachirulop.logmytrip.entity;

import android.location.Location;

import com.cachirulop.logmytrip.manager.TripManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author david
 */
public class Trip
        implements Serializable
{
    private long _id;
    private Date _tripDate;
    private String _title;
    private String _description;
    private double _totalDistance = -1;
    private long   _totalTime     = -1;
    private float  _maxSpeed      = -1;
    private float  _mediumSpeed   = -1;

    private transient List<TripSegment> _segments = null;

    public Trip ()
    {
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
     * @return the tripDate
     */
    public Date getTripDate ()
    {
        return _tripDate;
    }

    /**
     * @param tripDate the tripDate to set
     */
    public void setTripDate (Date tripDate)
    {
        this._tripDate = tripDate;
    }

    public String getTitle ()
    {
        return _title;
    }

    public void setTitle (String title)
    {
        _title = title;
    }
    /**
     * @return the _description
     */
    public String getDescription ()
    {
        return _description;
    }

    /**
     * @param description the _description to set
     */
    public void setDescription (String description)
    {
        this._description = description;
    }

    public double computeTotalDistance ()
    {
        if (_totalDistance == -1) {
            _totalDistance = 0;

            for (TripSegment s : getSegments ()) {
                _totalDistance += s.computeTotalDistance ();
            }
        }

        return _totalDistance;
    }

    public List<TripSegment> getSegments ()
    {
        if (_segments == null) {
            loadSegments ();
        }

        return _segments;
    }

    private void loadSegments ()
    {
        List<TripLocation> all;
        TripLocation       last;
        Calendar           cal;
        TripSegment        current = null;

        cal = Calendar.getInstance ();

        _segments = new ArrayList<> ();

        all = TripManager.getTripLocationList (this);
        last = null;
        for (TripLocation l : all) {
            if (last == null) {
                current = new TripSegment ();

                current.getLocations ()
                       .add (l);
                _segments.add (current);
            }
            else {
                boolean newSegment;

                cal.setTime (last.getLocationTimeAsDate ());
                cal.add (Calendar.HOUR, 2);

                newSegment = (l.getLocationTimeAsDate ()
                               .after (cal.getTime ()));
                if (!newSegment) {
                    Location lastLocation;
                    Location currentLocation;

                    lastLocation = last.toLocation ();
                    currentLocation = l.toLocation ();

                    newSegment = (lastLocation.distanceTo (currentLocation)) > 100;
                }

                if (newSegment) {
                    current = new TripSegment ();
                    current.getLocations ()
                           .add (l);

                    _segments.add (current);
                }
                else {
                    current.getLocations ()
                           .add (l);
                }
            }

            last = l;
        }
    }

    public List<TripSegment> getSegments (boolean refresh)
    {
        if (refresh) {
            _segments.clear ();
            _segments = null;
        }

        return getSegments ();
    }

    /**
     * Total time of the trip in milliseconds
     *
     * @return All segments duration time in milliseconds
     */
    public long computeTotalTime ()
    {
        if (_totalTime == -1) {
            _totalTime = 0;

            for (TripSegment s : getSegments ()) {
                _totalTime += s.computeTotalTime ();
            }
        }

        return _totalTime;
    }

    public TripLocation getStartLocation ()
    {
        if (getSegments ().size () > 0) {
            return getSegments ().get (0)
                                 .getStartLocation ();
        }
        else {
            return null;
        }
    }

    public TripLocation getEndLocation ()
    {
        if (getSegments ().size () > 0) {
            return getSegments ().get (getSegments ().size () - 1)
                                 .getEndLocation ();
        }
        else {
            return null;
        }
    }



    @Override
    public boolean equals (Object o)
    {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }
        if (!(o instanceof Trip)) {
            return false;
        }

        Trip trip = (Trip) o;

        return this.hashCode () == trip.hashCode ();
    }

    @Override
    public int hashCode ()
    {
        int result = (int) (_id ^ (_id >>> 32));
        result = 31 * result + _tripDate.hashCode ();
        result = 31 * result + _title.hashCode ();

        return result;
    }

    public float computeMaxSpeed ()
    {
        if (_maxSpeed == -1) {
            _maxSpeed = 0;

            for (TripSegment s : getSegments ()) {
                float current;

                current = s.computeMaxSpeed ();
                if (current > _maxSpeed) {
                    _maxSpeed = current;
                }
            }
        }

        return _maxSpeed;
    }

    public float computeMediumSpeed ()
    {
        if (_mediumSpeed == -1) {
            _mediumSpeed = 0;

            for (TripSegment s : getSegments ()) {
                float current;

                current = s.computeMediumSpeed ();
                if (current > _mediumSpeed) {
                    _mediumSpeed = current;
                }
            }
        }

        return _mediumSpeed;
    }
}
