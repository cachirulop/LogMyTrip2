
package com.cachirulop.logmytrip.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.adapter.TripItemAdapter;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.fragment.MainFragment;
import com.cachirulop.logmytrip.manager.ServiceManager;
import com.cachirulop.logmytrip.manager.SettingsManager;
import com.cachirulop.logmytrip.service.LogMyTripService;
import com.cachirulop.logmytrip.util.ToastHelper;

public class MainActivity
        extends Activity
        implements RecyclerView.OnItemTouchListener, ActionMode.Callback
{
    private final static int ACTIVITY_RESULT_SETTINGS = 0;

    private MainFragment _frgMain;
    private GestureDetectorCompat _detector;
    private ActionMode _actionMode;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            _frgMain.updateSavingStatus((Trip) msg.obj);
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        // Inflate the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            _frgMain = new MainFragment();
            getFragmentManager ().beginTransaction ().add (R.id.container,
                    _frgMain).commit();
        }

        // Start the log service
        startService(new Intent(this, LogMyTripService.class));

        // Detect gestures
        _detector = new GestureDetectorCompat(this, new RecyclerViewDemoOnGestureListener());
        _detector.setIsLongpressEnabled(true);
    }

    @Override
    protected void onStart ()
    {
        super.onStart();

        _frgMain.getRecyclerView().addOnItemTouchListener(this);
    }

    @Override
    protected void onStop ()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.main,
                                    menu);

        MenuItem save;

        save = menu.findItem(R.id.action_save);
        if (save != null) {
            if (SettingsManager.isLogTrip(this)) {
                save.setIcon(android.R.drawable.ic_media_pause);
            } else {
                save.setIcon(android.R.drawable.ic_menu_save);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        switch (item.getItemId ()) {
            case R.id.action_settings:
                showPreferences ();
                return true;

            case R.id.action_save:
                if (SettingsManager.isLogTrip(this)) {
                    ServiceManager.stopSaveTrip(this, handler);
                    item.setIcon(android.R.drawable.ic_menu_save);
                } else {
                    ServiceManager.startSaveTrip(this, handler);
                    item.setIcon(android.R.drawable.ic_media_pause);
                }

                return true;

            default:
                return super.onOptionsItemSelected (item);
        }
    }

    private void showPreferences ()
    {
        startActivityForResult(new Intent(this,
                        SettingsActivity.class),
                ACTIVITY_RESULT_SETTINGS);
    }

    /**********************************************************************************/
/*
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = MotionEventCompat.getActionMasked(event);
        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(MainActivity.class.getCanonicalName(),"Action was DOWN");
                return true;
            case (MotionEvent.ACTION_MOVE) :
                Log.d(MainActivity.class.getCanonicalName(), "Action was MOVE");
                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(MainActivity.class.getCanonicalName(),"Action was UP");
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                Log.d(MainActivity.class.getCanonicalName(),"Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(MainActivity.class.getCanonicalName(),"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
*/
/*
    @Override
    public boolean onDown(MotionEvent e) {
        ToastHelper.showShort(this, "down!!!");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        ToastHelper.showShort(this, "show press!!!");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        ToastHelper.showShort(this, "single tap up!!!");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        ToastHelper.showShort(this, "scroll!!!");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        ToastHelper.showShort(this, "Long press!!!");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        ToastHelper.showShort(this, "fling!!!");
        return false;
    }
*/
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        _detector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        ToastHelper.showShort(this, "touch!!!");
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ToastHelper.showShort(this, "disallowintercept!!!");
    }


    public void onClick(View view) {
/*
        if (view.getId() == R.id.fab_add) {
            // fab click
            addItemToList();
        } else
*/
        if (view.getId() == R.id.rvTrips) {
            // item click
            int idx = _frgMain.getRecyclerView().getChildPosition(view);
            if (_actionMode != null) {
                myToggleSelection(idx);
                return;
            }
/*
            DemoModel data = adapter.getItem(idx);
            View innerContainer = view.findViewById(R.id.container_inner_item);
            innerContainer.setTransitionName(Constants.NAME_INNER_CONTAINER + "_" + data.id);
            Intent startIntent = new Intent(this, CardViewDemoActivity.class);
            startIntent.putExtra(Constants.KEY_ID, data.id);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, innerContainer, Constants.NAME_INNER_CONTAINER);
            this.startActivity(startIntent, options.toBundle());
*/
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
/*
        MenuInflater inflater = _actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_cab_recyclerviewdemoactivity, menu);
        fab.setVisibility(View.GONE);
*/

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
/*
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                List<Integer> selectedItemPositions = adapter.getSelectedItems();
                int currPos;
                for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                    currPos = selectedItemPositions.get(i);
                    RecyclerViewDemoApp.removeItemFromList(currPos);
                    adapter.removeData(currPos);
                }
                actionMode.finish();
                return true;
            default:
                return false;
        }
*/
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        _actionMode = null;
        // adapter.clearSelections();
        // fab.setVisibility(View.VISIBLE);
    }

    private void myToggleSelection(int idx) {
        TripItemAdapter adapter;

        adapter = (TripItemAdapter) _frgMain.getRecyclerView().getAdapter();
        adapter.toggleSelection(idx);

        String title = getString(R.string.selected_count, adapter.getSelectedItemCount());

        _actionMode.setTitle(title);
    }

    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = _frgMain.getRecyclerView().findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = _frgMain.getRecyclerView().findChildViewUnder(e.getX(), e.getY());
            if (_actionMode != null) {
                return;
            }
            // Start the CAB using the ActionMode.Callback defined above
            _actionMode = startActionMode(MainActivity.this);
            int idx = _frgMain.getRecyclerView().getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }

