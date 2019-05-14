package com.cachirulop.logmytrip.dialog;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

/**
 * Created by david on 17/10/15.
 */
public class ListDialog
        extends DialogFragment
{
    private int       _titleId;
    private int       _arrayId;
    private boolean   _isSingleChoice;
    private boolean[] _selectedItems;
    private boolean[] _defaultItems;
    private OnListDialogListener _listener;

    private int _defaultItem;
    private int _selectedItem;


    public interface OnListDialogListener {
        void onNegativeButtonClick ();

        public void onSingleItemSelected (int selectedItem);
        public void onMultipleItemSelected (boolean[] selectedItems);
    }

    public int getTitleId ()
    {
        return _titleId;
    }

    public void setTitleId (int titleId)
    {
        _titleId = titleId;
    }

    public boolean[] getSelectedItems ()
    {
        return _selectedItems;
    }

    public void setSelectedItems (boolean[] selectedItems)
    {
        _selectedItems = selectedItems;
    }

    public boolean[] getDefaultItems ()
    {
        return _defaultItems;
    }

    public void setDefaultItems (boolean[] defaultItems)
    {
        _defaultItems = defaultItems;
    }

    public int getDefaultItem ()
    {
        return _defaultItem;
    }

    public void setDefaultItem (int defaultItem)
    {
        _defaultItem = defaultItem;
    }

    public int getSelectedItem ()
    {
        return _selectedItem;
    }

    public void setSelectedItem (int selectedItem)
    {
        _selectedItem = selectedItem;
    }

    public OnListDialogListener getListener ()
    {
        return _listener;
    }

    public void setListener (OnListDialogListener listener)
    {
        _listener = listener;
    }

    public int getArrayId ()
    {
        return _arrayId;
    }

    public void setArrayId (int arrayId)
    {
        _arrayId = arrayId;
    }

    public boolean isSingleChoice ()
    {
        return _isSingleChoice;
    }

    public void setSingleChoice (boolean singleChoice)
    {
        _isSingleChoice = singleChoice;
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
                                              public void onClick (DialogInterface dialog,
                                                                   int which)
                                              {
                                                  _selectedItem = which;

                                                  if (_listener != null) {
                                                      _listener.onSingleItemSelected (_selectedItem);
                                                  }

                                                  getDialog ().dismiss ();
                                              }
                                          });
        }
        else {
            builder.setMultiChoiceItems (_arrayId, null,
                                         new DialogInterface.OnMultiChoiceClickListener ()
                                         {
                                             @Override
                                             public void onClick (DialogInterface dialog,
                                                                  int which,
                                                                  boolean isChecked)
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
                    if (_listener != null) {
                        _listener.onMultipleItemSelected (_selectedItems);
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
        }

        return builder.create ();
    }
}
