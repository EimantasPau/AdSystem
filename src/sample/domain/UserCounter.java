package sample.domain;

import net.jini.core.entry.Entry;

public class UserCounter implements Entry{
    public Integer nextUserID;
    public UserCounter(){

    }
    public UserCounter(int n){
        nextUserID = n;
    }

    public void incrementID() {
        nextUserID = nextUserID + 1;
    }
}
