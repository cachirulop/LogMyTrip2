package com.cachirulop.logmytrip.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by dmagro on 19/10/2015.
 */
public abstract class CustomViewDialog
        extends DialogFragment
{
    private int  _titleId;
    private int  _messageId;
    private int  _viewId;
    private View _customView;

    public CustomViewDialog (int titleId, int viewId)
    {
        super ();

        _titleId = titleId;
        _messageId = -1;
        _viewId = viewId;
    }

    public CustomViewDialog (int titleId, int messageId, int viewId)
    {
        super ();

        _titleId = titleId;
        _messageId = messageId;
        _viewId = viewId;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder;
        LayoutInflater      inflater;

        builder = new AlertDialog.Builder (getActivity ());
        builder.setTitle (_titleId);

        if (_messageId != -1) {
            builder.setMessage (_messageId);
        }

        inflater = getActivity ().getLayoutInflater ();

        _customView = inflater.inflate (_viewId, null);
        builder.setView (_customView);

        builder.setPositiveButton (android.R.string.ok, new DialogInterface.OnClickListener ()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                onOkClicked (_customView);

                dialog.dismiss ();
            }
        });

        builder.setNegativeButton (android.R.string.cancel, null);

        bindData (_customView);

        return builder.create ();
    }

    abstract public void onOkClicked (View view);

    abstract public void bindData (View view);
}
