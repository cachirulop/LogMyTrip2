package com.cachirulop.logmytrip.fragment;

import android.content.Intent;

/**
 * Created by david on 31/01/16.
 */
public interface IMainFragment
{
    void onMainActivityResult (int requestCode, int resultCode, Intent data);

    void reloadData ();
}
