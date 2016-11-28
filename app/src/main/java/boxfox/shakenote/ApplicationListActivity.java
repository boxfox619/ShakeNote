package boxfox.shakenote;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;

import boxfox.shakenote.components.Item;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class ApplicationListActivity extends AppCompatActivity {
    private ProgressDialog dialog;
    private LinearLayout applist;
    private Realm realm;
    private boolean adCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_list);
        applist = (LinearLayout)findViewById(R.id.applist);
        setRealm();
        new Thread(new Runnable() {
            @Override
            public void run() {
                applicationList();
            }
        }).start();
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
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.help,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("도움말")
                .setMessage("스마트폰을 흔들었을때 선택한 애플리케이션이 실행됩니다. 여러개의 애플리케이션이 선택된 경우 목록이 나타납니다.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        return false;
    }

    public void setRealm(){
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(ApplicationListActivity.this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();
    }

    public void applicationList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = ProgressDialog.show(ApplicationListActivity.this, "",
                        "잠시 기다려주세요", true);
            }
        });
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = (ApplicationListActivity.this).getPackageManager();
        List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View nview = LayoutInflater.from(ApplicationListActivity.this).inflate(R.layout.activity_application_item, null);
                ((TextView) nview.findViewById(R.id.appName)).setText(getResources().getString(R.string.quick_note));
                ((ImageView) nview.findViewById(R.id.icon)).setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
                if (realm.where(Item.class).equalTo("packageName", getResources().getString(R.string.note_package)).count() != 0)
                    ((CheckBox) nview.findViewById(R.id.checkBox)).setChecked(true);
                nview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((CheckBox) nview.findViewById(R.id.checkBox)).setChecked(!((CheckBox) nview.findViewById(R.id.checkBox)).isChecked());
                    }
                });
                ((CheckBox) nview.findViewById(R.id.checkBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (((CheckBox) nview.findViewById(R.id.checkBox)).isChecked()) {
                            RealmConfiguration realmConfig = new RealmConfiguration
                                    .Builder(ApplicationListActivity.this)
                                    .deleteRealmIfMigrationNeeded()
                                    .build();
                            Realm.setDefaultConfiguration(realmConfig);
                            Realm realm = Realm.getDefaultInstance();
                            if (realm.where(Item.class).equalTo("packageName", getResources().getString(R.string.note_package)).count() == 0) {
                                realm.beginTransaction();
                                Item item = realm.createObject(Item.class);
                                item.setApplicationName(getResources().getString(R.string.app_name));
                                item.setIcon(getResources().getDrawable(R.mipmap.ic_launcher));
                                item.setPackageName(getResources().getString(R.string.note_package));
                                realm.commitTransaction();
                            }
                        } else {

                            RealmConfiguration realmConfig = new RealmConfiguration
                                    .Builder(ApplicationListActivity.this)
                                    .deleteRealmIfMigrationNeeded()
                                    .build();
                            Realm.setDefaultConfiguration(realmConfig);
                            Realm realm = Realm.getDefaultInstance();
                            try {
                                if (realm.where(Item.class).equalTo("packageName", getResources().getString(R.string.note_package)).count() == 1) {
                                    realm.beginTransaction();
                                    realm.where(Item.class).equalTo("packageName", getResources().getString(R.string.note_package)).findFirst().deleteFromRealm();
                                    realm.commitTransaction();
                                }
                            } catch (NullPointerException e) {

                            }
                        }
                    }
                });
                applist.addView(nview);
            }
        });

        for (int i =0;i< installedApps.size();i++) {
            ResolveInfo ai= installedApps.get(i);
            final String packageName = ai.activityInfo.packageName;
            final String applicationName =ai.loadLabel(getPackageManager())+"";
            final Drawable appIcon = ai.loadIcon(getPackageManager());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final View view = LayoutInflater.from(ApplicationListActivity.this).inflate(R.layout.activity_application_item, null);
                    ((TextView) view.findViewById(R.id.appName)).setText(applicationName);
                    ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(appIcon);
                    if (realm.where(Item.class).equalTo("packageName", packageName).count() != 0)
                        ((CheckBox) view.findViewById(R.id.checkBox)).setChecked(true);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((CheckBox) view.findViewById(R.id.checkBox)).setChecked(!((CheckBox) view.findViewById(R.id.checkBox)).isChecked());
                        }
                    });
                    ((CheckBox) view.findViewById(R.id.checkBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (((CheckBox) view.findViewById(R.id.checkBox)).isChecked()) {
                                RealmConfiguration realmConfig = new RealmConfiguration
                                        .Builder(ApplicationListActivity.this)
                                        .deleteRealmIfMigrationNeeded()
                                        .build();
                                Realm.setDefaultConfiguration(realmConfig);
                                Realm realm = Realm.getDefaultInstance();
                                if (realm.where(Item.class).equalTo("packageName", packageName).count() == 0) {
                                    realm.beginTransaction();
                                    Item item = realm.createObject(Item.class);
                                    item.setApplicationName(applicationName);
                                    item.setIcon(appIcon);
                                    item.setPackageName(packageName);
                                    realm.commitTransaction();
                                }
                            } else {

                                RealmConfiguration realmConfig = new RealmConfiguration
                                        .Builder(ApplicationListActivity.this)
                                        .deleteRealmIfMigrationNeeded()
                                        .build();
                                Realm.setDefaultConfiguration(realmConfig);
                                Realm realm = Realm.getDefaultInstance();
                                try {
                                    if (realm.where(Item.class).equalTo("packageName", packageName).count() == 1) {
                                        realm.beginTransaction();
                                        realm.where(Item.class).equalTo("packageName", packageName).findFirst().deleteFromRealm();
                                        realm.commitTransaction();
                                    }
                                } catch (NullPointerException e) {

                                }
                            }
                        }
                    });
                    applist.addView(view);
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        });
    }

}
