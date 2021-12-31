package mvf.mikevidev.walkandsee.UtilitiesWalkAndSee;

import android.util.Log;

//This class will contain methods to process the places found in each city
public class FinderHelper
{
    public static final String MUSEUM_TYPE = "museum";
    public static final String PARK_TYPE = "park";
    public static final String RESTAURANT_TYPE = "restaurant";
    public static final String NIGHT_CLUB_TYPE = "night_club";
    public static final String BAR_TYPE = "bar";
    public static final int MAX_RADIUS = 5000;
    public static final int MIN_RADIUS = 100;
    public static final int INCREASE_RADIUS = 50;
    public static final float MIN_ZOOM = 11.8f;
    public static float MAX_ZOOM = 17.45f;

    //Return the zoom to apply in the map based on the distance provided
    public static float getMapZoom(int distance)
    {
        //The zoom will be between 20 (max zoom allowed in Google maps) and the minimun which adjust to the screen
        float flzoom = 0f;
        if(distance == MIN_RADIUS)
        {
            flzoom = MAX_ZOOM;
        }
        else if(distance > MIN_RADIUS && distance <= 150)
        {
            flzoom = MAX_ZOOM - 0.5f;
        }
        else if(distance > 150 && distance <= 500)
        {
            flzoom = MAX_ZOOM - 2.2f;
        }
        else if(distance > 500 && distance <= 700)
        {
            flzoom = MAX_ZOOM - 2.8f;
        }
        else if(distance > 700 && distance <= 1200)
        {
            flzoom = MAX_ZOOM - 3.5f;
        }
        else if(distance > 1200 && distance < 4900)
        {
            //The zoom will be between 20 (max zoom allowed in Google maps) and the minimun which adjust to the screen
            float flZoomThreshold = (MAX_ZOOM - 3.5f) - MIN_ZOOM;
            float percentatgeProgress = (distance * 100) / (MAX_RADIUS - MIN_RADIUS);
            float substract = flZoomThreshold * (percentatgeProgress / 100);
            //Calculate the percentage to discount to the max zoom based on the percentage we increase the radius and considering the threshold
            if(distance > 2500 && distance < 4500)
            {
                substract += 0.2f;
            }
            flzoom = (MAX_ZOOM - 3.5f) - substract;
        }
        else
        {
            flzoom = MIN_ZOOM;
        }
        //Log.i("increaseAndDecreaseZoom","substract: " + substract);
        Log.i("increaseAndDecreaseZoom","Progress: " + distance);
        Log.i("increaseAndDecreaseZoom","flzoom: " + flzoom);

        return flzoom;
    }
}
