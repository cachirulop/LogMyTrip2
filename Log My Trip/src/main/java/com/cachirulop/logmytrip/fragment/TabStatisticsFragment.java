package com.cachirulop.logmytrip.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.dialog.ConfirmDialog;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.cachirulop.logmytrip.manager.TripManager;

import java.util.List;


public class TabStatisticsFragment
        extends Fragment
        implements RecyclerViewItemClickListener,
                   ActionMode.Callback
{
    private RecyclerView          _recyclerView;
    private TripStatisticsAdapter _adapter;
    private Trip                  _trip;
    private Context               _ctx;
    private ActionMode _actionMode;
    private int        _map;

    public TabStatisticsFragment ()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TabStatisticsFragment.
     */
    public static TabStatisticsFragment newInstance (Trip trip)
    {
        TabStatisticsFragment fragment;
        Bundle                args;

        args = new Bundle ();
        args.putSerializable (MainFragment.ARG_PARAM_TRIP, trip);

        fragment = new TabStatisticsFragment ();
        fragment.setArguments (args);

        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        if (getArguments () != null) {
            _trip = (Trip) getArguments ().getSerializable (MainFragment.ARG_PARAM_TRIP);
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate (R.layout.fragment_tab_statistics, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        super.onViewCreated (view, savedInstanceState);

        _ctx = getActivity ();

        _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvSegments);
        _recyclerView.setLayoutManager (new LinearLayoutManager (_ctx));
        _recyclerView.setHasFixedSize (true);

        _recyclerView.setItemAnimator (new DefaultItemAnimator ());

        _adapter = new TripStatisticsAdapter (_ctx, this, _trip);
        _adapter.setOnTripSegmentItemClickListener (this);
        _recyclerView.setAdapter (_adapter);
    }

    public void setMapType (int mapType)
    {
        _adapter.setMapType (mapType);
    }

    @Override
    public void onRecyclerViewItemLongClick (View v, int position)
    {
        if (_actionMode != null) {
            return;
        }

        _actionMode = getView ().startActionMode (this);

        updateActionModeTitle ();
    }

    private void updateActionModeTitle ()
    {
        _actionMode.setTitle (
                getString (R.string.title_selected_count, _adapter.getSelectedItemCount ()));
    }

    @Override
    public void onRecyclerViewItemClick (View v, int position)
    {
        if (_actionMode != null) {
            updateActionModeTitle ();
        }
    }

    @Override
    public boolean onCreateActionMode (ActionMode mode, Menu menu)
    {
        _adapter.setActionMode (true);

        MenuInflater inflater = mode.getMenuInflater ();
        inflater.inflate (R.menu.menu_segment_actionmode, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode (ActionMode mode, Menu menu)
    {
        return false;
    }

    @Override
    public boolean onActionItemClicked (ActionMode mode, MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_delete_selected_trip:
                deleteSelectedSegments ();

                return true;

            default:
                return false;
        }
    }

    private void deleteSelectedSegments ()
    {
        ConfirmDialog dlg;

        dlg = new ConfirmDialog (R.string.title_delete, R.string.msg_delete_confirm)
        {
            @Override
            public void onOkClicked ()
            {
                List<TripSegment> selectedItems = _adapter.getSelectedItems ();

                for (TripSegment s : selectedItems) {
                    TripManager.deleteSegment (_ctx, s);
                    _adapter.removeItem (s);
                }

                _actionMode.finish ();
            }
        };

        dlg.show (getActivity ().getSupportFragmentManager (), "deleteTrips");
    }

    @Override
    public void onDestroyActionMode (ActionMode mode)
    {
        _actionMode = null;
        _adapter.setActionMode (false);
        _adapter.clearSelections ();

        updateActionBarSubtitle ();
    }

    private void updateActionBarSubtitle ()
    {
        ActionBar bar;

        bar = ((AppCompatActivity) getActivity ()).getSupportActionBar ();
        bar.setSubtitle (
                _ctx.getString (R.string.main_activity_subtitle, _adapter.getItemCount ()));
    }
}
