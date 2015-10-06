package com.cachirulop.logmytrip.receiver;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.TextView;

import com.cachirulop.logmytrip.service.FetchAddressService;

/**
 * Created by dmagro on 06/10/2015.
 */
public class AddressResultReceiver
        extends ResultReceiver
{
    private TextView _view;

    public AddressResultReceiver (Handler handler, TextView view)
    {
        super (handler);

        _view = view;
    }

    /**
     * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
     */
    @Override
    protected void onReceiveResult (int resultCode, Bundle resultData)
    {
        _view.setText (resultData.getString (FetchAddressService.RESULT_DATA_KEY));
    }
}