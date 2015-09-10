package com.cachirulop.logmytrip.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.TripManager;

import java.util.List;

public class MainFragment
        extends Fragment
        implements RecyclerView.OnItemTouchListener, ActionMode.Callback {
    private RecyclerView _recyclerView;
    private TripItemAdapter _adapter;
    private GestureDetectorCompat _detector;
    private ActionMode _actionMode;

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
            // Detect gestures
            _detector = new GestureDetectorCompat(getActivity(), new RecyclerViewDemoOnGestureListener());
            _detector.setIsLongpressEnabled(true);

            _recyclerView = (RecyclerView) getView().findViewById(R.id.rvTrips);
            _recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            _recyclerView.setHasFixedSize(true);

            _recyclerView.setItemAnimator(new DefaultItemAnimator());
            _recyclerView.addOnItemTouchListener(this);

            loadTrips();
        }
    }

    private void loadTrips() {
        // Load existing trips
        List<Trip> trips;

        if (getView() != null) {
            trips = TripManager.LoadTrips(getActivity());

            _adapter = new TripItemAdapter(getActivity(), trips);
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

    public void onClick(View view) {
/*
        if (view.getId() == R.id.fab_add) {
            // fab click
            addItemToList();
        } else
*/
        if (view.getId() == R.id.tripListItemContainer) {   // Item click
            int selected;

            selected = _recyclerView.getChildAdapterPosition(view);
            if (_actionMode != null) {
                myToggleSelection(selected);
            } else {
/*
            DemoModel data = adapter.getItem(idx);
            View innerContainer = view.findViewById(R.id.container_inner_item);
            innerContainer.setTransitionName(Constants.NAME_INNER_CONTAINER + "_" + data.id);
            Intent startIntent = new Intent(this, CardViewDemoActivity.class);
            startIntent.putExtra(Constants.KEY_ID, data.id);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, innerContainer, Constants.NAME_INNER_CONTAINER);
            this.startActivity(startIntent, options.toBundle());
*/
            }
        }
    }

    private void myToggleSelection(int idx) {
        TripItemAdapter adapter;

        _adapter.toggleSelection(idx);

        String title = getString(R.string.selected_count, _adapter.getSelectedItemCount());

        _actionMode.setTitle(title);
    }

    /* OnItemTouchListener implementation */

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

    /*  ActionMode.Callback implementation */

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        _adapter.setActionMode(true);
/*
        MenuInflater inflater = _actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_recyclerviewdemoactivity, menu);
        fab.setVisibility(View.GONE);
*/

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
/*
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                List<Integer> selectedItemPositions = adapter.getSelectedItems();
                int currPos;
                for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                    currPos = selectedItemPositions.get(i);
                    RecyclerViewDemoApp.removeItemFromList(currPos);
                    adapter.removeData(currPos);
                }
                actionMode.finish();
                return true;
            default:
                return false;
        }
*/
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        _actionMode = null;
        _adapter.setActionMode(false);
        _adapter.clearSelections();
        // fab.setVisibility(View.VISIBLE);
    }

    /* Gesture detection class */

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
}
