package com.example.sunw2347.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Location myLocation;
    private EditText LocationSearch;
    private GoogleMap mMap;
    private LocationManager locationManager;

    private boolean gotMyLocationOneTime;
    private static final long MIN_TIME_BW_UPDATES = 1000*5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES  = 0.0F;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    private boolean isNetworkEnabled = false;
    private boolean isGPSEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sanDiego = new LatLng(32.7157, -117.1611);
        mMap.addMarker(new MarkerOptions().position(sanDiego).title("I was born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sanDiego));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed FINE permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed COARSE permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            Log.d("MyMapsApp", "Either FINE or COARSE Passed permission check");
            mMap.setMyLocationEnabled(true);
        }

        LocationSearch = (EditText) findViewById(R.id.editText_search);
        gotMyLocationOneTime = false;
        getLocation();

    }

    public void changeView(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void onClear(View view){
        mMap.clear();
    }


    public void onSearch(View v) {
        String location = LocationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListzip = null;
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Log.d("MyMapsApp", "on Search: location = " + location);
        Log.d("MyMapsApp", "on Search: provider = " + provider);

        LatLng userlocation = null;

        //check the last known location, specifically list the provider network or gps
        try {
            if (service != null) {
                Log.d("MyMapsApp", "onSearch: LocationManger is not null");
            }
            if ((myLocation = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is:" + myLocation.getLatitude() + " " + myLocation.getLongitude());
                Toast.makeText(this, "UserLog" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
            } else if ((myLocation = service.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                userlocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is:" + myLocation.getLatitude() + " " + myLocation.getLongitude());
                Toast.makeText(this, "UserLog" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
            } else {
                Log.d("MyMapsApp", "onSearch: myLocation is null");

            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Expectation on getLastKnownLocation");

        }
        // Create Geocoder
        if (!location.matches("")) {
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                addressList = geocoder.getFromLocationName(location, 100,
                        userlocation.latitude,
                        userlocation.longitude,
                        userlocation.latitude,
                        userlocation.longitude);
                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }
    }
    public void getLocation()
    {
        try
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //get GPS status
            //isProviderEnabled returns true is user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled)
            {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }
            //get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled)
            {
                Log.d("MyMapsApp", "getLocation: Network is enabled");
            }

            if(!isGPSEnabled&&!isNetworkEnabled)
            {

                Log.d("MyMapsApp", "getLocation: no provider is enabled");

            }
            else
            {
                if(isNetworkEnabled)
                {
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerNetwork);
                    Log.d("MyMapsApp", "getLocation: Network is enabled");

                }
                if(isGPSEnabled)
                {
                    //launch locationlistener
                }
            }
        }
        catch(Exception e)
        {
            Log.d("MyMapsApp", "getLocation: Caught and exception");
            e.printStackTrace();

        }
    }
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    //locationListener is an anonymous inner class
    //setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMapsApp", "getLocation: Network is enabled");
            dropAmarker(LocationManager.NETWORK_PROVIDER);
            // Check if doing one time via onMapReady, if so remove updates to both gps and network
            if(gotMyLocationOneTime==false)
            {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);

            }
            else
            {
                //if here then we are tracking so relaunch request for network
                if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListenerNetwork);
            }
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
            switch(status) {
                case LocationProvider.AVAILABLE:
                Log.d("MyMapsApp","Provider is available");
                break;
                case LocationProvider.OUT_OF_SERVICE:
                //enable network updates
                break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
                default:
                //enable both network and gps;
            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAmarker(String provider){
        /*if(locationManager != null)
            if(checkSelfPermission() fails)
                return;
        myLocation = locationManager.getLastKnownLocation(provider)
                LatLng userLocation = null;
        if(myLocation == null) print log or toast
                else
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude())
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation,MY_LOC_ZOOM_FACTOR)
        if(provider == LocationManager.GPS_PROVIDER)
            add circle for the marker with 2 outer rings(red)
                mMap.addCircle(newCircleOptions())
                .center(UserLocation)
                .radius(1)
                .strokeColor(Color.RED)
                .strokeWidth(2)
                .fillColor(Color.RED))
        else
            add circle for the marker with 2 outer rings (blue)
                mMap.addCircle(newCircleOptions())
                        .center(UserLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE))
        mMap.animateCamera(update);*/
    }

    public void trackMyLocation(View view){
        //kick off the location tracker using getLocation to start the LocationListeners
        //if(notTrackingMyLocation){
        //      getLocation();
        //      notTrackingMyLocation = false;
        //      else{
        //          removeUpdates for both network and gps;
        //          notTrackingMyLocation = true;
        //
        //      }
        //}

    }
}