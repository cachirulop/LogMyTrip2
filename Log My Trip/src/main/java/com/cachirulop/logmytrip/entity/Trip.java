package com.cachirulop.logmytrip.entity;

import android.content.Context;

import com.cachirulop.logmytrip.helper.FormatHelper;
import com.cachirulop.logmytrip.manager.TripManager;

import java.io.Serializable;
import java.util.ArrayList;
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

    private List<TripSegment> _segments = null;

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

    public double getTotalDistance ()
    {
        return _totalDistance;
    }

    public void setTotalDistance (double totalDistance)
    {
        _totalDistance = totalDistance;
    }

    public long getTotalTime ()
    {
        return _totalTime;
    }

    public void setTotalTime (long totalTime)
    {
        _totalTime = totalTime;
    }

    public List<TripSegment> getSegments ()
    {
        return _segments;
    }

    public void setSegments (List<TripSegment> segments)
    {
        _segments = segments;
    }

    public String getSummary (Context ctx)
    {
        if (_totalTime == -1) {
            computeTotalTime (ctx);

            TripManager.updateTrip (ctx, this);
        }

        if (_totalDistance == -1) {
            computeTotalDistance (ctx);

            TripManager.updateTrip (ctx, this);
        }

        return String.format ("%s - %s",
                              FormatHelper.formatDuration (_totalTime),
                              FormatHelper.formatDistance (_totalDistance));
    }

    /**
     * Total time of the trip in milliseconds
     *
     * @return All segments duration time in milliseconds
     */
    public long computeTotalTime (Context ctx)
    {
        if (_segments == null) {
            TripManager.loadTripSegments (ctx, this);
        }

        _totalTime = 0;

        for (TripSegment s : _segments) {
            _totalTime += s.computeTotalTime ();
        }

        return _totalTime;
    }

    public double computeTotalDistance (Context ctx)
    {
        if (_segments == null) {
            TripManager.loadTripSegments (ctx, this);
        }

        _totalDistance = 0;

        for (TripSegment s : _segments) {
            _totalDistance += s.computeTotalDistance ();
        }

        return _totalDistance;
    }

    public float computeMaxSpeed ()
    {
        if (_segments == null) {
            return 0.0f;
        }

        float result;

        result = 0;

        for (TripSegment s : _segments) {
            float current;

            current = s.computeMaxSpeed ();
            if (current > result) {
                result = current;
            }
        }

        return result;
    }

    public float computeMediumSpeed ()
    {
        if (_segments == null) {
            return 0.0f;
        }

        float result;

        result = 0;

        for (TripSegment s : _segments) {
            result += s.computeMediumSpeed ();
        }

        return result / _segments.size ();
    }

    public TripLocation getStartLocation ()
    {
        if (_segments != null && _segments.size () > 0) {
            return _segments.get (0).getStartLocation ();
        }
        else {
            return null;
        }
    }

    public TripLocation getEndLocation ()
    {
        if (_segments != null && _segments.size () > 0) {
            return _segments.get (_segments.size () - 1).getEndLocation ();
        }
        else {
            return null;
        }
    }

    public void addLocation (TripLocation location)
    {
        if (_segments == null || _segments.size () == 0) {
            _segments = new ArrayList<> ();
            _segments.add (new TripSegment (this));
        }

        _segments.get (_segments.size () - 1).addLocation (location);
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

    public void computeLiveStatistics (Context ctx)
    {
        _segments = null;

        TripManager.loadTripSegments (ctx, this);
        TripManager.mergePendingLocations (this);

        this.computeTotalDistance (ctx);
        this.computeTotalTime (ctx);
    }
}
