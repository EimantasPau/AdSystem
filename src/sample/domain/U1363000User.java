package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000User implements Entry {
    public Integer ID;
    public String username;
    public String password;

    public U1363000User() {

    }

    public U1363000User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public U1363000User(int ID, String username, String password){
        this.ID = ID;
        this.username = username;
        this.password = password;
    }
}
