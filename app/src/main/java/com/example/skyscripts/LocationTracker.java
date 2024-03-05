package com.example.skyscripts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * Created by Bhuvaneshwaran
 * on 7:33 PM, 3/2/2023
 *
 * @author AcuteCoder
 */

@SuppressLint("StaticFieldLeak")
public class LocationTracker {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000;
    public static String TAG = LocationTracker.class.getName();
    private static Context context;
    private static LocationTracker instance;
    protected LocationManager locationManager;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    private Location location = null;
    private OnLocationUpdateListener listener;
    public LocationListener locationProviderListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            try {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                updateLocation(latitude, longitude);

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

    public static synchronized LocationTracker getInstance(Context context) {
        LocationTracker.context = context;
        if (instance == null) {
            instance = new LocationTracker();
        }
        return instance;
    }

    public void connectToLocation(OnLocationUpdateListener listener) {
        this.listener = listener;
        stopLocationUpdates();
        displayLocation();
    }

    private void displayLocation() {
        try {
            Log.e(TAG, "displayLocation");
            Location location = getLocation();
            if (location != null) {
                updateLocation(location.getLatitude(), location.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                this.canGetLocation = false;
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    Log.e(TAG + "-->Network", "Network Enabled");
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "nk");
                            return null;
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationProviderListener);
                        return location;
                    }
                } else if (isGPSEnabled) {
                    Log.e(TAG + "-->GPS", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationProviderListener);
                        return location;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void updateLocation(double latitude, double longitude) {
        Log.e(TAG, "updated Lat == " + latitude + "  updated long == " + longitude);
        listener.onUpdate(latitude, longitude);
    }

    public void stopLocationUpdates() {
        try {
            if (locationManager != null) {
                Log.e(TAG, "stopLocationUpdates");
                locationManager.removeUpdates(locationProviderListener);
                locationManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnLocationUpdateListener {
        void onUpdate(double latitude, double longitude);
    }

}