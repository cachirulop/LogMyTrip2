package com.cachirulop.logmytrip.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.cachirulop.logmytrip.receiver.AddressResultReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by dmagro on 05/10/2015.
 */
public class FetchAddressService
        extends IntentService
{
    public static final String TAG             = FetchAddressService.class.getCanonicalName ();
    public static final String RECEIVER        = TAG + ".RECEIVER";
    public static final String LOCATION        = TAG + ".LOCATION";
    public static final String RESULT_DATA_KEY = TAG + ".RESULT_DATA_KEY";

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver _receiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressService ()
    {
        super (TAG);
    }

    public static void startService (Context ctx, AddressResultReceiver receiver, Location location)
    {
        Intent intent = new Intent (ctx, FetchAddressService.class);

        intent.putExtra (FetchAddressService.RECEIVER, receiver);
        intent.putExtra (FetchAddressService.LOCATION, location);

        ctx.startService (intent);
    }


    /**
     * Tries to get the location address using a Geocoder. If successful, sends an address to a
     * result receiver. If unsuccessful, sends an error message instead.
     * Note: We define a {@link android.os.ResultReceiver} in * MainActivity to process content
     * sent from this service.
     * <p/>
     * This service calls this method from the default worker thread with the intent that started
     * the service. When this method returns, the service automatically stops.
     */
    @Override
    protected void onHandleIntent (Intent intent)
    {
        _receiver = intent.getParcelableExtra (RECEIVER);
        if (_receiver == null) {
            Log.wtf (TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra (LOCATION);
        if (location == null) {
            Log.wtf (TAG, "No location data received.");

            // deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);

            return;
        }

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder (this, Locale.getDefault ());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation (location.getLatitude (), location.getLongitude (),
                                                  1);
        }
        catch (IOException ioException) {
            // Catch network or other I/O problems.
            Log.e (TAG, "Service not available", ioException);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            Log.e (TAG, "Invalid location: " +
                    "Latitude = " + location.getLatitude () +
                    ", Longitude = " + location.getLongitude (), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size () == 0) {
            // TODO: Externalize message
            Log.e (TAG, "No address found");
            deliverResultToReceiver (FAILURE_RESULT, "No address found");
        }
        else {
            Address address = addresses.get (0);
            ArrayList<String> addressFragments = new ArrayList<String> ();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for (int i = 0 ; i < address.getMaxAddressLineIndex () ; i++) {
                addressFragments.add (address.getAddressLine (i));
            }

            Log.i (TAG, "Address found");
            deliverResultToReceiver (SUCCESS_RESULT,
                                     TextUtils.join (System.getProperty ("line.separator"),
                                                     addressFragments));
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver (int resultCode, String message)
    {
        Bundle bundle = new Bundle ();

        bundle.putString (RESULT_DATA_KEY, message);

        _receiver.send (resultCode, bundle);
    }
}
