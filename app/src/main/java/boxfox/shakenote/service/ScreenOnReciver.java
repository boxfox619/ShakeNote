package boxfox.shakenote.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONObject;
import boxfox.shakenote.R;

public class ScreenOnReciver extends BroadcastReceiver {
    private JSONObject obj = new JSONObject();
    @Override
    public void onReceive(final Context context, Intent intent){
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            if(!isServiceRunning(context,context.getResources().getString(R.string.serviceName))){
                Intent Service = new Intent(context, ShakeService.class);
                context.startService(Service);
            }
        }
    }

    public Boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
