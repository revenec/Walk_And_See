package mvf.mikevidev.walkandsee.viewmodels;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.FinderHelper;
import mvf.mikevidev.walkandsee.repositories.LoadingPlacesActivity;
import mvf.mikevidev.walkandsee.R;
import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.Utilities;

public class SearchPlacesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public SeekBar sbRadious;
    public LocationManager locationManager;
    public LocationListener locationListener;
    public Location locationUser;
    public CheckBox cbShowAll;
    public CheckBox cbShowRestaurants;
    public CheckBox cbShowParks;
    public CheckBox cbShowMuseums;
    public CheckBox cbShowNightClubs;
    public CheckBox cbShowBars;
    public CheckBox cbOnlyOpen;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check if the user grants permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            //if the user has already give permission, the process will jump the pop up
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3, locationListener);
            //Get the current location at the begin and add a list of the places
            locationUser = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationUser == null) {
                locationUser = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (locationUser != null) {
                // Add a marker in Sydney and move the camera
                sbRadious.setProgress(FinderHelper.MIN_RADIUS);
                increaseAndDecreaseZoom(sbRadious.getProgress(),true);

            }
            else
            {
                Utilities.toastMessage("There is not signal. check your internet connection or your GPS and try again",getApplicationContext());
            }
        }
    }

    public void goFindPlaces(View view)
    {
        //Variable to hold the parameters
        ArrayList<String> params = new ArrayList<>();

        boolean isOptionSelected = false;
        if(cbShowAll.isChecked())
        {
            params.add(FinderHelper.MUSEUM_TYPE);
            params.add(FinderHelper.PARK_TYPE);
            params.add(FinderHelper.RESTAURANT_TYPE);
            params.add(FinderHelper.NIGHT_CLUB_TYPE);
            params.add(FinderHelper.BAR_TYPE);
            isOptionSelected = true;
        }
        else
        {
            if(cbShowRestaurants.isChecked())
            {
                params.add(FinderHelper.RESTAURANT_TYPE);
                isOptionSelected = true;
            }
            if(cbShowParks.isChecked())
            {
                params.add(FinderHelper.PARK_TYPE);
                isOptionSelected = true;
            }
            if(cbShowMuseums.isChecked())
            {
                params.add(FinderHelper.MUSEUM_TYPE);
                isOptionSelected = true;
            }
            if(cbShowNightClubs.isChecked())
            {
                params.add(FinderHelper.NIGHT_CLUB_TYPE);
                isOptionSelected = true;
            }
            if(cbShowBars.isChecked())
            {
                params.add(FinderHelper.BAR_TYPE);
                isOptionSelected = true;
            }
        }

        if(isOptionSelected == false)
        {
            Utilities.toastMessage("We need to know what you want to find, please select an option :)",getApplicationContext());
        }
        else
        {
            boolean isOpen;
            //Open Places Activity passing per parameter the radius and the options selected if "Show All is not selected"
            Intent intent = new Intent(getApplicationContext(), LoadingPlacesActivity.class);
            Log.i("PROGRESS","Progress: " + sbRadious.getProgress());
            intent.putExtra("intRadius",sbRadious.getProgress());
            intent.putExtra("placesType",params);
            if(cbOnlyOpen.isChecked())
            {
                isOpen = true;
            }
            else
            {
                isOpen = false;
            }
            intent.putExtra("onlyopen",isOpen);
            startActivity(intent);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places);
        //Get the value in the checkboxes
        cbShowAll = findViewById(R.id.allOpt);
        cbShowRestaurants = findViewById(R.id.restaurantsOpt);
        cbShowParks = findViewById(R.id.parksOpt);
        cbShowMuseums = findViewById(R.id.museumsOpt);
        cbShowNightClubs = findViewById(R.id.nightClubsOpt);
        cbShowBars = findViewById(R.id.barsOpt);
        cbOnlyOpen = findViewById(R.id.cbOnlyOpen);
        screenAdjustments();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location)
            {
                locationUser = location;
                sbRadious.setProgress(FinderHelper.MIN_RADIUS);
                increaseAndDecreaseZoom(sbRadious.getProgress(),true);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                Utilities.toastMessage("Check your internet connection, something is not working :(", getApplicationContext());
            }
        };
    }

    private void screenAdjustments()
    {
        //Get the max zoom based on the device
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point(); display. getSize(size);
        Log.i("screenAdjustments", "size" + size.x);
        switch(size.x)
        {
            case 1440:
                FinderHelper.MAX_ZOOM = 17.8f;
                break;
            case 480:
                FinderHelper.MAX_ZOOM = 17.3f;
                cbShowAll.setTextSize(12);
                cbShowRestaurants.setTextSize(12);
                cbShowParks.setTextSize(12);
                cbShowMuseums.setTextSize(12);
                cbShowNightClubs.setTextSize(12);
                cbShowBars.setTextSize(12);
                cbOnlyOpen.setTextSize(12);
                break;
            default:
        }
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

        sbRadious = findViewById(R.id.sbDistance);
        sbRadious.setMax(FinderHelper.MAX_RADIUS);
        sbRadious.setMin(FinderHelper.MIN_RADIUS);
        sbRadious.setProgress(FinderHelper.INCREASE_RADIUS);
        sbRadious.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                //Get the progress and make zoom +/- based if the progress increase or decrease
                increaseAndDecreaseZoom(progress,false);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //Check if the user grants permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            //if the user has already give permission, the process will jump the pop up
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3, locationListener);
            //Get the current location at the begin and add a list of the places
            locationUser = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationUser == null) {
                locationUser = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (locationUser != null) {
                // Add a marker in Sydney and move the camera
                sbRadious.setProgress(FinderHelper.MIN_RADIUS);
                increaseAndDecreaseZoom(sbRadious.getProgress(),true);

            }
            else
            {
                Utilities.toastMessage("There is not signal. check your internet connection or your GPS and try again",getApplicationContext());
            }
        }

    }
    //increase the zoom and the circle based on the distance
    public void increaseAndDecreaseZoom(int barProgress, boolean blnUseDynamic)
    {
        mMap.clear();
        LatLng currentLocation = new LatLng(locationUser.getLatitude(), locationUser.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here").icon(BitmapDescriptorFactory.fromResource(R.drawable.lognoletters1_logo)).anchor(0.5f,0.5f));
        if(blnUseDynamic)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,FinderHelper.MAX_ZOOM));
        }
        else
        {

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,FinderHelper.getMapZoom(barProgress)));
        }

        mMap.addCircle(new CircleOptions()
                .center(currentLocation)
                .radius(barProgress)
                .strokeWidth(5)
                .strokeColor(Color.parseColor(Utilities.basicColorApp)));
    }

}