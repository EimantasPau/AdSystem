package sample.domain;

import net.jini.core.entry.Entry;

import java.util.Date;

public class U1363000Ad implements Entry {
    public Integer ID;
    public Integer userID;
    public String name;
    public String description;
    public Double price;
    public String type;
    public Date closingTime = null;

    public U1363000Ad(int ID, int userID, String name, String description, double price, String type){
        this.ID = ID;
        this.userID = userID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
    }
    public U1363000Ad(){

    }
}
