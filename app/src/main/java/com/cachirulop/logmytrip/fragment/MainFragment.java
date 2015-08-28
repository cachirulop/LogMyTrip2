
package com.cachirulop.logmytrip.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;

public class MainFragment
        extends Fragment
{

    public MainFragment ()
    {}

    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main,
                                          container,
                                          false);
    }
}
