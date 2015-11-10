package com.cachirulop.logmytrip.manager;

import com.cachirulop.logmytrip.entity.Trip;

/**
 * Created by dmagro on 10/11/2015.
 */
public class SelectedTripHolder
{
    private static SelectedTripHolder ourInstance = new SelectedTripHolder ();

    private Trip _selectedTrip = null;

    private SelectedTripHolder ()
    {
    }

    public static SelectedTripHolder getInstance ()
    {
        return ourInstance;
    }

    public Trip getSelectedTrip ()
    {
        return _selectedTrip;
    }

    public void setSelectedTrip (Trip selectedTrip)
    {
        _selectedTrip = selectedTrip;
    }

}
