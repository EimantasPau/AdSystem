package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000Comment implements Entry {
    public Integer adID;
    public Integer userID;
    public String message;
    public U1363000Comment() {

    }

    public U1363000Comment(int adID, int userID, String message) {
        this.adID = adID;
        this.userID = userID;
        this.message = message;
    }
}
