package mvf.mikevidev.walkandsee.UtilitiesWalkAndSee;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Utilities
{
    public static String key = "AIzaSyBdqGweqcB97eit2khrIBE6yagMjSb8dIg";
    public static final String basicColorApp = "#3f48cc";
    public static boolean isBlank(String text)
    {
        if(text == null)
        {
            return true;
        }
        else if("".equals(text) || "".equals(text.trim()))
        {
            return true;
        }
        return false;
    }

    public static void toastMessage(String text, Context context)
    {
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }
    //This method will format the distance to be presented to the user
    public static String calculateDistance(float flDistance)
    {
        //Get list of places to operate in the future
        String distanceFormatted = "";

        //if distance is 0 probably the location has not either latitude or longitude
        if(flDistance == 0)
        {
            return "??? Mts";
        }

        if(flDistance < 1000)
        {
            distanceFormatted = String.valueOf(Math.round(flDistance));
        }
        else
        {
            distanceFormatted = String.valueOf((Math.round((flDistance / 1000)* 100.00) / 100.00));
            try
            {
                Log.i("DISTANCE_STR",distanceFormatted);
                Log.i("DISTANCE_STR_L",String.valueOf(distanceFormatted.split(".").length));
                if(distanceFormatted.split("\\.").length == 1)
                {
                    Log.i("DISTANCE_STR","inside option 1");
                    distanceFormatted += ".00";
                }
                else if(distanceFormatted.split("\\.").length == 2 && distanceFormatted.split("\\.")[1].length() < 2)
                {
                    Log.i("DISTANCE_STR","inside option 2");
                    distanceFormatted += "0";
                }
            }
            catch(Exception e)
            {
                distanceFormatted = "No distance available";
                return distanceFormatted;
            }
        }

        distanceFormatted += (flDistance < 1000 ? " Mts" : " Kms");

        return distanceFormatted;
    }
}
