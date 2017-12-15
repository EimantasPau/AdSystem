package sample.domain;

import net.jini.core.entry.Entry;

public class U1363000Bid implements Entry {
    public Integer adID;
    public Integer bidderID;
    public Double bid;
    public Boolean highestBid;
    public U1363000Bid(int adID, int bidderID, double bid, boolean highestBid) {
        this.adID = adID;
        this.bidderID = bidderID;
        this.bid = bid;
        this.highestBid = highestBid;
    }
    public U1363000Bid(){

    }
}
