package com.cachirulop.logmytrip.fragment;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.manager.TripManager;

import java.util.List;

public class MainFragment
        extends Fragment
        implements TripItemAdapter.OnTripItemClickListener, ActionMode.Callback
{
    private RecyclerView _recyclerView;
    private TripItemAdapter _adapter;
    private ActionMode _actionMode;
    private FloatingActionButton _fabSaveTrip;
    private Context _ctx;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updateSavingStatus((Trip) msg.obj);
        }
    };

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main,
                container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getView() != null) {
            // Init context
            _ctx = getActivity();

            // Recyclerview
            _recyclerView = (RecyclerView) getView().findViewById(R.id.rvTrips);
            _recyclerView.setLayoutManager(new LinearLayoutManager(_ctx));
            _recyclerView.setHasFixedSize(true);

            _recyclerView.setItemAnimator(new DefaultItemAnimator());

            loadTrips();

            // Save button
            _fabSaveTrip = (FloatingActionButton) getView().findViewById(R.id.fabSaveTrip);
            _fabSaveTrip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSaveTripClick(v);
                }
            });

        }
    }

    private void onSaveTripClick(View v) {
        if (SettingsManager.isLogTrip(_ctx)) {
            ServiceManager.stopSaveTrip(_ctx, handler);

            _fabSaveTrip.setImageResource(R.mipmap.ic_button_save);
        } else {
            ServiceManager.startSaveTrip(_ctx, handler);

            _fabSaveTrip.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    /**
     * Load existing trips
     */
    private void loadTrips() {
        List<Trip> trips;

        if (getView() != null) {
            trips = TripManager.LoadTrips(_ctx);

            _adapter = new TripItemAdapter(_ctx, trips);
            _adapter.setOnTripItemClickListener(this);
            _recyclerView.setAdapter(_adapter);
        }
    }

    public void updateSavingStatus(Trip currentTrip) {
        int position;

        _recyclerView.smoothScrollToPosition(0);
        _adapter.updateTripStatus();
    }

    public RecyclerView getRecyclerView() {
        return _recyclerView;
    }

    @Override
    public void onLongClick(View v) {
        if (_actionMode != null) {
            return;
        }

        _actionMode = getView().startActionMode(this);

        updateActionModeTitle();
    }

    private void updateActionModeTitle() {
        _actionMode.setTitle(getString(R.string.selected_count, _adapter.getSelectedItemCount()));
    }

    @Override
    public void onClick(View view) {
        updateActionModeTitle();
    }

//    public void onClickOld(View view) {
///*
//        if (view.getId() == R.id.fab_add) {
//            // fab click
//            addItemToList();
//        } else
//*/
////        if (view.getId() == R.id.tripListItemContainer) {   // Item click
////            int selected;
////
////            selected = _recyclerView.getChildAdapterPosition(view);
////            if (_actionMode != null) {
////                myToggleSelection(selected);
////            } else {
///*
//            DemoModel data = adapter.getItem(idx);
//            View innerContainer = view.findViewById(R.id.container_inner_item);
//            innerContainer.setTransitionName(Constants.NAME_INNER_CONTAINER + "_" + data.id);
//            Intent startIntent = new Intent(this, CardViewDemoActivity.class);
//            startIntent.putExtra(Constants.KEY_ID, data.id);
//            ActivityOptions options = ActivityOptions
//                    .makeSceneTransitionAnimation(this, innerContainer, Constants.NAME_INNER_CONTAINER);
//            this.startActivity(startIntent, options.toBundle());
//*/
////            }
////        }
//    }

//    private void myToggleSelection(int idx) {
//        TripItemAdapter adapter;
//
//        _adapter.toggleSelection(idx);
//
//        String title = getString(R.string.selected_count, _adapter.getSelectedItemCount());
//
//        _actionMode.setTitle(title);
//    }

    /* OnItemTouchListener implementation */
/*
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        _detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
*/
    /*  ActionMode.Callback implementation */

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        _adapter.setActionMode(true);

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_trip_actionmode, menu);

        _fabSaveTrip.hide();

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_selected_trip:
                List<Trip> selectedItems = _adapter.getSelectedItems();

                for (Trip t : selectedItems) {
                    TripManager.deleteTrip(_ctx, t);
                    _adapter.removeItem(t);
                }

                _actionMode.finish();
                _adapter.notifyDataSetChanged();

                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        _actionMode = null;
        _adapter.setActionMode(false);
        _adapter.clearSelections();
        _fabSaveTrip.show();
    }



    /* Gesture detection class */
/*
    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view;

            view = _recyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);

            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            int selected;
            View view;

            if (_actionMode != null) {
                return;
            }

            _actionMode = getView().startActionMode(MainFragment.this);
            view = _recyclerView.findChildViewUnder(e.getX(), e.getY());

            selected = _recyclerView.getChildAdapterPosition(view);

            myToggleSelection(selected);

            super.onLongPress(e);
        }
    }
*/
}
