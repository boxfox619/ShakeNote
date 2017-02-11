package boxfox.shakenote.components;

import io.realm.RealmObject;

/**
 * Created by boxfox on 2016-08-25.
 */
public class ServiceState extends RealmObject {
    private boolean shake = true, head;

    public boolean isShake() {
        return shake;
    }

    public void setShake(boolean run) {
        this.shake = run;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }
}
