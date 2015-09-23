package com.cachirulop.logmytrip.entity;

import java.util.ArrayList;
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
}
