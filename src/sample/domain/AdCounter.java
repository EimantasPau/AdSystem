package sample.domain;

import net.jini.core.entry.Entry;

public class AdCounter implements Entry{
    public Integer nextAdID;
    public AdCounter(){

    }
    public AdCounter(int n){
        nextAdID = n;
    }

    public void incrementID() {
        nextAdID = nextAdID + 1;
    }
}