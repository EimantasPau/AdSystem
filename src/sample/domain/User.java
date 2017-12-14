package sample.domain;

import net.jini.core.entry.Entry;

public class User implements Entry {
    public Integer ID;
    public String username;
    public String password;

    public User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(int ID, String username, String password){
        this.ID = ID;
        this.username = username;
        this.password = password;
    }
}
