package com.cachirulop.logmytrip.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.cachirulop.logmytrip.helper.LogHelper;

/**
 * helper for Confirm-Dialog creation
 */
public class ConfirmDialog
        extends DialogFragment
{
    private int _titleId;
    private int _messageId;

    public interface OnConfirmDialogListener {
        void onPositiveButtonClick ();
        void onNegativeButtonClick ();
    }

    private OnConfirmDialogListener _listener;

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder (getActivity ());
        builder.setTitle (getTitleId ());
        builder.setMessage (getMessageId ());

        builder.setPositiveButton (android.R.string.ok, new DialogInterface.OnClickListener ()
        {
            public void onClick (DialogInterface dialog, int which)
            {
                if (_listener != null) {
                    _listener.onPositiveButtonClick ();
                }

                getDialog ().dismiss ();
            }
        });

        builder.setNegativeButton (android.R.string.cancel, new DialogInterface.OnClickListener ()
        {
            @Override
            public void onClick (DialogInterface dialog, int which)
            {
                if (_listener != null) {
                    _listener.onNegativeButtonClick ();
                }

                getDialog ().dismiss ();
            }
        });

        return builder.create ();
    }

    public void setListener (OnConfirmDialogListener listener)
    {
        _listener = listener;
    }

    public OnConfirmDialogListener getListener ()
    {
        return _listener;
    }

    @Override
    public void onStart ()
    {
        super.onStart ();
/*
        AlertDialog dialog;

        dialog = (AlertDialog) getDialog ();
        if (dialog != null) {
            Button positive;

            positive = dialog.getButton (Dialog.BUTTON_POSITIVE);

            positive.setOnClickListener (this);
        }
*/
    }

    public void onClick (View v)
    {
        getDialog ().dismiss ();
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
}