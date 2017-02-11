package boxfox.shakenote;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

import boxfox.shakenote.components.Item;
import boxfox.shakenote.components.Note;
import boxfox.shakenote.components.ServiceState;
import boxfox.shakenote.components.Setting;
import boxfox.shakenote.service.ChatHeadService;
import boxfox.shakenote.service.ShakeService;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1234;
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG = 5678;
    private LinearLayout notelist;
    private Realm realm;
    private Note selectedNote;
    private boolean adCheck = false;
    private Switch serivceSwitch;
    private Switch headSwitch;
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.instance = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ((ImageButton)findViewById(R.id.imageButton)).getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        ((TextView)findViewById(R.id.label)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf"));
        notelist = (LinearLayout) findViewById(R.id.applist);
        setRealm();
        initialize();
        setNotelist();
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.bannerAdID));
        final AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(getString(R.string.bannerAdID));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if(!adCheck) {
                    ((LinearLayout) findViewById(R.id.rootView)).addView(mAdView);
                    adCheck =!adCheck;
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        initialize();
        setNotelist();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    public void setRealm(){
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(MainActivity.this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();
    }

    public void initialize(){
        if(realm.where(Setting.class).count()==0){
            realm.beginTransaction();
            realm.createObject(Setting.class);
            realm.commitTransaction();
        }
        int progress = realm.where(Setting.class).findFirst().getSensitivity();
        ((TextView)findViewById(R.id.sens)).setText("감도 :"+progress);
        ((SeekBar)findViewById(R.id.sensibility)).setProgress(progress);
        ((SeekBar)findViewById(R.id.sensibility)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RealmConfiguration realmConfig = new RealmConfiguration
                        .Builder(MainActivity.this)
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(realmConfig);
                Realm realm = Realm.getDefaultInstance();
                Setting set = realm.where(Setting.class).findFirst();
                realm.beginTransaction();
                set.setSensitivity(progress);
                realm.commitTransaction();
                ((TextView)findViewById(R.id.sens)).setText("감도 :" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        if(realm.where(Item.class).count()==0) {
            realm.beginTransaction();
            Item item = realm.createObject(Item.class);
            item.setApplicationName(getResources().getString(R.string.app_name));
            item.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
            item.setPackageName(getResources().getString(R.string.note_package));
            realm.commitTransaction();
        }

        if(realm.where(Setting.class).count()==0){
            realm.beginTransaction();
            realm.createObject(Setting.class);
            realm.commitTransaction();
        }

        ((ImageButton)findViewById(R.id.imageButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ApplicationListActivity.class);
                startActivity(intent);
            }
        });
        serivceSwitch = (Switch)findViewById(R.id.serviceSwitch);
        headSwitch = (Switch)findViewById(R.id.headSwitch);
        if(isServiceRunning(getResources().getString(R.string.serviceName))){
            serivceSwitch.setChecked(true);
            headSwitch.setChecked(false);
        }else if(isServiceRunning(getResources().getString(R.string.headServiceName))){
            headSwitch.setChecked(true);
            serivceSwitch.setChecked(false);
        }
        CompoundButton.OnCheckedChangeListener onClickListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RealmConfiguration realmConfig = new RealmConfiguration
                        .Builder(MainActivity.this)
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(realmConfig);
                Realm realm = Realm.getDefaultInstance();
                ServiceState serviceState;
                if(realm.where(ServiceState.class).count()==0)
                    serviceState = realm.createObject(ServiceState.class);
                else serviceState = realm.where(ServiceState.class).findFirst();
                if (isChecked) {
                    Intent Service = null;
                    if(buttonView.equals(serivceSwitch)) {
                        realm.beginTransaction();
                        serviceState.setShake(true);
                        serviceState.setHead(false);
                        realm.commitTransaction();
                        headSwitch.setChecked(false);
                        Service = new Intent(MainActivity.this, ShakeService.class);
                    }else{
                        if(!Utils.canDrawOverlays(MainActivity.this)) {
                            requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
                            headSwitch.setChecked(false);
                            return;
                        }
                        realm.beginTransaction();
                        serviceState.setShake(false);
                        serviceState.setHead(true);
                        realm.commitTransaction();
                        serivceSwitch.setChecked(false);
                        Service = new Intent(MainActivity.this, ChatHeadService.class);
                    }
                    startService(Service);
                } else {
                    Intent Service = null;
                    if(buttonView.equals(serivceSwitch)) {
                        realm.beginTransaction();
                        serviceState.setShake(false);
                        realm.commitTransaction();
                        Service = new Intent(MainActivity.this, ShakeService.class);
                    }else{
                        realm.beginTransaction();
                        serviceState.setHead(false);
                        realm.commitTransaction();
                        Service = new Intent(MainActivity.this, ChatHeadService.class);
                    }
                    stopService(Service);
                }
            }
        };
        serivceSwitch.setOnCheckedChangeListener(onClickListener);
        headSwitch.setOnCheckedChangeListener(onClickListener);
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("권한 허용이 필요합니다.");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(requestCode);
            }

        }else if(requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG){
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(requestCode);
            }
        }
    }

    public Boolean isServiceRunning(String serviceName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void setNotelist(){
        notelist.removeAllViews();
        List<Note> list =  realm.where(Note.class).findAll();
        for(int i =list.size()-1 ;i>=0;i--){
            final Note note = list.get(i);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.note_item, null);
            String context = list.get(i).getText().replaceAll("\n"," ");
            ((TextView)view.findViewById(R.id.title)).setText(((context.length()>10)?context.substring(10):context));
            ((TextView)view.findViewById(R.id.date)).setText(list.get(i).getDate());
            ((CardView)view.findViewById(R.id.view)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedNote = note;
                    Dialog dialog = new NoteDialog(MainActivity.this,android.R.style.Theme_DeviceDefault_Light_Dialog);
                    dialog.show();
                }
            });
            notelist.addView(view);
        }
    }

    class NoteDialog extends Dialog {

        public NoteDialog(Context context) {
            super(context);
        }
        public NoteDialog(Context context,int style){
            super(context, style);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.note_dialog);

            ((TextView) findViewById(R.id.label)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf"));
            ((TextView)findViewById(R.id.note)).setText(selectedNote.getText());
            ((Button)findViewById(R.id.remove)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RealmConfiguration realmConfig = new RealmConfiguration
                            .Builder(getContext())
                            .deleteRealmIfMigrationNeeded()
                            .build();
                    Realm.setDefaultConfiguration(realmConfig);
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    selectedNote.deleteFromRealm();
                    realm.commitTransaction();
                    setNotelist();
                    dismiss();
                }
            });
            ((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            ((Button)findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RealmConfiguration realmConfig = new RealmConfiguration
                            .Builder(getContext())
                            .deleteRealmIfMigrationNeeded()
                            .build();
                    Realm.setDefaultConfiguration(realmConfig);
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    selectedNote.setText(((TextView) findViewById(R.id.note)).getText() + "");
                    realm.commitTransaction();
                    setNotelist();
                    dismiss();
                }
            });
        }
    }

    public void stoppedHeadService(){
        headSwitch.setChecked(false);
    }

    public static MainActivity getInstance(){
        return instance;
    }


}
