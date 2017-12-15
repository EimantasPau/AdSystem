package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000AdCounter implements Entry{
    public Integer nextAdID;
    public U1363000AdCounter(){

    }
    public U1363000AdCounter(int n){
        nextAdID = n;
    }

    public void incrementID() {
        nextAdID = nextAdID + 1;
    }
}