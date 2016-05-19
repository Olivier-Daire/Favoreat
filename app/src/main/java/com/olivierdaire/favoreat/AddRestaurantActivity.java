package com.olivierdaire.favoreat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class AddRestaurantActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Receive data from background OCR Service
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String text = bundle.getString(OCRService.RECOGNIZED_TEXT);
                int resultCode = bundle.getInt(OCRService.RESULT);
                if (resultCode == RESULT_OK) {
                    // send text to display
                    TextView restaurantName = (TextView) findViewById(R.id.RestaurantName);
                    restaurantName.setText(text);
                } else {
                    Toast.makeText(AddRestaurantActivity.this, "Image recognition failed, sorry about that !", Toast.LENGTH_LONG).show();                }
            }
        }
    };
    private TextView textView;
    private SeekBar seekBar;
    private Spinner spin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_restaurant);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        seekBar = (SeekBar) findViewById(R.id.RestaurantPrice);
        textView = (TextView) findViewById(R.id.textPrice);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            EditText editName = (EditText) findViewById(R.id.RestaurantName);
            editName.setText((String)bundle.get("RESTAURANT_NAME"));
        }

        // get spinner item
        spin = (Spinner) findViewById(R.id.typeSpinnerRest);

        List<String> list = Arrays.asList(getResources().getStringArray(R.array.type_array));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(dataAdapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Set Price Text
        textView.setText(seekBar.getProgress() + " $");
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                textView.setText(progress + " $");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_add_restaurant, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.validate:
                onClickValidate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        LocationService locationService = new LocationService(AddRestaurantActivity.this);
        LatLng latLng = new LatLng(locationService.getLatitude(), locationService.getLongitude());

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses  = geocoder.getFromLocation(locationService.getLatitude(),locationService.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(addresses.get(0).getAddressLine(0)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));

        final List<Address> finalAddresses = addresses;

        TextView restAdress = (TextView) findViewById(R.id.RestaurantAdress);
        String completeAddress = finalAddresses.get(0).getAddressLine(0) + " " + finalAddresses.get(0).getPostalCode() + " " + finalAddresses.get(0).getLocality();
        assert restAdress != null;
        restAdress.setText(completeAddress);



    }

    public void onClickValidate() {

        Context context = getApplicationContext();

        // Get views by ID
        EditText editName = (EditText) findViewById(R.id.RestaurantName);
        EditText editAddress = (EditText) findViewById(R.id.RestaurantAdress);
        TextView editPrice = (TextView) findViewById(R.id.textPrice);
        String price = editPrice.getText().toString().replaceAll("[^0-9]", "");
        RatingBar editRate = (RatingBar) findViewById(R.id.RestaurantNote);

        String editSpin = spin.getSelectedItem().toString();

        if (editName.getText().toString().matches("")) {
            CharSequence text = getString(R.string.no_name);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (editAddress.getText().toString().matches("")) {
            CharSequence text = getString(R.string.no_address);
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        // Get the latitude and longitude from address
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        double latitude = 0;
        double longitude = 0;
        try {
            address = coder.getFromLocationName(editAddress.getText().toString(), 5);
            Address location = address.get(0);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create new restaurant object
        Restaurant newRestaurant = new Restaurant(editName.getText().toString(), latitude, longitude, editSpin, Integer.parseInt(price), Math.round(editRate.getRating()));

        // Save de new restaurant in shared preference file
        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(newRestaurant);
        prefsEditor.putString(newRestaurant.getName(), json);
        prefsEditor.commit();

        // Return to main activity
        Intent returnBtn = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(returnBtn);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(OCRService.RECEIVER));
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
