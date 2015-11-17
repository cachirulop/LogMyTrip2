package com.cachirulop.logmytrip.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cachirulop.logmytrip.R;
import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by dmagro on 27/10/2015.
 */
public class MapHelper
{
    private static final int   COLOR_TRANSPARENCY        = 40;
    private static final int[] SEGMENT_COLORS = new int[]{ Color.RED, Color.BLUE, Color.GREEN,
                                                           Color.MAGENTA, Color.YELLOW };
    private static final int[] SEGMENT_COLORS_UNSELECTED = new int[]{
            Color.argb (COLOR_TRANSPARENCY, 255, 0, 0), Color.argb (COLOR_TRANSPARENCY, 0, 0, 255),
            Color.argb (COLOR_TRANSPARENCY, 0, 255, 0),
            Color.argb (COLOR_TRANSPARENCY, 255, 0, 255),
            Color.argb (COLOR_TRANSPARENCY, 255, 255, 0) };

    private static final int     SEGMENT_BACKGROUND_COLOR            = Color.GRAY;
    private static final float[] MARKER_COLORS                       = new float[]{
            BitmapDescriptorFactory.HUE_RED, BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_YELLOW };
    private static final int     SEGMENT_BACKGROUND_COLOR_UNSELECTED = Color.argb (
            COLOR_TRANSPARENCY,
            88,
            88,
            88);


    private ArrayList<Polyline> _arrows        = new ArrayList<> ();
    private List<TripSegment>   _drawnSegments = new ArrayList<> ();
    private TripSegment _selectedSegment;
    private GoogleMap                        _map              = null;
    private boolean                          _drawn            = false;
    private MarkerClickListener              _markerListener   = new MarkerClickListener ();
    private MapClickListener                 _mapClickListener = new MapClickListener ();
    private WeakHashMap<Marker, TripSegment> _markerSegment    = new WeakHashMap<> ();
    private Context _ctx;

    public MapHelper (Context ctx)
    {
        _ctx = ctx;
    }

    public void setMap (GoogleMap map)
    {
        _map = map;
        _map.setInfoWindowAdapter (new TripSegmentMapInfoWindowAdapter (_ctx));
        _map.setOnMarkerClickListener (_markerListener);
        _map.setOnMapClickListener (_mapClickListener);
        _arrows.clear ();
        _drawnSegments.clear ();
    }

    public void drawTrip (Trip trip, boolean isActiveTrip)
    {
        if (_map != null) {
            drawSegmentList (trip.getSegments (), isActiveTrip);
        }
    }

    private void drawSegmentList (List<TripSegment> segments, boolean isActiveTrip)
    {
        drawSegmentList (segments, isActiveTrip, null);
    }

