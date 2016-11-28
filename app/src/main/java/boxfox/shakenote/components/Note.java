package boxfox.shakenote.components;

import io.realm.RealmObject;

/**
 * Created by Administrator on 2016-08-13.
 */
public class Note extends RealmObject {
    private String text,date;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
