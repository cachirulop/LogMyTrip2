package com.cachirulop.logmytrip.fragment;

import android.view.View;

/**
 * Created by dmagro on 20/10/2015.
 */
public interface RecyclerViewItemClickListener
{
    void onRecyclerViewItemLongClick (View v, int position);

    void onRecyclerViewItemClick (View v, int position);
}
