package boxfox.shakenote.service;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ScreenOnService extends Service {

    private BroadcastReceiver mReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mReceiver = new ScreenOnReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);

        if(intent != null){
            if(intent.getAction()==null){
                if(mReceiver==null){
                    mReceiver = new ScreenOnReciver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                    registerReceiver(mReceiver, intentFilter);
                }
            }
        }
        startForeground(1, new Notification());
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
    }
}
