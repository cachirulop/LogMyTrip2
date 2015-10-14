package com.cachirulop.logmytrip.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripStatisticsAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.receiver.AddressResultReceiver;
import com.cachirulop.logmytrip.service.FetchAddressService;
import com.cachirulop.logmytrip.util.FormatHelper;

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
    private RecyclerView          _recyclerView;
    private TripStatisticsAdapter _adapter;
    private Trip                  _trip;
    private Context               _ctx;

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
        // Inflate the layout for this fragment
        // return inflater.inflate (R.layout.fragment_tab_statistics, container, false);
        return inflater.inflate (R.layout.fragment_tab_statistics2, container, false);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        LinearLayout            detail;
        CardView                card;
        CollapsingToolbarLayout toolbar;

        super.onViewCreated (view, savedInstanceState);

        _ctx = getActivity ();

        toolbar = (CollapsingToolbarLayout) view.findViewById (
                R.id.statistics_summary_collapsingtoolbar);
        toolbar.setTitle (_trip.getDescription ());

        // LinearLayout to add the cards
        detail = (LinearLayout) view.findViewById (R.id.llTripStatistics);

        // Trip summary card
        card = (CardView) getLayoutInflater (savedInstanceState).inflate (R.layout.trip_summary,
                                                                          null);


        fillSummaryCard (card);
        detail.addView (card);


        //        _recyclerView = (RecyclerView) getView ().findViewById (R.id.rvSegments);
        //        _recyclerView.setLayoutManager (new LinearLayoutManager (_ctx));
        //        _recyclerView.setHasFixedSize (true);
        //
        //        _recyclerView.setItemAnimator (new DefaultItemAnimator ());
        //
        //        _adapter = new TripStatisticsAdapter (_ctx, this, _trip);
        //        _recyclerView.setAdapter (_adapter);
    }

    private void fillSummaryCard (CardView c)
    {
        TextView     tv;
        TripLocation l;

        tv = (TextView) c.findViewById (R.id.tvTripSummaryLocationFrom);
        l = _trip.getStartLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
        }

        tv = (TextView) c.findViewById (R.id.tvTripSummaryLocationTo);
        l = _trip.getEndLocation ();
        if (l != null) {
            tv.setText (l.toString ());
            FetchAddressService.startService (_ctx, new AddressResultReceiver (new Handler (), tv),
                                              l.toLocation ());
        }
        else {
            tv.setText ("");
        }

        tv = (TextView) c.findViewById (R.id.tvTripSummaryStartDate);
        tv.setText (FormatHelper.formatDate (_ctx, _trip.getStartLocation ()
                                                        .getLocationTimeAsDate ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryEndDate);
        tv.setText (FormatHelper.formatDate (_ctx, _trip.getEndLocation ()
                                                        .getLocationTimeAsDate ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryStartTime);
        tv.setText (FormatHelper.formatTime (_ctx, _trip.getStartLocation ()
                                                        .getLocationTimeAsDate ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryEndTime);
        tv.setText (FormatHelper.formatTime (_ctx, _trip.getEndLocation ()
                                                        .getLocationTimeAsDate ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryTotalDistance);
        tv.setText (FormatHelper.formatDistance (_trip.computeTotalDistance ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryTotalTime);
        tv.setText (FormatHelper.formatDuration (_trip.computeTotalTime ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryMaxSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMaxSpeed ()));

        tv = (TextView) c.findViewById (R.id.tvTripSummaryMediumSpeed);
        tv.setText (FormatHelper.formatSpeed (_trip.computeMediumSpeed ()));
    }
}