    private void drawSegmentList (List<TripSegment> segments,
                                  boolean isActiveTrip,
                                  MapListener listener)
    {
        LatLngBounds.Builder builder;
        int                  lastSegmentIndex;
        int                  currentIndex;
        boolean hasPoints;

        if (_map != null) {
            _map.clear ();
            _drawn = true;

            hasPoints = false;
            builder = new LatLngBounds.Builder ();

            currentIndex = 0;
            lastSegmentIndex = segments.size () - 1;

            for (TripSegment s : segments) {
                boolean isActiveSegment;

                isActiveSegment = (isActiveTrip) && (currentIndex == lastSegmentIndex);
                privateDrawSegment (s, builder, isActiveSegment);

                currentIndex++;

                hasPoints = (hasPoints || s.getStartLocation () != null);
            }

            _map.setOnCameraChangeListener (new CameraListener (_map,
                                                                builder,
                                                                isActiveTrip,
                                                                listener));
            if (hasPoints && !isActiveTrip) {
                if (_selectedSegment == null) {
                    _map.animateCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 50));
                }
                else {
                    _map.animateCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 90));
                }
            }
        }
    }

    private void privateDrawSegment (TripSegment segment,
                                     LatLngBounds.Builder builder, boolean isActiveSegment)
    {
        List<TripLocation> points;
        MarkerOptions      markerOptions;
        List<LatLng>       track;
        int     zIndex;
        float   alpha;
        boolean showInfo;
        Marker  marker;
        int     lineWidth;
        int     borderWidth;
        int colorIndex;
        int color;
        int backgroundColor;

        if (_map != null) {
            if (!_drawnSegments.contains (segment)) {
                _drawnSegments.add (segment);
            }

            zIndex = 0;
            alpha = 1.0f;
            showInfo = false;
            lineWidth = 5;
            borderWidth = 10;
            colorIndex = segment.getIndex () % SEGMENT_COLORS.length;
            color = SEGMENT_COLORS[colorIndex];
            backgroundColor = SEGMENT_BACKGROUND_COLOR;

            if (_selectedSegment != null) {
                if (_selectedSegment.equals (segment)) {
                    zIndex = 10;
                    showInfo = true;
                    lineWidth = 8;
                    borderWidth = lineWidth * 2;
                }
                else {
                    alpha = ((float) COLOR_TRANSPARENCY) / 100;
                    color = SEGMENT_COLORS_UNSELECTED[colorIndex];
                    backgroundColor = SEGMENT_BACKGROUND_COLOR_UNSELECTED;
                }
            }

            points = segment.getLocations ();

            // Start mark
            markerOptions = new MarkerOptions ();
            markerOptions.position (points.get (0).toLatLng ());
            markerOptions.icon (BitmapDescriptorFactory.defaultMarker (getMarkerColor (color)));
            markerOptions.alpha (alpha);
            markerOptions.title (segment.getTitle (_ctx));

            if (segment.getStartLocation () != null) {
                markerOptions.snippet (String.format ("%s\n%s",
                                                      FormatHelper.formatDateTime (_ctx,
                                                                                   segment.getStartDate ()),
                                                      FormatHelper.formatDateTime (_ctx,
                                                                                   segment.getStartDate ())));
            }

            marker = _map.addMarker (markerOptions);
            _markerSegment.put (marker, segment);
            if (showInfo) {
                marker.showInfoWindow ();
            }

            // End mark
            if (!isActiveSegment) {
                markerOptions = new MarkerOptions ();
                markerOptions.position (points.get (points.size () - 1).toLatLng ());
                markerOptions.icon (BitmapDescriptorFactory.defaultMarker (getMarkerColor (color)));
                markerOptions.alpha (alpha);

                _map.addMarker (markerOptions);
            }

            track = new ArrayList<LatLng> ();

            int i = 0;
            for (TripLocation p : points) {
                LatLng current;

                current = p.toLatLng ();

                track.add (current);

                if (_selectedSegment == null || _selectedSegment.equals (segment)) {
                    builder.include (current);
                }

                i++;
            }

            Polyline route;
            PolylineOptions routeOptions;
            Polyline border;
            PolylineOptions borderOptions;

            routeOptions = new PolylineOptions ();
            routeOptions.width (lineWidth);
            routeOptions.color (color);
            routeOptions.geodesic (true);
            routeOptions.zIndex (zIndex + 1);

            borderOptions = new PolylineOptions ();
            borderOptions.width (borderWidth);
            borderOptions.color (backgroundColor);
            borderOptions.geodesic (true);
            routeOptions.zIndex (zIndex);

            border = _map.addPolyline (borderOptions);
            route = _map.addPolyline (routeOptions);

            route.setPoints (track);
            border.setPoints (track);
        }
    }

    private float getMarkerColor (int color)
    {
        for (int i = 0 ; i < SEGMENT_COLORS.length ; i++) {
            if (SEGMENT_COLORS[i] == color || SEGMENT_COLORS_UNSELECTED[i] == color) {
                return MARKER_COLORS[i];
            }
        }

        return MARKER_COLORS[0];
    }

    public void drawSegment (TripSegment segment)
    {
        drawSegment (segment, null);
    }

    public void drawSegment (TripSegment segment, MapListener listener)
    {
        LatLngBounds.Builder builder;

        if (_map != null) {
            builder = new LatLngBounds.Builder ();

            privateDrawSegment (segment, builder, false);

            _map.setOnCameraChangeListener (new CameraListener (_map, builder, false, listener));
            _map.animateCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 50));
        }
    }

    public void drawArrows ()
    {
        // TODO: Rethink this method
        int          currentColor;
        Projection   proj;
        LatLngBounds bounds;

        if (_map != null) {
            proj = _map.getProjection ();
            bounds = proj.getVisibleRegion ().latLngBounds;

            for (Polyline arrow : _arrows) {
                arrow.remove ();
            }

            _arrows.clear ();

            ArrayList<PolylineOptions> arrowsLine;

            arrowsLine = new ArrayList<> ();

            currentColor = 0;

            for (TripSegment s : _drawnSegments) {
                List<TripLocation> points;
                ArrayList<TripLocation> validPoints;

                validPoints = new ArrayList<> ();

                points = s.getLocations ();

                for (int i = 0 ; i < points.size () ; i++) {
                    if (i > 0 && i < (points.size () - 1)) {
                        TripLocation p;
                        LatLng current;

                        p = points.get (i);
                        current = p.toLatLng ();

                        if (bounds.contains (current)) {
                            validPoints.add (p);
                        }
                    }
                }

                int interval;

                if (validPoints.size () > 5) {
                    interval = validPoints.size () / 5;
                }
                else {
                    interval = validPoints.size ();
                }

                for (int i = 0 ; i < validPoints.size () ; i++) {
                    if (i % interval == 0) {
                        addArrow (proj, arrowsLine, validPoints.get (i),
                                  SEGMENT_COLORS[currentColor]);
                    }
                }

                currentColor++;
                currentColor = currentColor % SEGMENT_COLORS.length;
            }

            for (PolylineOptions l : arrowsLine) {
                _arrows.add (_map.addPolyline (l));
            }
        }
    }

    private void addArrow (Projection proj,
                           List<PolylineOptions> arrowsLine,
                           TripLocation location,
                           int color)
    {
        LatLng p1;
        LatLng p2;
        LatLng current;

        Point center;
        Point left;
        Point right;
        int   screenFactorX;
        int   screenFactorY;
        int   arrowWidth;

        current = location.toLatLng ();
        screenFactorX = 8;
        screenFactorY = 14;

        arrowWidth = 4;

        center = proj.toScreenLocation (current);

        left = new Point ();
        left.set (center.x - screenFactorX, center.y + screenFactorY);

        right = new Point ();
        right.set (center.x + screenFactorX, center.y + screenFactorY);

        p1 = proj.fromScreenLocation (left);
        p2 = proj.fromScreenLocation (right);

        p1 = new LatLng (rotateLatitudeAround (p1.latitude,
                                               p1.longitude,
                                               location.getBearing (),
                                               current),
                         rotateLongitudeAround (p1.latitude,
                                                p1.longitude,
                                                location.getBearing (),
                                                current));
        p2 = new LatLng (rotateLatitudeAround (p2.latitude,
                                               p2.longitude,
                                               location.getBearing (),
                                               current),
                         rotateLongitudeAround (p2.latitude,
                                                p2.longitude,
                                                location.getBearing (),
                                                current));

        PolylineOptions arrowLine;

        arrowLine = new PolylineOptions ();
        arrowLine.add (current, p1);
        arrowLine.width (arrowWidth);
        arrowLine.color (Color.GRAY);
        arrowLine.zIndex (10);

        arrowsLine.add (arrowLine);

        arrowLine = new PolylineOptions ();
        arrowLine.add (current, p1);
        arrowLine.width (arrowWidth - 2);
        arrowLine.color (color);
        arrowLine.zIndex (11);

        arrowsLine.add (arrowLine);

        arrowLine = new PolylineOptions ();
        arrowLine.add (current, p2);
        arrowLine.width (arrowWidth);
        arrowLine.color (Color.GRAY);
        arrowLine.zIndex (10);

        arrowsLine.add (arrowLine);

        arrowLine = new PolylineOptions ();
        arrowLine.add (current, p2);
        arrowLine.width (arrowWidth - 2);
        arrowLine.color (color);
        arrowLine.zIndex (11);

        arrowsLine.add (arrowLine);
    }

    public double rotateLatitudeAround (double lat, double lon, double angle, LatLng center)
    {
        lat = center.latitude + (Math.cos (Math.toRadians (angle)) * (lat - center.latitude) - Math.sin (
                Math.toRadians (angle)) * (lon - center.longitude));

        return lat;
    }

    public double rotateLongitudeAround (double lat, double lon, double angle, LatLng center)
    {
        lon = center.longitude + (Math.sin (Math.toRadians (angle)) * (lat - center.latitude) + Math
                .cos (Math.toRadians (angle)) * (lon - center.longitude));

        return lon;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    public interface MapListener
    {
        void onMapLoaded ();
    }

    private class CameraListener
            implements GoogleMap.OnCameraChangeListener
    {
        private GoogleMap            _map;
        private LatLngBounds.Builder _builder;
        private boolean _isActiveTrip;
        private boolean _moved;
        private MapListener _listener;

        public CameraListener (GoogleMap map,
                               LatLngBounds.Builder builder,
                               boolean isActiveTrip,
                               MapListener listener)
        {
            _map = map;
            _builder = builder;
            _isActiveTrip = isActiveTrip;
            _moved = false;
            _listener = listener;
        }

        @Override
        public void onCameraChange (CameraPosition arg0)
        {
            // TODO: Improve the method
            // drawArrows ();

            if (_listener != null) {
                _listener.onMapLoaded ();
            }

            _moved = true;
        }
    }

    //////////////////////////////////////////////////////////////////

    private class MarkerClickListener
            implements GoogleMap.OnMarkerClickListener
    {
        @Override
        public boolean onMarkerClick (Marker marker)
        {
            TripSegment segment;

            _selectedSegment = locateSegment (marker.getPosition ());
            drawSegmentList (_drawnSegments, false);

            return true;
        }

        private TripSegment locateSegment (LatLng position)
        {
            for (TripSegment s : _drawnSegments) {
                List<TripLocation> locations;

                locations = s.getLocations ();
                if (locations.get (0)
                             .toLatLng ()
                             .equals (position) || locations.get (locations.size () - 1)
                                                            .toLatLng ()
                                                            .equals (position)) {
                    return s;
                }
            }

            return null;
        }
    }

    //////////////////////////////////////////////////////////////////
    private class MapClickListener
            implements GoogleMap.OnMapClickListener
    {

        @Override
        public void onMapClick (LatLng latLng)
        {
            if (_selectedSegment != null) {
                _selectedSegment = null;
                drawSegmentList (_drawnSegments, false);
            }
        }
    }


    //////////////////////////////////////////////////////////////////

    public class TripSegmentMapInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter
    {
        private Context _ctx;

        public TripSegmentMapInfoWindowAdapter (Context ctx)
        {
            _ctx = ctx;
        }

        @Override
        public View getInfoContents (Marker marker)
        {
            LayoutInflater inflater = (LayoutInflater) _ctx.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
            View           result;
            TripSegment    segment;

            result = inflater.inflate (R.layout.trip_segment_map_info_window, null);

            segment = _markerSegment.get (marker);

            bindData (result, segment);

            return result;
        }

        private void bindData (View view, TripSegment segment)
        {
            TripLocation l;
            StringBuffer timeInfo;

            setText (view, R.id.tvTripSegmentMapInfoTitle, segment.getTitle (_ctx));

            timeInfo = new StringBuffer ();

            l = segment.getStartLocation ();
            if (l != null) {
                timeInfo.append (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));
            }

            l = segment.getEndLocation ();
            if (l != null) {
                timeInfo.append (" - ");
                timeInfo.append (FormatHelper.formatTime (_ctx, l.getLocationTimeAsDate ()));
            }

            if (timeInfo.length () > 0) {
                setText (view, R.id.tvTripSegmentMapInfoTimeInfo, timeInfo.toString ());
            }

            setText (view,
                     R.id.tvTripSegmentMapInfoTotalDistance,
                     FormatHelper.formatDistance (segment.computeTotalDistance ()));
            setText (view,
                     R.id.tvTripSegmentMapInfoTotalTime,
                     FormatHelper.formatDuration (segment.computeTotalTime ()));
        }

        private void setText (View view, int id, String text)
        {
            TextView tv;

            tv = (TextView) view.findViewById (id);

            tv.setText (text);
        }

        @Override
        public View getInfoWindow (Marker marker)
        {
            return null;
        }
    }

}



