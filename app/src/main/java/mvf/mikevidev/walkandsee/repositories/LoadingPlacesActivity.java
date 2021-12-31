package mvf.mikevidev.walkandsee.repositories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Trace;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import mvf.mikevidev.walkandsee.R;
import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.Utilities;
import mvf.mikevidev.walkandsee.models.WalkAndSeePlace;
import mvf.mikevidev.walkandsee.viewmodels.PlacesActivity;

public class LoadingPlacesActivity extends AppCompatActivity {

    public LocationManager locationManager;
    public LocationListener locationListener;
    public Location locationUser;

    public String strSpecificPlace;
    public int intRadiusFromScreen;
    public ArrayList<String> strPlaceTypeFromScreen;
    public static List<WalkAndSeePlace> lstWalkAndSeePlaces;
    public static WalkAndSeePlace myPlaceToStartRoute;
    public int totalResults;
    public TextView tvMessage;

    //Manage database system
    public FirebaseDatabase firebaseDatabase;
    public FirebaseStorage firebaseStorage;
    public FirebaseFirestore firestore;
    public DatabaseReference dbPlaces;
    public StorageReference stImagePlaces;
    public FirebaseAuth firebaseAuth;

    public class DownloadImages extends AsyncTask<String,Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... urls) {
            URL url;
            try
            {
                url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                if(response == 200)
                {
                    InputStream input = conn.getInputStream();

                    Bitmap myBitmap = BitmapFactory.decodeStream(input);

                    return  myBitmap;
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    public class DownloadData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";

            try {

                for(String url : urls)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                    String input;
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((input = in.readLine()) != null) {
                        stringBuffer.append(input);
                    }
                    in.close();

                    if(!stringBuffer.toString().contains("ZERO_RESULTS"))
                    {
                        result += stringBuffer.toString() + "___";
                    }
                    Thread.sleep(1000);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //after execution fill up the list view with the locations
            String response;

            try {
                JSONObject JSONResult = new JSONObject(result);
                response = JSONResult.getString("status");
            } catch (JSONException e) {
                e.printStackTrace();
                response = "ZERO_RESULTS";
            }
            Log.i("Places:",response);

            if("ZERO_RESULTS".equals(response))
            {
                goToPlacesActivityWithNoResults();
            }
            else
            {
                getPlaceIds(result);
            }

            Log.i("Places: ", result);
        }

    }

    //Get place id and distance from current location, so we can sort places by proximity
    public ArrayList<String> getPlaceIds(String JSONString)
    {
        Log.i("TAG", "getPlaceIds ");
        ArrayList<String> lstPlaceIds = new ArrayList<>();
        ArrayList<String> lstPlaceCodes = new ArrayList<>();
        Map<Float,Integer> mapDistanceToPositionList = new TreeMap<>();
        Map<Integer,String> mapPositionListToPlaceId = new HashMap<>();
        Map<String, Float> mapPlaceIdToDistanceCurrentLocation = new HashMap<>();
        HashMap<String,String> mapPlaceIdToPhotoReferences = new HashMap<>();
        HashMap<String,String> mapPlaceIdToPlaceCode = new HashMap<>();

        int pos = 0;
        lstWalkAndSeePlaces.clear();
        //arrAdapter.notifyDataSetChanged();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return lstPlaceIds;
        }
        Location locationEndPoint = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(locationEndPoint == null)
        {
            locationEndPoint =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        try {

            for(String result : JSONString.split("___"))
            {
                result = result.replaceAll(" ","");
                JSONObject jsonObject = new JSONObject(result);
                JSONArray arrResults = jsonObject.getJSONArray("results");
                for(int i = 0; i < arrResults.length();i++)
                {
                    Log.i("PLACE_URL_DESCRIPTION",arrResults.getJSONObject(i).toString());
                    JSONObject info = arrResults.getJSONObject(i).getJSONObject("geometry");
                    JSONObject infoLoc = info.getJSONObject("location");
                    double latitude = (double) infoLoc.get("lat");
                    double longitude = (double) infoLoc.get("lng");
                    locationEndPoint.setLongitude(longitude);
                    locationEndPoint.setLatitude(latitude);
                    Log.i("LOCATION_USER",locationUser.toString());
                    Log.i("LOCATION_END_POINT",locationEndPoint.toString());
                    mapDistanceToPositionList.put(locationUser.distanceTo(locationEndPoint),pos);
                    mapPositionListToPlaceId.put(pos,arrResults.getJSONObject(i).getString("place_id"));
                    if(arrResults.getJSONObject(i).toString().contains("photos"))
                    {
                        JSONArray arrResultsPhotos = arrResults.getJSONObject(i).getJSONArray("photos");
                        if(arrResultsPhotos.length() > 0)
                        {
                            mapPlaceIdToPhotoReferences.put(arrResults.getJSONObject(i).getString("place_id"),arrResultsPhotos.getJSONObject(0).getString("photo_reference"));
                        }
                    }
                    //Get globalCode
                    try
                    {
                        JSONObject infoCode = arrResults.getJSONObject(i).getJSONObject("plus_code");
                        String infoGlobalCode = infoCode.getString("global_code").substring(0,6);
                        mapPlaceIdToPlaceCode.put(arrResults.getJSONObject(i).getString("place_id"),infoGlobalCode);
                        if(!lstPlaceCodes.contains(infoGlobalCode) && infoGlobalCode.length() <= 10)
                        {
                            lstPlaceCodes.add(infoGlobalCode);
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("PLUS_CODE","Not founded: " + e.getMessage());
                        lstPlaceCodes.add("");
                    }

                    pos++;
                }
            }

            for(Float key : mapDistanceToPositionList.keySet())
            {
                lstPlaceIds.add(mapPositionListToPlaceId.get(mapDistanceToPositionList.get(key)));
                mapPlaceIdToDistanceCurrentLocation.put(mapPositionListToPlaceId.get(mapDistanceToPositionList.get(key)),key);
            }

            Log.i("Photos",mapPlaceIdToPhotoReferences.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
        }
        //Get this value to find out when the process will finish, so we can order the places for distance in ascending order
        this.totalResults = lstPlaceIds.size();
        getPlaceIdsToQueryGooglePlaces(lstPlaceIds,lstPlaceCodes,mapPlaceIdToDistanceCurrentLocation,mapPlaceIdToPhotoReferences,mapPlaceIdToPlaceCode);
        return lstPlaceIds;
    }
    //Method to load places already exiting in the database in order to reduce to requests to google places
    private void getPlaceIdsToQueryGooglePlaces(ArrayList<String> lstPlaceIds,
                                                ArrayList<String> lstCodePlaces,
                                                Map<String, Float> mapPlaceIdToDistanceCurrentLocation,
                                                HashMap<String,String> mapPlaceIdToPhotoReferences,
                                                HashMap<String,String> mapPlaceIdToPlaceCode)
    {

        Log.e("CURRENT_USER", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //Get directory
        File storagePath = new File(Environment.getExternalStorageDirectory(), "WalkAndSeeImages");
        // Create directory if not exists
        if(!storagePath.exists()) {
            storagePath.mkdirs();
        }
        ArrayList<String> lstPlaceIdsToReturn = (ArrayList<String>) lstPlaceIds.clone();
        firestore.collection("Places").whereIn("placeCode",lstCodePlaces).orderBy("placeAddress").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                Log.i("getPlaceIdsToQueryGooglePlaces -> Data from FireStore", document.getId() + " => " + document.getData());
                                WalkAndSeePlace wasp = document.toObject(WalkAndSeePlace.class);
                                //Distance is not store in the system because the user could search for the same place but the location is different
                                float fDistance;
                                if(mapPlaceIdToDistanceCurrentLocation.get(wasp.getPlaceId()) != null)
                                {
                                    fDistance = mapPlaceIdToDistanceCurrentLocation.get(wasp.getPlaceId());
                                }
                                else
                                {
                                    fDistance = 0;
                                }

                                wasp.setPlaceDistance(Utilities.calculateDistance(fDistance));
                                wasp.setFlDistanceFromOrigin(fDistance);
                                //Get images and save the file in the local device to reduce the quantity of data in memory
                                try {

                                    File photo = new File(storagePath,wasp.getPlaceId() + ".jpg");
                                    if(photo.exists())
                                    {
                                        wasp.setPlacePhoto(BitmapFactory.decodeFile(photo.getPath()));
                                    }
                                    else
                                    {
                                        File localFile = File.createTempFile("Image",".jpg");
                                        stImagePlaces.child(wasp.getPlaceId() + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                            {
                                                Log.i("IMAGE","The image was found in the system - Path: " + localFile.getPath());
                                                wasp.setPlacePhoto(BitmapFactory.decodeFile(localFile.getPath()));
                                            }

                                        })
                                        .addOnFailureListener(new OnFailureListener()
                                        {
                                            @Override
                                            public void onFailure(@NonNull Exception e)
                                            {
                                                Log.e("IMAGE_ERROR","The image was not found in the system");
                                                wasp.setPlacePhoto(BitmapFactory.decodeResource(getResources(), R.drawable.empty_house));
                                            }
                                        });
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e("getPlaceIdsToQueryGooglePlaces -> ERROR", e.getMessage());
                                    wasp.setPlacePhoto(BitmapFactory.decodeResource(getResources(), R.drawable.empty_house));
                                }

                                lstWalkAndSeePlaces.add(wasp);
                                lstPlaceIdsToReturn.remove(wasp.getPlaceId());
                            }
                            processPlaceIds(lstPlaceIdsToReturn,mapPlaceIdToDistanceCurrentLocation,mapPlaceIdToPhotoReferences,mapPlaceIdToPlaceCode);
                        }
                        else
                        {
                            Log.e("Data from FireStore(FAILURE)", task.getException().toString());
                            goToPlacesActivityWithNoResults();
                        }

                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        //Close this page and will return to the previous one
                        Log.e("RETREIVE_DATA(FAILURE)", e.getMessage());
                        finish();
                    }
                });

        Log.i("getPlaceIdsToQueryGooglePlaces: ","Places from origin: " + lstPlaceIds.toString());

    }
    //Method to move to the next activity not showing results
    private void goToPlacesActivityWithNoResults()
    {
        WalkAndSeePlace wasRecord = new WalkAndSeePlace();
        wasRecord.setPlaceName("No places found");
        lstWalkAndSeePlaces.add(wasRecord);
        Intent intent = new Intent(getApplicationContext(), PlacesActivity.class);
        startActivity(intent);
        finish();
    }

    //Method created to query the places not existing in Firebase by uso
    public void processPlaceIds(ArrayList<String> lstPlaceIdsToQuery,
                                Map<String, Float> mapPlaceIdToDistanceCurrentLocation,
                                HashMap<String,String> mapPlaceIdToPhotoReferences,
                                HashMap<String,String> mapPlaceIdToPlaceCode)
    {

        if(!lstPlaceIdsToQuery.isEmpty())
        {
            for (String placeId : lstPlaceIdsToQuery)
            {
                Log.i("PLACE_ID_REQUEST: ",placeId);
                getPlace(placeId,mapPlaceIdToDistanceCurrentLocation,mapPlaceIdToPhotoReferences,mapPlaceIdToPlaceCode);
            }
        }
        else
        {
            Log.i("DownloadData: ","There is not places to retrieve from Google Place");
            moveToPlacesList();
        }
    }

    public void getPlace(String placeId,Map<String,Float> mapPlaceIdToDistanceCurrentLocation,HashMap<String,String> mapPlaceIdToPhotoReferences,HashMap<String,String> mapPlaceIdToplaceCode) {

        Trace.beginSection("SessionGetPlaces");

        //Set the information we want to retrieve from google
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        //Init places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), Utilities.key);
        }
        //Create client places and request places
        PlacesClient placesClient = Places.createClient(getApplicationContext());
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            WalkAndSeePlace walkAndSeePlace;
            Log.i("TAG", "Place found: " + place);

            if (place.getLatLng() != null)
            {
                float flDistance = 0.00f;
                if(mapPlaceIdToDistanceCurrentLocation.get(place.getId()) != null)
                {
                    flDistance = mapPlaceIdToDistanceCurrentLocation.get(place.getId());
                }

                Log.i("PLACE_ID_REQUEST_2: ",place.getId());
                walkAndSeePlace = new WalkAndSeePlace(place.getName(), place.getId(), place.getLatLng().latitude,place.getLatLng().longitude, null, place.getAddress() ,flDistance,Utilities.calculateDistance(flDistance),mapPlaceIdToplaceCode.get(place.getId()));
                //Get image from http request
                if(mapPlaceIdToPhotoReferences.get(place.getId()) != null)
                {
                    DownloadImages downloadImages = new DownloadImages();
                    String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + mapPlaceIdToPhotoReferences.get(place.getId()) + "&key=" + Utilities.key;
                    try {
                        walkAndSeePlace.setPlacePhoto(downloadImages.execute(url).get());
                        // Create a reference to "placeId.jpg"
                        StorageReference placeRef = stImagePlaces.child(place.getId() + ".jpg");

                        // Create a reference to 'Images/placeId.jpg'
                        StorageReference placeImagesRef = stImagePlaces.child("Images/" + place.getId() + ".jpg");

                        // While the file names are the same, the references point to different files
                        placeRef.getName().equals(placeImagesRef.getName());    // true
                        placeRef.getPath().equals(placeImagesRef.getPath());    // false

                        //Store image in database
                        ByteArrayOutputStream placeImageBytes = new ByteArrayOutputStream();
                        walkAndSeePlace.getPlacePhoto().compress(Bitmap.CompressFormat.JPEG, 100, placeImageBytes);
                        byte[] data = placeImageBytes.toByteArray();

                        UploadTask uploadTask = placeRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception)
                            {
                                // Handle unsuccessful uploads
                                Log.e("FAILED_UPLOAD","The image could not be uploaded");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                Log.e("SUCCESS_UPLOAD","The image was uploaded: " + taskSnapshot.toString());
                            }
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.i("INFO_GOOGLE_GET", walkAndSeePlace.toString());

                String strPlaceName = walkAndSeePlace.getPlaceName();
                if (Utilities.isBlank(strPlaceName) || "null".equals(strPlaceName)) {
                    strPlaceName = "Place not found";
                }
                else if(strPlaceName.length() > 38)
                {
                    strPlaceName = strPlaceName.substring(0,38) + "...";
                }
                walkAndSeePlace.setPlaceName(strPlaceName);
                lstWalkAndSeePlaces.add(walkAndSeePlace);
                //Add record to the database in Firebase

                firestore.collection("Places").add(walkAndSeePlace.toMapInstance()).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("ADDED_SUCCESS:","Added successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("ADDED_FAILED:","Added failed: " + e.getMessage());
                    }
                });

                //Create data to be showed in the list view
                Log.i("Places2: ", walkAndSeePlace.toString());

            }


        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.i("TAG", "Place not found: " + exception.getMessage());
            }
        }).addOnCompleteListener((task) -> {

            if(this.totalResults == lstWalkAndSeePlaces.size())
            {
                Log.i("TAG", "Inside adapter and notify change");
                moveToPlacesList();
            }

        });

        Trace.endSection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 3, locationListener);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }

            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_places);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        intRadiusFromScreen = getIntent().getIntExtra("intRadius",0);
        strPlaceTypeFromScreen = (ArrayList<String>)getIntent().getSerializableExtra("placesType");
        strSpecificPlace = null;
        lstWalkAndSeePlaces = new ArrayList<>();
        tvMessage = findViewById(R.id.tvMessage);

        //Get database and keep the dat offline
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                            .setPersistenceEnabled(true)
                                            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                                            .build();
        firestore.setFirestoreSettings(settings);
        firebaseAuth = FirebaseAuth.getInstance();
        dbPlaces = firebaseDatabase.getReference("Places");
        dbPlaces.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Log.i("DATA_CHANGE","Inside data change listener");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.i("DATA_CANCELLED","Inside data cancelled");
            }
        });
        stImagePlaces = firebaseStorage.getReference("Images");

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {}

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

    @Override
    protected void onResume()
    {
        super.onResume();
        initPlaceSearch();
    }

    public void initPlaceSearch() {
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
            if (locationUser != null)
            {
                LatLng myLatLng = new LatLng(locationUser.getLatitude(),locationUser.getLongitude());
                myPlaceToStartRoute = new WalkAndSeePlace("", null, myLatLng.latitude,myLatLng.longitude, null, "" ,0.00f,"0 Mts","");
                new Thread()
                {
                    @Override
                    public void run()
                    {
                        getPlacesFromGoogleMaps(locationUser, intRadiusFromScreen, strPlaceTypeFromScreen, strSpecificPlace);
                    }
                }.start();
            }
            else
            {
                Utilities.toastMessage("There is not signal. check your internet connection or your GPS and try again",getApplicationContext());
            }
        }

    }
    private void getPlacesFromGoogleMaps(Location location, int radius, ArrayList<String> lstPlaceTypes, String specificPlace) {
        DownloadData download = new DownloadData();
        ArrayList<String> urlsArray = new ArrayList<>();

        try
        {
            for(int i = 0; i< lstPlaceTypes.size(); i++)
            {
                StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

                url.append("location=").append(location.getLatitude()).append(",").append(location.getLongitude());
                url.append("&radius=").append(radius);

                if (!Utilities.isBlank(lstPlaceTypes.get(i))) {
                    url.append("&type=").append(lstPlaceTypes.get(i));
                }

                if (!Utilities.isBlank(specificPlace)) {
                    url.append("&keyword=").append(specificPlace);
                }
                //Only return records if the place is opened now
                if(getIntent().getBooleanExtra("onlyopen",false) == true)
                {
                    url.append("&open_now=true");
                }

                url.append("&key=" + Utilities.key);
                Log.i("url: ", url.toString());
                urlsArray.add(url.toString());
            }
            String[] urlsArrayToSend = new String[urlsArray.size()];
            for(int i = 0; i < urlsArrayToSend.length; i++)
            {
                urlsArrayToSend[i] = urlsArray.get(i);
            }
            download.execute(urlsArrayToSend).get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    //Method to move to places list, this can be called from PostExecute method in DownloadData(if there is no place to retrieve in google maps)or onComplete method in getPlace
    private void moveToPlacesList()
    {
        //Sort results and return to view
        Collections.sort(lstWalkAndSeePlaces);

        Intent intent = new Intent(getApplicationContext(),PlacesActivity.class);
        intent.putExtra("intRadius",intRadiusFromScreen);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }
}