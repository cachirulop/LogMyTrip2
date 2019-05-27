package com.cachirulop.logmytrip.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by dmagro on 19/10/2015.
 */
public class CustomViewDialog
        extends DialogFragment
{
    private int  _titleId;
    private int  _messageId = -1;
    private int  _viewId;
    private View                   _customView;
    private OnCustomDialogListener _listener;

    public interface OnCustomDialogListener {
        void onPositiveButtonClick ();
        void onNegativeButtonClick ();

        void bindData (View v);
    }

    public OnCustomDialogListener getListener ()
    {
        return _listener;
    }

    public void setListener (OnCustomDialogListener listener)
    {
        _listener = listener;
    }

    public View getCustomView ()
    {
        return _customView;
    }

    public void setCustomView (View customView)
    {
        _customView = customView;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder;
        LayoutInflater      inflater;

        builder = new AlertDialog.Builder (getActivity ());
        builder.setTitle (getTitleId ());

        if (getMessageId () != -1) {
            builder.setMessage (getMessageId ());
        }

        inflater = getActivity ().getLayoutInflater ();

        _customView = inflater.inflate (getViewId (), null);
        builder.setView (_customView);

        builder.setPositiveButton (android.R.string.ok, new DialogInterface.OnClickListener ()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                if (_listener != null) {
                    _listener.onPositiveButtonClick ();
                }

                dialog.dismiss ();
            }
        });

        builder.setNegativeButton (android.R.string.cancel, new DialogInterface.OnClickListener ()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                if (_listener != null) {
                    _listener.onNegativeButtonClick ();
                }

                dialog.dismiss ();
            }
        });

        if (_listener != null) {
            _listener.bindData (_customView);
        }

        return builder.create ();
    }

    public int getTitleId ()
    {
        return _titleId;
    }

    public void setTitleId (int titleId)
    {
        _titleId = titleId;
    }

    public int getMessageId ()
    {
        return _messageId;
    }

    public void setMessageId (int messageId)
    {
        _messageId = messageId;
    }

    public int getViewId ()
    {
        return _viewId;
    }

    public void setViewId (int viewId)
    {
        _viewId = viewId;
    }
}
