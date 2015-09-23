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

    public List<TripSegment> getSegments ()
    {
        if (_segments == null) {
            List<TripLocation> all;
            TripLocation last;
            Calendar cal;
            TripSegment current = null;

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

        return _segments;
    }

    @Override
    public boolean equals (Object o)
    {
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
}
