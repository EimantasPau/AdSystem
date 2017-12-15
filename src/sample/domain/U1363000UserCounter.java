package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000UserCounter implements Entry{
    public Integer nextUserID;
    public U1363000UserCounter(){

    }
    public U1363000UserCounter(int n){
        nextUserID = n;
    }

    public void incrementID() {
        nextUserID = nextUserID + 1;
    }
}
