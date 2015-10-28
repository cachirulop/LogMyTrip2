package com.cachirulop.logmytrip.util;

import android.graphics.Color;
import android.graphics.Point;

import com.cachirulop.logmytrip.entity.Trip;
import com.cachirulop.logmytrip.entity.TripLocation;
import com.cachirulop.logmytrip.entity.TripSegment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmagro on 27/10/2015.
 */
public class MapHelper
{
    private static final int[] SEGMENT_COLORS = new int[]{ Color.RED, Color.BLUE, Color.GREEN,
                                                           Color.MAGENTA, Color.YELLOW };
    ArrayList<Polyline>    _arrows        = new ArrayList<> ();
    ArrayList<TripSegment> _drawnSegments = new ArrayList<> ();
    private GoogleMap _map;
    private boolean   _drawn;

    public MapHelper (GoogleMap map)
    {
        _map = map;
    }

    public void drawTrip (Trip trip, boolean isActiveTrip)
    {
        List<TripSegment>    segments;
        int                  currentColor;
        int                  lastSegmentIndex;
        int                  currentIndex;
        LatLngBounds.Builder builder;

        _drawn = true;

        builder = new LatLngBounds.Builder ();

        segments = trip.getSegments (true);
        currentIndex = 0;
        lastSegmentIndex = segments.size () - 1;

        currentColor = 0;

        // if (segments.size () > 0 && !isActiveTrip && !SettingsManager.isLogTrip (ctx)) {
        _map.setOnCameraChangeListener (new CameraListener (_map, builder));
        // }

        for (TripSegment s : segments) {
            boolean isActiveSegment;

            isActiveSegment = (isActiveTrip) && (currentIndex == lastSegmentIndex);
            privateDrawSegment (s, builder, isActiveSegment, SEGMENT_COLORS[currentColor]);

            currentColor++;
            currentColor = currentColor % SEGMENT_COLORS.length;
            currentIndex++;
        }
    }

    private void privateDrawSegment (TripSegment segment, LatLngBounds.Builder builder, boolean isActiveSegment, int color)
    {
        List<TripLocation> points;
        MarkerOptions      markerOptions;
        List<LatLng>       track;

        _drawnSegments.add (segment);

        points = segment.getLocations ();

        // Start mark
        markerOptions = new MarkerOptions ();
        markerOptions.position (points.get (0)
                                      .toLatLng ());
        // markerOptions.icon (BitmapDescriptorFactory.fromResource (R.mipmap.id_start_log));
        _map.addMarker (markerOptions);

        // End mark
        if (!isActiveSegment) {
            markerOptions = new MarkerOptions ();
            markerOptions.position (points.get (points.size () - 1)
                                          .toLatLng ());
            _map.addMarker (markerOptions);
        }

        track = new ArrayList<LatLng> ();

        int i = 0;
        for (TripLocation p : points) {
            LatLng current;

            current = p.toLatLng ();

            track.add (current);
            builder.include (current);

            i++;
        }

        Polyline        route;
        PolylineOptions routeOptions;
        Polyline        border;
        PolylineOptions borderOptions;

        routeOptions = new PolylineOptions ();
        routeOptions.width (5);
        routeOptions.color (color);
        routeOptions.geodesic (true);
        routeOptions.zIndex (1);

        borderOptions = new PolylineOptions ();
        borderOptions.width (10);
        borderOptions.color (Color.GRAY);
        borderOptions.geodesic (true);
        routeOptions.zIndex (0);

        border = _map.addPolyline (borderOptions);
        route = _map.addPolyline (routeOptions);

        route.setPoints (track);
        border.setPoints (track);
    }

    public void dawSegment (TripSegment segment, int color)
    {
        LatLngBounds.Builder builder;

        builder = new LatLngBounds.Builder ();

        _map.setOnCameraChangeListener (new CameraListener (_map, builder));

        privateDrawSegment (segment, builder, false, color);

        _map.moveCamera (CameraUpdateFactory.newLatLngBounds (builder.build (), 0));
    }

    public void drawArrows ()
    {
        // TODO: Rethink this method
        int          currentColor;
        Projection   proj;
        LatLngBounds bounds;

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
                    addArrow (proj, arrowsLine, validPoints.get (i), SEGMENT_COLORS[currentColor]);
                }
            }

            currentColor++;
            currentColor = currentColor % SEGMENT_COLORS.length;
        }

        for (PolylineOptions l : arrowsLine) {
            _arrows.add (_map.addPolyline (l));
        }
    }

    private void addArrow (Projection proj, List<PolylineOptions> arrowsLine, TripLocation location, int color)
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

        p1 = new LatLng (
                rotateLatitudeAround (p1.latitude, p1.longitude, location.getBearing (), current),
                rotateLongitudeAround (p1.latitude, p1.longitude, location.getBearing (), current));
        p2 = new LatLng (
                rotateLatitudeAround (p2.latitude, p2.longitude, location.getBearing (), current),
                rotateLongitudeAround (p2.latitude, p2.longitude, location.getBearing (), current));

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
        lat = center.latitude + (Math.cos (
                Math.toRadians (angle)) * (lat - center.latitude) - Math.sin (
                Math.toRadians (angle)) * (lon - center.longitude));

        return lat;
    }

    public double rotateLongitudeAround (double lat, double lon, double angle, LatLng center)
    {
        lon = center.longitude + (Math.sin (
                Math.toRadians (angle)) * (lat - center.latitude) + Math.cos (
                Math.toRadians (angle)) * (lon - center.longitude));

        return lon;
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    private class CameraListener
            implements GoogleMap.OnCameraChangeListener
    {
        private GoogleMap            _map;
        private LatLngBounds.Builder _builder;

        public CameraListener (GoogleMap map, LatLngBounds.Builder builder)
        {
            _map = map;
            _builder = builder;
        }

        @Override
        public void onCameraChange (CameraPosition arg0)
        {
            // Move camera.
            if (_drawn) {
                _map.moveCamera (CameraUpdateFactory.newLatLngBounds (_builder.build (), 50));

                _drawn = false;
            }

            drawArrows ();

            // Remove listener to prevent position reset on camera move.
            // _map.setOnCameraChangeListener (null);
        }
    }
}