///*
//    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            View view = _frgMain.getRecyclerView().findChildViewUnder(e.getX(), e.getY());
//            onClick(view);
//            return super.onSingleTapConfirmed(e);
//        }
//
//        public void onLongPress(MotionEvent e) {
//            View view = _frgMain.getRecyclerView().findChildViewUnder(e.getX(), e.getY());
//            if (_actionMode != null) {
//                return;
//            }
//            // Start the CAB using the ActionMode.Callback defined above
//            _actionMode = startActionMode(MainActivity.this);
//            int idx = _frgMain.getRecyclerView().getChildPosition(view);
//            myToggleSelection(idx);
//            super.onLongPress(e);
//        }
//    }
//
//  //  @Override
//    public void onClick(View view) {
//        if (view.getId() == R.id.rvTrips) {
//            // item click
//            int idx = _frgMain.getRecyclerView().getChildPosition(view);
//            if (_actionMode != null) {
//                myToggleSelection(idx);
//                return;
//            }
///*
//            Trip data = _frgMain.getRecyclerView().getAdapter().getItem(idx);
//            View innerContainer = view.findViewById(R.id.container_inner_item);
//            innerContainer.setTransitionName(Constants.NAME_INNER_CONTAINER + "_" + data.id);
//            Intent startIntent = new Intent(this, CardViewDemoActivity.class);
//            startIntent.putExtra(Constants.KEY_ID, data.id);
//            ActivityOptions options = ActivityOptions
//                    .makeSceneTransitionAnimation(this, innerContainer, Constants.NAME_INNER_CONTAINER);
//            this.startActivity(startIntent, options.toBundle());
//*/
//        }
//    }
//
//    private void myToggleSelection(int idx) {
//        ((TripItemAdapter) _frgMain.getRecyclerView().getAdapter()).toggleSelection(idx);
//
//        // String title = getString(R.string.selected_count, adapter.getSelectedItemCount());
//        _actionMode.setTitle("EOEOE");
//    }
//
//    @Override
//    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//        return false;
//    }
//
//    @Override
//    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//        return false;
//    }
//
//    @Override
//    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//        return false;
//    }
//
//    @Override
//    public void onDestroyActionMode(ActionMode mode) {
//
//    }
}
