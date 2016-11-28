package boxfox.shakenote.components;

import io.realm.RealmObject;

/**
 * Created by Administrator on 2016-08-13.
 */
public class Setting extends RealmObject {
    private int sensitivity = 0;

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }
}
