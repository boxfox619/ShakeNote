package boxfox.shakenote;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import boxfox.shakenote.components.Item;
import boxfox.shakenote.components.Setting;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class DialogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.activity_dialog);
        LinearLayout applist = (LinearLayout)findViewById(R.id.applist);
        ((TextView)findViewById(R.id.label)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf"));
        ((Button) findViewById(R.id.closeButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        RealmConfiguration realmConfig = new RealmConfiguration
                .Builder(DialogActivity.this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        Realm realm = Realm.getDefaultInstance();
        Setting set = realm.where(Setting.class).findFirst();
        ((TextView)findViewById(R.id.sens)).setText("감도 :"+set.getSensitivity());
        ((SeekBar)findViewById(R.id.sensitivity)).setProgress(set.getSensitivity());
        ((SeekBar)findViewById(R.id.sensitivity)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                RealmConfiguration realmConfig = new RealmConfiguration
                        .Builder(DialogActivity.this)
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
        final RealmResults<Item> list = realm.where(Item.class).findAll();
        Item item = null;
        for(int i =0; i<list.size();i++){
            item = list.get(i);
            View view = LayoutInflater.from(DialogActivity.this).inflate(R.layout.item, null);
            ((ImageView)view.findViewById(R.id.appIcon1)).setImageBitmap(item.getIcon());
            ((TextView)view.findViewById(R.id.appName1)).setText(item.getApplicationName());
            final Item finalItem1 = item;
            ((View)view.findViewById(R.id.card1)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start(finalItem1.getPackageName());
                }
            });
            if(++i<list.size()) {
                item = list.get(i);
                ((TextView) view.findViewById(R.id.appName2)).setText(item.getApplicationName());
                ((ImageView) view.findViewById(R.id.appIcon2)).setImageBitmap(item.getIcon());
                final Item finalItem = item;
                ((CardView)view.findViewById(R.id.card2)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start(finalItem.getPackageName());
                    }
                });
            }else ((CardView)view.findViewById(R.id.card2)).setVisibility(View.INVISIBLE);
            if(++i<list.size()) {
                item = list.get(i);
                ((ImageView) view.findViewById(R.id.appIcon3)).setImageBitmap(item.getIcon());
                ((TextView) view.findViewById(R.id.appName3)).setText(item.getApplicationName());
                final Item finalItem2 = item;
                ((CardView)view.findViewById(R.id.card3)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start(finalItem2.getPackageName());
                    }
                });
            }else ((CardView)view.findViewById(R.id.card3)).setVisibility(View.INVISIBLE);
            applist.addView(view);
        }
    }

    private void start(String str){
        if(str.equals(getResources().getString(R.string.note_package))){
            Intent intent = new Intent(this, NoteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }else {
            Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(str);
            startActivity(intent);
        }
        finish();
    }
}
