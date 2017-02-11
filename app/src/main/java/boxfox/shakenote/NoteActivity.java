package boxfox.shakenote;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import boxfox.shakenote.components.Note;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class NoteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.activity_note);
        String str = getIntent().getStringExtra("context");
        ((TextView)findViewById(R.id.label)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/GoodDog.otf"));
        ((TextView)findViewById(R.id.note)).setText(str);
        ((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((Button)findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmConfiguration realmConfig = new RealmConfiguration
                        .Builder(NoteActivity.this)
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(realmConfig);
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                Note note = realm.createObject(Note.class);
                note.setText(((TextView) findViewById(R.id.note)).getText() + "");
                Date cDate = new Date();
                String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                note.setDate(fDate);
                realm.commitTransaction();
                finish();
            }
        });
    }
}
