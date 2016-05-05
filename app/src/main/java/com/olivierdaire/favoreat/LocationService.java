package com.olivierdaire.favoreat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

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
    public final static int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    private static double latitude;
    private static double longitude;
    private static LocationManager locationManager;

    public LocationService(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 && !checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, context)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSION_ACCESS_FINE_LOCATION, (Activity) context);
        }
        fetchLocationData();
    }

    public static void fetchLocationData(){
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null){
            // Case where the user has disabled location
            latitude = DEFAULT_LATITUDE;
            longitude = DEFAULT_LONGITUDE;
            // TODO Warn user
            //Toast.makeText(activity,"Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();
        } else {
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

    public static void requestPermission(final String strPermission, final int code, final Activity activity){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, strPermission)){
            Snackbar.make(activity.findViewById(R.id.parentCoordinator), R.string.GPS_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(activity, new String[]{strPermission}, code);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{strPermission}, code);
        }
    }

    public static boolean checkPermission(String strPermission, Context context){
        int permission = ContextCompat.checkSelfPermission(context, strPermission);
        if (permission == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }


}
