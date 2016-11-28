package boxfox.shakenote;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
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
import boxfox.shakenote.service.ShakeService;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity {
    private LinearLayout notelist;
    private Realm realm;
    private Note selectedNote;
    private InterstitialAd interstitialAd;
    private boolean adCheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        ((ImageButton)findViewById(R.id.imageButton)).getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        ((TextView)findViewById(R.id.label)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf"));
        notelist = (LinearLayout) findViewById(R.id.applist);
        setRealm();
        initialize();
        setNotelist();
        setFullAd();
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
        displayAD();
    }

    public void displayAD(){
            if(interstitialAd.isLoaded()) { //광고가 로드 되었을 시
                interstitialAd.show(); //보여준다
            }
    }

    private void setFullAd(){
        interstitialAd = new InterstitialAd(this); //새 광고를 만듭니다.
        interstitialAd.setAdUnitId(getResources().getString(R.string.adID)); //이전에 String에 저장해 두었던 광고 ID를 전면 광고에 설정합니다.
        AdRequest adRequest1 = new AdRequest.Builder().build(); //새 광고요청
        interstitialAd.loadAd(adRequest1); //요청한 광고를 load 합니다.
        interstitialAd.setAdListener(new AdListener() { //전면 광고의 상태를 확인하는 리스너 등록

            @Override
            public void onAdClosed() { //전면 광고가 열린 뒤에 닫혔을 때
            }
        });
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

        Switch serivceSwitch = (Switch)findViewById(R.id.serviceSwitch);
        if(isServiceRunning(getResources().getString(R.string.serviceName)))serivceSwitch.setChecked(true);
        serivceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RealmConfiguration realmConfig = new RealmConfiguration
                        .Builder(MainActivity.this)
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(realmConfig);
                Realm realm = Realm.getDefaultInstance();
                ServiceState serviceState;
                realm.beginTransaction();
                if(realm.where(ServiceState.class).count()==0)
                serviceState = realm.createObject(ServiceState.class);
                else serviceState = realm.where(ServiceState.class).findFirst();
                if (isChecked) {
                    serviceState.setRun(true);
                    Intent Service = new Intent(MainActivity.this, ShakeService.class);
                    startService(Service);
                    Toast.makeText(MainActivity.this,"서비스가 시작되었습니다. 화면을 흔들어보세요!",Toast.LENGTH_SHORT).show();
                } else {
                    serviceState.setRun(false);
                    Intent Service = new Intent(MainActivity.this, ShakeService.class);
                    stopService(Service);
                    Toast.makeText(MainActivity.this,"서비스가 종료되었습니다.",Toast.LENGTH_SHORT).show();
                }
                realm.commitTransaction();
            }
        });
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

}
