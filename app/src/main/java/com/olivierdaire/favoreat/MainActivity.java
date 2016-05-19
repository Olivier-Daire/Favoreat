package com.olivierdaire.favoreat;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import android.support.design.widget.NavigationView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SortDialogFragment.EditNameDialogListener, GoogleMap.OnMarkerClickListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private LocationService locationService;
    private GoogleMap map;
    private List<Restaurant> listRestaurants;
    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;
    private FloatingActionButton addFAB;
    private double destinationLatitude = 0;
    private double destinationLongitude = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get list of the restaurants
        createListRestaurants();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.logo_white);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // FAB
        addFAB = (FloatingActionButton) findViewById(R.id.fab);
        addFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectorActivity.class);
                startActivity(intent);
            }
        });

        // FAB
        FloatingActionButton destinationFAB = (FloatingActionButton) findViewById(R.id.destination_fab);
        destinationFAB.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destinationLatitude+ "," + destinationLongitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        // Bottom sheet
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED){
                    bottomSheetBehavior.setPeekHeight(0);
                    addFAB.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        // NAVIGATION DRAWER

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){

                    case R.id.home:
                        Toast.makeText(getApplicationContext(),"Home Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.account:
                        Toast.makeText(getApplicationContext(),"Account Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.friends:
                        Toast.makeText(getApplicationContext(),"Friends Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                //Fetch the data remotely

                //Reset the SearchView
                /*searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();*/

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_sort:
                FragmentManager fm = getFragmentManager();
                SortDialogFragment dialogFragment = new SortDialogFragment();
                dialogFragment.show(fm, "Sample Fragment");

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        locationService = new LocationService(MainActivity.this);
        placeUserMarker();
        placeRestaurantMarkers();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LocationService.MY_PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationService.fetchLocationData();
                    placeUserMarker();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.GPS_permission_denied, Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    /**
     * Create the favorite restaurants list by reading into preference xml file
     */
    public void createListRestaurants() {

        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this.getApplicationContext());

        listRestaurants = new ArrayList<Restaurant>();
        Map<String,?> mapPref = appSharedPrefs.getAll();
        for(Map.Entry<String,?> entry : mapPref.entrySet()){
            Gson gson = new Gson();
            String json = appSharedPrefs.getString(entry.getKey(), "");
            listRestaurants.add(gson.fromJson(json, Restaurant.class));
        }
    }

    /**
     * Place favorite restaurants list markers and the current location marker
     */
    public void placeRestaurantMarkers(){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        Iterator<Restaurant> restaurantIterator = listRestaurants.iterator();
        int i = 0;
        while (restaurantIterator.hasNext()) {
            Restaurant r = restaurantIterator.next();
            LatLng rLatLng = new LatLng(r.getLatitude(), r.getLongitude());
            List<Address> rAddresses = null;
            try {
                rAddresses  = geocoder.getFromLocation(r.getLatitude(),r.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.setOnMarkerClickListener(this);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker_rest);
            map.addMarker(new MarkerOptions().position(rLatLng).icon(icon).title(Integer.toString(i)).snippet("restaurant"));
            // FIXME Hacky way to retrieve index, use hash map instead to link an index to a marker
            i++;
        }
    }

    /**
     * Place the marker of the current location
     */
    public void placeUserMarker(){
        LatLng latLng = new LatLng(locationService.getLatitude(), locationService.getLongitude());
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses  = geocoder.getFromLocation(locationService.getLatitude(),locationService.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker_user);
        map.addMarker(new MarkerOptions().position(latLng).icon(icon).title(addresses.get(0).getAddressLine(0)).snippet("user"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        if (!marker.getSnippet().equals("user")){
            int index = Integer.parseInt(marker.getTitle());
            Restaurant restaurant = listRestaurants.get(index);

            map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

            TextView title = (TextView)findViewById(R.id.bottom_sheet_title);
            TextView type = (TextView)findViewById(R.id.bottom_sheet_type);
            TextView price = (TextView)findViewById(R.id.bottom_sheet_price);
            RatingBar note = (RatingBar)findViewById(R.id.bottom_sheet_note);

            if (title != null) {
                title.setText(restaurant.getName()); // FIXME set restaurant name
            }
            if (type != null) {
                type.setText(restaurant.getType()); // FIXME set restaurant name
            }
            if (price != null) {
                price.setText(Integer.toString(restaurant.getAveragePrice()) + "$"); // FIXME set restaurant name
            }
            if (note != null) {
                note.setRating(restaurant.getRating());
            }

            // Update destination address
            destinationLatitude = restaurant.getLatitude();
            destinationLongitude = restaurant.getLongitude();

            addFAB.setVisibility(View.INVISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        return true;
    }


    @Override
    public void onFinishEditDialog(int inputPrice, String inputType, int inputRating) {
        sortRestaurant(inputPrice, inputType, inputRating);
    }

    /**
     * Replace restaurants markers after sort
     * @param inputPrice
     * @param inputType
     * @param inputRating
     */
    public void sortRestaurant(int inputPrice, String inputType, int inputRating) {
        map.clear();
        placeUserMarker();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Iterator<Restaurant> restaurantIterator = listRestaurants.iterator();
        while (restaurantIterator.hasNext()) {
            Restaurant r = restaurantIterator.next();
            if ((r.getAveragePrice() < inputPrice) && ( (r.getType().equals(inputType)) || (inputType.equals("All")) ) && ( (r.getRating() == inputRating) || (inputRating == 0) ) ) {
                LatLng rLatLng = new LatLng(r.getLatitude(), r.getLongitude());
                List<Address> rAddresses = null;
                try {
                    rAddresses = geocoder.getFromLocation(r.getLatitude(), r.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.mipmap.marker_rest);
                map.addMarker(new MarkerOptions().position(rLatLng).icon(icon).title(rAddresses.get(0).getAddressLine(0)));

            }
        }
    }
}

