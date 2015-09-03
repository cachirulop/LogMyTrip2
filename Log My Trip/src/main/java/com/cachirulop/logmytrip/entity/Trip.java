package com.cachirulop.logmytrip.entity;

import java.util.Date;

/**
 * @author david
 *
 */
public class Trip
{
    private long _id;
    private Date _tripDate;
    private String _description;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trip)) return false;

        Trip trip = (Trip) o;

        return this.hashCode() == trip.hashCode();
    }

    @Override
    public int hashCode() {
        int result = (int) (_id ^ (_id >>> 32));
        result = 31 * result + _tripDate.hashCode();
        result = 31 * result + _description.hashCode();
        return result;
    }
}
