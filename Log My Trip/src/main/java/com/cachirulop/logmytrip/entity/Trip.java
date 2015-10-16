package com.cachirulop.logmytrip.entity;

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
    private String _description;

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
        double result;

        result = 0;
        for (TripSegment s : getSegments ()) {
            result += s.computeTotalDistance ();
        }

        return result;
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
                cal.setTime (last.getLocationTimeAsDate ());
                cal.add (Calendar.HOUR, 2);

                if (l.getLocationTimeAsDate ()
                     .after (cal.getTime ())) {
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
        long result;

        result = 0;
        for (TripSegment s : getSegments ()) {
            result += s.computeTotalTime ();
        }

        return result;
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
        result = 31 * result + _description.hashCode ();
        return result;
    }

    public float computeMaxSpeed ()
    {
        float max;

        max = 0;

        for (TripSegment s : getSegments ()) {
            float current;

            current = s.computeMaxSpeed ();
            if (current > max) {
                max = current;
            }
        }

        return max;
    }

    public float computeMediumSpeed ()
    {
        float max;

        max = 0;

        for (TripSegment s : getSegments ()) {
            float current;

            current = s.computeMediumSpeed ();
            if (current > max) {
                max = current;
            }
        }

        return max;
    }
}
