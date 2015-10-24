package com.cachirulop.logmytrip.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by david on 17/10/15.
 */
public abstract class ListDialog
        extends DialogFragment
{
    private int       _titleId;
    private int       _arrayId;
    private boolean   _isSingleChoice;
    private boolean[] _selectedItems;
    private boolean[] _defaultItems;

    private int _defaultItem;
    private int _selectedItem;

    public ListDialog (int titleId, int arrayId, int defaultItem)
    {
        this (titleId, arrayId);

        _defaultItem = defaultItem;
        _isSingleChoice = true;
    }

    private ListDialog (int titleId, int arrayId)
    {
        _titleId = titleId;
        _arrayId = arrayId;
    }

    public ListDialog (int titleId, int arrayId, boolean[] defaultItems)
    {
        this (titleId, arrayId);

        _defaultItems = defaultItems;
        _isSingleChoice = false;

        String[] items;

        items = getResources ().getStringArray (_arrayId);
        _selectedItems = new boolean[items.length];
    }


    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState)
    {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder (getActivity ());

        // Set the dialog title
        builder.setTitle (_titleId);

        if (_isSingleChoice) {
            builder.setSingleChoiceItems (_arrayId, _defaultItem,
                                          new DialogInterface.OnClickListener ()
                                          {
                                              @Override
                                              public void onClick (DialogInterface dialog, int which)
                                              {
                                                  _selectedItem = which;

                                                  onSingleItemSelected (_selectedItem);
                                                  getDialog ().dismiss ();
                                              }
                                          });
        }
        else {
            builder.setMultiChoiceItems (_arrayId, null,
                                         new DialogInterface.OnMultiChoiceClickListener ()
                                         {
                                             @Override
                                             public void onClick (DialogInterface dialog, int which, boolean isChecked)
                                             {
                                                 _selectedItems[which] = isChecked;
                                             }
                                         });
        }

        if (!_isSingleChoice) {
            builder.setPositiveButton (android.R.string.ok, new DialogInterface.OnClickListener ()
            {
                @Override
                public void onClick (DialogInterface dialog, int id)
                {
                    onMultipleItemSelected (_selectedItems);
                    getDialog ().dismiss ();
                }
            });

            builder.setNegativeButton (android.R.string.cancel, null);
        }

        return builder.create ();
    }

    public void onSingleItemSelected (int selectedItem)
    {
    }

    public void onMultipleItemSelected (boolean[] selectedItems)
    {
    }
}
