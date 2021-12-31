package mvf.mikevidev.walkandsee.viewmodels;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import mvf.mikevidev.walkandsee.R;
import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.FinderHelper;
import mvf.mikevidev.walkandsee.models.WalkAndSeePlace;
import mvf.mikevidev.walkandsee.repositories.LoadingPlacesActivity;

public class RouteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
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
        int intMaxDistance = 0;
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng startLocation = new LatLng(LoadingPlacesActivity.myPlaceToStartRoute.getPlaceLat(), LoadingPlacesActivity.myPlaceToStartRoute.getPlaceLon());
        mMap.addMarker(new MarkerOptions().position(startLocation).title("You are here"));

        for(WalkAndSeePlace wasp : PlacesActivity.lstPlacesSelected)
        {
            mMap.addMarker(new MarkerOptions().position(wasp.getPlaceLocation()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(wasp.getPlaceName()));
            int intFromOrigin = (int) wasp.getFlDistanceFromOrigin();
            if(intFromOrigin > intMaxDistance)
            {
                intMaxDistance = intFromOrigin;
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, FinderHelper.getMapZoom(intMaxDistance)));
    }
}