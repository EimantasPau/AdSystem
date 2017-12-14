package sample.domain;

import net.jini.core.entry.Entry;

public class Notification implements Entry {
    public Integer userID;
    public String message;
    public Notification(){

    }
    public Notification(int userID, String message) {
        this.userID = userID;
        this.message = message;
    }
}
