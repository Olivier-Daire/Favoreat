package com.olivierdaire.favoreat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * This class gives access to the position of the user
 *
 * @author Olivier Daire
 * @version 1.0
 * @since 19/04/16
 */
public class LocationService {
    // Set Paris coordinates as default
    private final static double DEFAULT_LATITUDE = 48.8534100;
    private final static double DEFAULT_LONGITUDE = 2.3488000;
    private final static int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    private double latitude;
    private double longitude;

    public LocationService(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        initLocationService(locationManager, context);
    }

    /**
     * Initiate location handling permissions and disabled GPS cases
     * @param locationManager System location service
     * @param context current activity
     */
    private void initLocationService(LocationManager locationManager, Context context){
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null){
            // Case where the user has disabled location
            latitude = DEFAULT_LATITUDE;
            longitude = DEFAULT_LONGITUDE;
            // TODO Warn user
        } else {
            if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, MY_PERMISSION_ACCESS_FINE_LOCATION);
            }
            Location currentLocation = locationManager.getLastKnownLocation(provider);
            latitude = currentLocation.getLatitude();
            longitude = currentLocation.getLongitude();
        }
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }
}
