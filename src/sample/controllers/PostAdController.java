package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import net.jini.core.transaction.TransactionException;
import sample.ActiveUser;
import sample.Alert;
import sample.domain.U1363000AdCounter;
import sample.domain.U1363000BaseModel;
import sample.domain.U1363000Ad;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

public class PostAdController {

    public U1363000BaseModel model;
    @FXML
    TextField itemName;
    @FXML
    TextArea itemDescription;
    @FXML
    TextField itemPrice;
    @FXML
    RadioButton sell;
    @FXML
    RadioButton auction;
    @FXML
    Button postAdButton;
    @FXML
    ChoiceBox<String> adType;
    @FXML
    ChoiceBox<String> auctionLength;


    public PostAdController(){

        model = new U1363000BaseModel();
        //check if a counter is present in the space
        if(model.readEntry(new U1363000AdCounter()) == null){
            try {
                model.writeEntry(new U1363000AdCounter(0));
                System.out.println("writing counter");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (TransactionException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewAd() throws RemoteException, TransactionException {
        //get user input
        String name = itemName.getText();
        String description = itemDescription.getText();
        String type = adType.getValue();
        String price = itemPrice.getText();
        String length = auctionLength.getValue();
        //validate input
        if(name.isEmpty() || description.isEmpty()) {
            Alert.displayNotification("Please enter the name and the description.");
            return;
        }

        if(type.equals("Sell") && price.isEmpty()){
            Alert.displayNotification("Price is required for this ad type.");
            return;
        }

        //create ad object
        U1363000Ad newAd = new U1363000Ad();
        if(type.equals("Auction")){
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            try {
                cal.add(Calendar.MINUTE, Integer.parseInt(length));
                newAd.closingTime = cal.getTime();
            } catch (Exception e) {
                Alert.displayNotification("Invalid auction length.");
                return;
            }
        } else {
            try{
                newAd.price = Double.parseDouble(price);
            }
            catch (NumberFormatException e){
                Alert.displayNotification("Please enter a numeric value for the price.");
                return;
            }

        }
        newAd.name = name;
        newAd.description = description;
        //get the counter to get the next ad ID
        U1363000AdCounter counter = (U1363000AdCounter)model.takeEntry(new U1363000AdCounter());
        counter.incrementID();
        newAd.ID = counter.nextAdID;
        newAd.userID = ActiveUser.getInstance().getID();
        newAd.type = type;

        //write new ad the the counter back to the space
        model.writeEntry(newAd);
        model.writeEntry(counter);
        //close window
        Stage stage = (Stage)postAdButton.getScene().getWindow();
        stage.close();
    }
}
