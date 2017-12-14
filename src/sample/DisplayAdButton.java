package sample;

import javafx.scene.control.Button;
import sample.domain.Ad;

public class DisplayAdButton extends Button {
    public Ad ad;
    public DisplayAdButton(Ad ad){
        this.ad = ad;
    }
}
