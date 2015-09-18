package com.cachirulop.logmytrip.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.fragment.TripDetailFragment;

public class TripDetailActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Inflate the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        // Set the fragment content
        if (findViewById(R.id.tripDetailActivityContainer) != null) {
            if (savedInstanceState == null) {
                TripDetailFragment fragment;

                fragment = new TripDetailFragment();

                fragment.setArguments(getIntent().getExtras());

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.tripDetailActivityContainer, fragment).commit();

            }
        }
    }
}
