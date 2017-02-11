package boxfox.shakenote;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Utils {


    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }else{
            return Settings.canDrawOverlays(context);
        }
    }
}