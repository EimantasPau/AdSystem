package sample.domain;

import net.jini.core.entry.Entry;

public class Comment implements Entry {
    public Integer adID;
    public Integer userID;
    public String message;
    public Comment() {

    }

    public Comment(int adID, int userID, String message) {
        this.adID = adID;
        this.userID = userID;
        this.message = message;
    }
}
