package mvf.mikevidev.walkandsee;

import android.content.Context;
import android.widget.Toast;

public class Utilities
{
    public static String key = "";
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
}
