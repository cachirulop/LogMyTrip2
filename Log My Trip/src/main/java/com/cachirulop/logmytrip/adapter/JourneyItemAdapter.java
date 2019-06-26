package com.cachirulop.logmytrip.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Journey;
import com.cachirulop.logmytrip.fragment.RecyclerViewItemClickListener;
import com.cachirulop.logmytrip.helper.LogHelper;
import com.cachirulop.logmytrip.manager.JourneyManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.viewholder.JourneyItemViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmagro on 01/09/2015.
 */
public class JourneyItemAdapter
        extends RecyclerView.Adapter
{

    private Context       _ctx;
    private List<Journey> _items;

    private SparseBooleanArray            _selectedItems;
    private boolean                       _actionMode;
    private RecyclerViewItemClickListener _onJourneyItemClickListener;
    private JourneyItemAdapterListener _listener = null;

    public JourneyItemAdapter (Context ctx, JourneyItemAdapterListener listener)
    {
        _ctx = ctx;
        _listener = listener;

        loadItems ();

        _onJourneyItemClickListener = null;

        _selectedItems = new SparseBooleanArray ();
    }

    public void loadItems ()
    {
        final ProgressDialog progDialog;

        progDialog = ProgressDialog.show (_ctx, _ctx.getString (R.string.msg_loading_journeys),
                                          null,
                                          true);

        new Thread ()
        {
            public void run ()
            {
                _items = JourneyManager.loadJourneys (_ctx);

                onJourneyLoadedMainThread ();

                progDialog.dismiss ();
            }
        }.start ();
    }

    private void onJourneyLoadedMainThread ()
    {
        Handler  main;
        Runnable runInMain;

        main = new Handler (_ctx.getMainLooper ());

        runInMain = new Runnable ()
        {
            @Override
            public void run ()
            {
                notifyDataSetChanged ();

                if (_listener != null) {
                    _listener.onJourneyListLoaded ();
                }
            }
        };

        main.post (runInMain);
    }

    public RecyclerViewItemClickListener getOnJourneyItemClickListener ()
    {
        return _onJourneyItemClickListener;
    }

    public void setOnJourneyItemClickListener (RecyclerViewItemClickListener listener)
    {
        _onJourneyItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType)
    {
        View rowView;

        LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate (R.layout.journeylist_item, parent, false);

        return new JourneyItemViewHolder (_ctx, this, rowView);
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder holder, int position)
    {
        JourneyItemViewHolder vh;

        vh = (JourneyItemViewHolder) holder;

        // Drawable background;
        int background;

        if (_actionMode && SettingsManager.isLogJourney (_ctx) && position == 0) {
            background = R.color.disabled;
        }
        else if (_actionMode && isSelected (vh.getLayoutPosition ())) {
            background = R.color.default_background;
        }
        else {
            background = R.color.cardview_light_background;
        }

        // Set data into the view.
        vh.bindView (_items.get (position),
                     _selectedItems.get (position, false), background, _onJourneyItemClickListener);
    }

    public boolean isSelected (int pos)
    {
        return _selectedItems.get (pos, false);
    }

    @Override
    public long getItemId (int position)
    {
        return _items.get (position).getId ();
    }

    @Override
    public int getItemCount ()
    {
        if (_items == null) {
            return 0;
        }
        else {
            return _items.size ();
        }
    }

    public void startLog ()
    {
        Journey current;
        int position;

        current = JourneyManager.getActiveJourney (_ctx);
        if (current != null && _items != null) {
            position = _items.indexOf (current);
            if (position == -1) {
                _items.add (0, current);

                notifyItemInserted (0);
            }
            else {
                notifyItemChanged (position);
            }
        }
    }

    public void stopLog (Journey journey)
    {
        int position;

        position = _items.indexOf (journey);
        if (position != -1) {
            notifyItemChanged (position);
        }
    }

    public void toggleSelection (int pos)
    {
        if (!SettingsManager.isLogJourney (_ctx) || pos != 0) {
            if (_selectedItems.get (pos, false)) {
                _selectedItems.delete (pos);
            }
            else {
                _selectedItems.put (pos, true);
            }

            notifyItemChanged (pos);
        }
    }

    public void selectAllItems ()
    {
        _selectedItems.clear ();

        for (int i = 0 ; i < _items.size () ; i++) {
            _selectedItems.put (i, true);

            notifyItemChanged (i);
        }
    }

    public void deselectAllItems ()
    {
        _selectedItems.clear ();

        notifyDataSetChanged ();
    }

    public int getSelectedItemCount ()
    {
        return _selectedItems.size ();
    }

    public List<Journey> getSelectedItems ()
    {
        List<Journey> result;

        result = new ArrayList<Journey> (_selectedItems.size ());

        for (int i = 0 ; i < _selectedItems.size () ; i++) {
            result.add (_items.get (_selectedItems.keyAt (i)));
        }

        return result;
    }

    public boolean isActionMode ()
    {
        return _actionMode;
    }

    public void setActionMode (boolean selectionMode)
    {
        this._actionMode = selectionMode;
        this.notifyDataSetChanged ();
    }

    public void removeItem (Journey t)
    {
        int pos;

        pos = _items.indexOf (t);
        if (pos != -1) {
            _items.remove (t);
            notifyItemChanged (pos);
        }
    }

    public void setItem (int position, Journey value)
    {
        _items.set (position, value);
    }

    public Journey getItem (int position)
    {
        return _items.get (position);
    }

    public void reloadItems ()
    {
        if (_items != null) {
            _items.clear ();
        }

        loadItems ();
    }

    public void clearJourneys ()
    {
        if (_items != null) {
            _items.clear ();
        }
    }

    public interface JourneyItemAdapterListener
    {
        void onJourneyListLoaded ();
    }
}
