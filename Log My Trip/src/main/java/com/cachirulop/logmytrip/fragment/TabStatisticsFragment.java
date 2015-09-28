package com.cachirulop.logmytrip.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TabStatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TabStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabStatisticsFragment
        extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RecyclerView    _recyclerView;
    private TripItemAdapter _adapter;
    private Context         _ctx;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TabStatisticsFragment ()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabStatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TabStatisticsFragment newInstance (String param1, String param2)
    {
        TabStatisticsFragment fragment = new TabStatisticsFragment ();
        Bundle                args     = new Bundle ();
        args.putString (ARG_PARAM1, param1);
        args.putString (ARG_PARAM2, param2);
        fragment.setArguments (args);
        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate (savedInstanceState);
        if (getArguments () != null) {
            mParam1 = getArguments ().getString (ARG_PARAM1);
            mParam2 = getArguments ().getString (ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
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

        loadTrips ();
    }

    private void loadTrips ()
    {
        List<Trip> trips;

        if (getView () != null) {
            trips = TripManager.LoadTrips (_ctx);

            _adapter = new TripItemAdapter (_ctx, trips);
            // _adapter.setOnTripItemClickListener (this);
            _recyclerView.setAdapter (_adapter);
        }
    }
}
