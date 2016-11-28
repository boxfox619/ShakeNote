package boxfox.shakenote.components;

import io.realm.RealmObject;

/**
 * Created by boxfox on 2016-08-25.
 */
public class ServiceState extends RealmObject {
    private boolean run = true;

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
