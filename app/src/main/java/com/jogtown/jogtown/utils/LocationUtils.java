package com.jogtown.jogtown.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;

public class LocationUtils {

    public static void createPolyline() {

    }


    public static double calculateDistanceBetweenTwoLatLngs(LatLng startCoordinates, LatLng endCoordinates) {
        return SphericalUtil.computeDistanceBetween(startCoordinates, endCoordinates);
    }

    public static int calculateTotalDistance(List arrayOfLatLngs) {
        //It won't make sense for me to just pick the start location and end location and calculate
        //the distance between the two. This distance will not be accurate as the jogger may have used
        //convoluted jog paths to get to that location.
        //I believe the better approach is to calculate small distances between each close latlngs and
        //sum it all up.
        return 0;
    }

    public static double distance(double lat1, double lng1,
                                  double lat2, double lng2){
        double a = (lat1-lat2)* LocationUtils.distPerLat(lat1);
        double b = (lng1-lng2)* LocationUtils.distPerLng(lat1);
        return Math.sqrt(a*a+b*b) * 0.23d;
    }

    private static double distPerLng(double lat){
        return 0.0003121092*Math.pow(lat, 4)
                +0.0101182384*Math.pow(lat, 3)
                -17.2385140059*lat*lat
                +5.5485277537*lat+111301.967182595;
    }

    private static double distPerLat(double lat){
        return -0.000000487305676*Math.pow(lat, 4)
                -0.0033668574*Math.pow(lat, 3)
                +0.4601181791*lat*lat
                -1.4558127346*lat+110579.25662316;
    }

}
