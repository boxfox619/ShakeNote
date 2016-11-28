package boxfox.shakenote.components;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

import io.realm.RealmObject;

/**
 * Created by Administrator on 2016-08-13.
 */
public class Item extends RealmObject {
    private String packageName,applicationName;
    private byte[] icon;

    public String getPackageName() {
         return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Bitmap getIcon() {
        Bitmap bitmap = BitmapFactory.decodeByteArray(icon, 0, icon.length);
        return bitmap ;
    }

    public void setIcon(Drawable icon) {
        Bitmap bitmap = ((BitmapDrawable)icon).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,0,stream);
        byte[] bitmapdata = stream.toByteArray();
        this.icon = bitmapdata;
    }
}
