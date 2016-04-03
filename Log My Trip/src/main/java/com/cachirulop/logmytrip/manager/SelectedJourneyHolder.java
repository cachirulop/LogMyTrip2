package com.cachirulop.logmytrip.manager;

import com.cachirulop.logmytrip.entity.Journey;

/**
 * Created by dmagro on 10/11/2015.
 */
public class SelectedJourneyHolder
{
    private static SelectedJourneyHolder ourInstance = new SelectedJourneyHolder ();

    private Journey _selectedJourney = null;

    private SelectedJourneyHolder ()
    {
    }

    public static SelectedJourneyHolder getInstance ()
    {
        return ourInstance;
    }

    public Journey getSelectedJourney ()
    {
        return _selectedJourney;
    }

    public void setSelectedJourney (Journey selectedJourney)
    {
        _selectedJourney = selectedJourney;
    }

}
