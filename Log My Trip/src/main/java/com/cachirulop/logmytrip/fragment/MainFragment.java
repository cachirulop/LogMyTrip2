package com.cachirulop.logmytrip.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.manager.TripManager;

import java.util.List;

public class MainFragment
        extends Fragment {
    private RecyclerView recyclerView;

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

        RecyclerView rvTrips;

        if (getView() != null) {
            rvTrips = (RecyclerView) getView().findViewById(R.id.rvTrips);
            rvTrips.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvTrips.setHasFixedSize(true);

            // this is the default; this call is actually only necessary with custom ItemAnimators
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            loadTrips();
        }
    }

    private void loadTrips() {
        // Load existing trips
        List<Trip> trips;
        RecyclerView rvTrips;

        if (getView() != null) {
            trips = TripManager.LoadTrips(getActivity());
            rvTrips = (RecyclerView) getView().findViewById(R.id.rvTrips);

            rvTrips.setAdapter(new TripItemAdapter(getActivity(), trips));
        }
    }

    public void updateSavingStatus(Trip currentTrip) {
        RecyclerView rvTrips;
        TripItemAdapter adapter;
        int position;

        rvTrips = (RecyclerView) getView().findViewById(R.id.rvTrips);
        adapter = (TripItemAdapter) rvTrips.getAdapter();

        rvTrips.smoothScrollToPosition(0);
        adapter.updateTripStatus();
    }

    public RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(R.id.rvTrips);
    }
}
