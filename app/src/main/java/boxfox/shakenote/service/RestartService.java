package boxfox.shakenote.service;

import android.app.AlarmManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class RestartService  extends BroadcastReceiver {
    public static final String ACTION_RESTART_SHAKESERVICE = "ACTION.RESTART.RestartService";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_RESTART_SHAKESERVICE)){
            Intent i = new Intent(context,ShakeService.class);
            context.startService(i);
        }
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent i = new Intent(context,ShakeService.class);
            context.startService(i);
        }
    }
}
