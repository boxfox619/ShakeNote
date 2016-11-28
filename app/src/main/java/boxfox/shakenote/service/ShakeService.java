package boxfox.shakenote.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;

import boxfox.shakenote.DialogActivity;
import boxfox.shakenote.NoteActivity;
import boxfox.shakenote.R;
import boxfox.shakenote.components.Item;
import boxfox.shakenote.components.ServiceState;
import boxfox.shakenote.components.Setting;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Administrator on 2016-08-12.
 */
public class ShakeService extends Service implements SensorEventListener {
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        unregisterRestartAlarm();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerormeterSensor != null)
            sensorManager.registerListener(this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDestroy() {
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(ShakeService.this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        Realm realm = Realm.getDefaultInstance();
        ServiceState serviceState = realm.where(ServiceState.class).findFirst();
        if(serviceState.isRun()){
            registerRestartAlarm();
        }
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    void registerRestartAlarm() {
        Intent intent = new Intent(ShakeService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_SHAKESERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                ShakeService.this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1 * 1000;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10 * 1000, sender);
    }

    void unregisterRestartAlarm() {
        Intent intent = new Intent(ShakeService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_SHAKESERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(
                ShakeService.this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            RealmConfiguration realmConfig = new RealmConfiguration
                    .Builder(this)
                    .deleteRealmIfMigrationNeeded()
                    .build();
            Realm.setDefaultConfiguration(realmConfig);
            Realm realm = Realm.getDefaultInstance();
            Setting set = realm.where(Setting.class).findFirst();
            if (gabOfTime > (160+(set.getSensitivity())*5)) {
                lastTime = currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
                if (speed > SHAKE_THRESHOLD) {
                    if(realm.where(Item.class).count()==1){
                        String packageName = realm.where(Item.class).findFirst().getPackageName();
                        if(packageName.equals(getResources().getString(R.string.note_package))){
                            Intent intent = new Intent(this, NoteActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            this.startActivity(intent);
                        }else {
                            Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(intent);
                        }
                    }else {
                        Intent intent = new Intent(this, DialogActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(intent);
                    }
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
