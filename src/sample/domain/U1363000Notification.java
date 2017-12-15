package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000Notification implements Entry {
    public Integer userID;
    public String message;
    public U1363000Notification(){

    }
    public U1363000Notification(int userID, String message) {
        this.userID = userID;
        this.message = message;
    }
}
