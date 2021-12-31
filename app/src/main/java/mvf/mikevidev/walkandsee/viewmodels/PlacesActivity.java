package mvf.mikevidev.walkandsee.viewmodels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.CreateRouteActivity;
import mvf.mikevidev.walkandsee.repositories.LoadingPlacesActivity;
import mvf.mikevidev.walkandsee.R;
import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.Utilities;
import mvf.mikevidev.walkandsee.adapters.WalkAndSeePlaceAdapter;
import mvf.mikevidev.walkandsee.models.WalkAndSeePlace;

public class PlacesActivity extends AppCompatActivity {

    public WalkAndSeePlaceAdapter arrAdapter;
    public RecyclerView rvPlaces;
    public static ArrayList<WalkAndSeePlace> lstPlacesSelected;
    public BottomNavigationView bottomNavigation;
    public static String navBotTitle;
    public static boolean isAllSelected = false;
    //TODO: find de.hdodenhof:circleimageview:2.2.0 to add the images inside image view
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottomnav_select_all:
                    selectAllItems(item);
                    break;
                case R.id.bottomnav_backtosearch:
                    moveToActivity(false);
                    break;
                case R.id.bottomnav_createroute:
                    moveToActivity(true);
                    break;
            }
            return false;
        }
    };

    //Methods from menu
    public void selectAllItems(MenuItem item) {

        if (isAllSelected == false) {
            item.setTitle("Deselect All");

            for (WalkAndSeePlace wasp : LoadingPlacesActivity.lstWalkAndSeePlaces)
            {
                wasp.setSelected(true);
            }

            isAllSelected = true;
        } else {
            item.setTitle("Select All");
            for (WalkAndSeePlace wasp : LoadingPlacesActivity.lstWalkAndSeePlaces)
            {
                wasp.setSelected(false);
            }

            isAllSelected = false;
        }
        arrAdapter.notifyDataSetChanged();

    }

    //Method to move to activities
    public void moveToActivity(boolean blnMoveToCreateRoute) {
        Intent intent;
        if (blnMoveToCreateRoute == false)
        {
            intent = new Intent(getApplicationContext(), CreateRouteActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            if (isAnyItemSelected() == false)
            {
                Utilities.toastMessage("You need to select one place at least", getApplicationContext());
            }
            else
            {
                lstPlacesSelected = new ArrayList<>();
                intent = new Intent(getApplicationContext(), RouteActivity.class);
                for (WalkAndSeePlace wasp : LoadingPlacesActivity.lstWalkAndSeePlaces)
                {
                    if (wasp.isSelected() == true)
                    {
                        lstPlacesSelected.add(wasp);
                    }
                }
                startActivity(intent);
                finish();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        int radius = getIntent().getIntExtra("intRadius", 0);
        getSupportActionBar().hide();
        setTitle("Places within " + (radius < 1000 ? radius + " Mts" : (radius / 1000) + " Kms"));
        rvPlaces = findViewById(R.id.rvPlaces);
        bottomNavigation = findViewById(R.id.bottomNavigationBar);
        navBotTitle = "Select All";
        bottomNavigation.setOnNavigationItemSelectedListener(navListener);
        rvPlaces.setLayoutManager(new LinearLayoutManager(this));
        arrAdapter = new WalkAndSeePlaceAdapter();
        rvPlaces.setAdapter(arrAdapter);
        arrAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadingPlacesActivity.lstWalkAndSeePlaces.clear();
    }

    public boolean isAnyItemSelected()
    {
        for(WalkAndSeePlace wasp : LoadingPlacesActivity.lstWalkAndSeePlaces)
        {
            Log.i("Item_sel","Item select " + wasp.isSelected());
            if(wasp.isSelected() == true)
            {
                return true;
            }
        }
        return false;
    }

}