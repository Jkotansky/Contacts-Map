package com.comp590.wgmiller.contactmap;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private GoogleApiClient c = null;
    LocationRequest mLocationRequest;
    Location loc;
    String note = "";
    SupportMapFragment mapFragment = null;
    private GoogleMap gMap;
    //Geocoder stuff to get Addresses:
    private Geocoder geocoder;
    List<Address> addresses;
    String address;
    String city;
    String state;
    String country;
    String postalCode;
    //Button to see contacts from the app:
    public FloatingActionButton contactButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Clicking the contact Button starts contact list activity
        contactButton = (FloatingActionButton) findViewById(R.id.contactButton);
        contactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, contactListActivity.class));
            }
        });

        //Clicking the Maps Button starts contact list activity
        FloatingActionButton mapsButton = (FloatingActionButton) findViewById(R.id.mapsButton);
        mapsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainActivity.this, PlotContacts.class));
            }
        });


        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        c = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Initialize GeoCoder:
        geocoder = new Geocoder(this, Locale.getDefault());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent i = new Intent(getApplicationContext(), contactListActivity.class);
//                startActivity(i)

                // Creates a new Intent to insert a contact
               Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);

                // Sets the MIME type to match the Contacts Provider
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                intent.putExtra(ContactsContract.Intents.Insert.NOTES, note);
                //Declare that the Address type will be custom
                intent.putExtra(ContactsContract.Intents.Insert.POSTAL, address + " \n" + city +", " + state + " " + postalCode);
                //Save the Custom Address as "Met At:"
                //extras.putString(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, "Met At:");
                //Add the address and other information:
                //extras.putString(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address);
                //extras.putString(ContactsContract.CommonDataKinds.StructuredPostal.CITY, city);
                //extras.putString(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, postalCode);
                //extras.putString(ContactsContract.CommonDataKinds.StructuredPostal.REGION, state);
                //intent.putExtras(extras);
                startActivity(intent);

//
//                Snackbar.make(view, "Adding Contact", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
        //map.addMarker(new MarkerOptions().position(new LatLng(0, -79)).title("Marker"));
        if(loc != null){
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(loc.getLatitude(), loc.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude()))      // Sets the center of the map to location user
                    .zoom(14)                   // Sets the zoom
                    .bearing(-30)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("TAG", "We are connected to Google Services");
        try {
            loc = LocationServices.FusedLocationApi.getLastLocation(c);
            Log.v("LOC", "" + loc.getLatitude() + ", " + loc.getLongitude());
            note = "" + loc.getLatitude() + "," + loc.getLongitude();
            try {
                //Begin geocoder and save to the address list variable
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                //Take address list variable and parse out information:
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            gMap.clear();// This will make sure that duplicates do not occur
            gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Current"));
            onMapReady(gMap);


            LocationServices.FusedLocationApi.requestLocationUpdates(c, mLocationRequest, this);
        }
        catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        String s = "error: " + connectionResult.getErrorCode();
        Log.v("TAG", s);
    }
    protected void onStart() {
        c.connect();
        super.onStart();
    }

    protected void onStop() {
        c.disconnect();
        super.onStop();
    }
}
