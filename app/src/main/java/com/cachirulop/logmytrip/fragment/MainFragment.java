
package com.cachirulop.logmytrip.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;

public class MainFragment
        extends Fragment
        implements OnClickListener
{

    public MainFragment ()
    {}

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        View rootView = inflater.inflate (R.layout.fragment_main,
                                          container,
                                          false);
        
        ToggleButton saveTrip;
        
        saveTrip = (ToggleButton) rootView.findViewById (R.id.tbSaveTrip);
        saveTrip.setOnClickListener (this);
        saveTrip.setChecked (SettingsManager.getLogTrip (this.getActivity ()));
        
        return rootView;
    }

    @Override
    public void onClick (View v)
    {
        switch (v.getId ()) {
            case R.id.tbSaveTrip:
                onSaveTripClick (v);
        }
    }

    private void onSaveTripClick (View v) 
    {
      boolean on;
      
      on = ((ToggleButton) v).isChecked ();
      if (on) {
          ServiceManager.startSaveTrip (this.getActivity ());
      }
      else {
          ServiceManager.stopSaveTrip (this.getActivity ());
      }
    }

}
