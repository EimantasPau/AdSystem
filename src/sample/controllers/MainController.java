package sample.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.AvailabilityEvent;
import net.jini.space.MatchSet;
import sample.ActiveUser;
import sample.Alert;
import sample.DisplayAdButton;
import sample.domain.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainController {
    @FXML
    Label viewAds;
    @FXML
    VBox adBox;
    @FXML
    Button postAdButton;
    @FXML
    Label welcomeLabel;
    @FXML
    VBox userAdBox;
    @FXML
    VBox notificationsContainer;
    public U1363000BaseModel model;
    public U1363000Ad newAd;
    public MainController(){
        this.model = new U1363000BaseModel();
        try {
            UnicastRemoteObject.exportObject(adAddedListener, 0);
            UnicastRemoteObject.exportObject(notificationAddedListener, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void initialize() throws TransactionException, UnusableEntryException, RemoteException {
        welcomeLabel.setText("Welcome, " + ActiveUser.getInstance().getUsername() + "(" + ActiveUser.getInstance().getID() + ")");
        updateAdList();
        //we want any new ads added.
        List<Entry> adAddedTemplateList = new ArrayList<>();
        adAddedTemplateList.add(new U1363000Ad());


        List<Entry> commentNotificationTemplateList = new ArrayList<>();
        //interested in any notification that have the userID
        U1363000Notification notificationTemplate = new U1363000Notification();
        notificationTemplate.userID = ActiveUser.getInstance().getID();
        commentNotificationTemplateList.add(notificationTemplate);

        //register listeners
        model.registerForAvailability(commentNotificationTemplateList, notificationAddedListener);
         model.registerForAvailability(adAddedTemplateList, adAddedListener);

         checkForEndedAuctions();
    }

    public void updateAdList() throws RemoteException, TransactionException, UnusableEntryException {
        //clear currently rendered ads
        adBox.getChildren().clear();
        adBox.setSpacing(10);
        U1363000Ad adTemplate = new U1363000Ad();
        //get the collection and add it to the ad container
        MatchSet ads = model.readEntries(adTemplate);
        U1363000Ad ad;
        while ((ad = (U1363000Ad)ads.next()) != null) {
           addAd(ad, adBox);
        }
    }

    public void updateUserAds() throws RemoteException, TransactionException, UnusableEntryException {
        //clear currently rendered ads
        userAdBox.getChildren().clear();
        userAdBox.setSpacing(10);
        U1363000Ad adTemplate = new U1363000Ad();
        //get all ads that belong to the logged in user and add them to the user ad container
        adTemplate.userID = ActiveUser.getInstance().getID();
        MatchSet ads = model.readEntries(adTemplate);
        U1363000Ad ad;
        while ((ad = (U1363000Ad)ads.next()) != null) {
            addAd(ad, userAdBox);
        }
    }

    public void updateNotifications() throws RemoteException, UnusableEntryException, TransactionException {
        notificationsContainer.getChildren().clear();
        notificationsContainer.setSpacing(10);
        U1363000Notification template = new U1363000Notification();
        template.userID = ActiveUser.getInstance().getID();
        MatchSet notifications = model.readEntries(template);
        U1363000Notification notification;
        while ((notification = (U1363000Notification)notifications.next()) != null) {
            Text notificationText = new Text(notification.message);
            Separator separator = new Separator();
            notificationsContainer.getChildren().addAll(notificationText, separator);
        }
    }

    public void addAd(U1363000Ad ad, VBox container){
        //create fields for the ad and set styles
        Label label = new Label(ad.name);
        label.getStyleClass().add("ad-title");
        Text text = new Text(ad.description);
        text.getStyleClass().add("ad-content");
        //create a custom button that can hold an ad object, this will be useful when handling the event
        DisplayAdButton openAdButton = new DisplayAdButton(ad);
        openAdButton.setText("Open ad");
        openAdButton.getStyleClass().add("ad-button");
        openAdButton.setOnAction(displayAdEvent);
        container.getChildren().addAll(label,text, openAdButton);
    }
    public void displayAdForm() throws IOException {
        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Post an ad");
        window.setMinWidth(500);
        window.setMinHeight(500);

        Parent root = FXMLLoader.load(getClass().getResource("../views/PostAd.fxml"));
        Scene scene = new Scene(root,600, 600);
        window.setScene(scene);
        window.show();

    }
    public void checkForEndedAuctions() throws RemoteException, TransactionException, UnusableEntryException {
        U1363000Ad template = new U1363000Ad();
        template.type = "Auction";
        MatchSet result = model.readEntries(template);
        U1363000Ad ad;
        while ((ad = (U1363000Ad)result.next()) != null) {
            if(ad.closingTime.before(new Date())){
                System.out.println("auction ended");
                //read the highest bid for an ad if any
                U1363000Bid bidTemplate = new U1363000Bid();
                bidTemplate.highestBid = true;
                bidTemplate.adID = ad.ID;

                U1363000Bid highestBid;
                //send notifications if any bids were made
                if((highestBid = (U1363000Bid) model.readEntry(bidTemplate)) != null) {
                    System.out.println("creating notifications");
                    //create notification for the buyer
                    U1363000Notification buyerNotification = new U1363000Notification(highestBid.bidderID, "You have won the auction for item " + "'" + ad.name + "' at the price of:" + highestBid.bid);
                    model.writeEntry(buyerNotification);
                    //create notification for seller
                    U1363000User buyerTemplate = new U1363000User();
                    buyerTemplate.ID = ad.userID;
                    U1363000User buyer = (U1363000User) model.readEntry(buyerTemplate);
                    U1363000Notification sellerNotification = new U1363000Notification(ad.userID, "Auction for " + "'" + ad.name + "' has ended with the highest bid of " + highestBid.bid + " from " + buyer.username);
                    model.writeEntry(sellerNotification);
                } else {
                    U1363000Notification noBidsNotification = new U1363000Notification(ad.userID, "Your auction for" + "'" + ad.name + "' has ended with no bids");
                    model.writeEntry(noBidsNotification);
                    System.out.println("happening");
                }
                //remove ad from space
                model.takeEntry(ad);
            }
        }
    }

    //listener declarations
    EventHandler<ActionEvent> displayAdEvent = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
            Stage window = new Stage();
            //we get the button from the action event alongside the ad object.
            DisplayAdButton button = (DisplayAdButton) actionEvent.getSource();
            U1363000Ad ad = button.ad;
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle(ad.name);
            window.setMinWidth(500);
            window.setMinHeight(500);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/Ad.fxml"));
            Parent root = null;
            //since we passed the ad to the button we can pass it to the AdController
            AdController controller = new AdController(ad);
            loader.setController(controller);

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            root = loader.getRoot();
            Scene scene = new Scene(root, 800, 600);
            window.setScene(scene);
            window.show();
        }
    };

    RemoteEventListener adAddedListener = new RemoteEventListener() {
        @Override
        public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
            // Cast the RemoteEvent to an AvailabilityEvent, as this adds extra functionality
            AvailabilityEvent event = (AvailabilityEvent) theEvent;
            try {
                // AvailabilityEvent provides an extra method to get
                //     the entry that fired the notification
                final U1363000Ad ad = (U1363000Ad) event.getEntry();

                //since we are on a different thread from the JavaFX application thread, we'll update the UI when we finish on this one
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        addAd(ad, adBox);
                        if(ad.userID != ActiveUser.getInstance().getID()){
                            Alert.displayNotification("A new ad has been posted!");
                        }

                    }
                });
            } catch (UnusableEntryException e) {
                System.err.println(e.getMessage());
            }
        }
    };

    RemoteEventListener notificationAddedListener = new RemoteEventListener() {
        @Override
        public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
            // Cast the RemoteEvent to an AvailabilityEvent, as this adds extra functionality
            AvailabilityEvent event = (AvailabilityEvent) theEvent;

            try {
                // AvailabilityEvent provides an extra method to get
                //     the entry that fired the notification
                final U1363000Notification notification = (U1363000Notification) event.getEntry();
                //since we are on a different thread from the JavaFX application thread, we'll update the UI when we finish on this one
                Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        //pass the message to be displayed.
                        Alert.displayNotification(notification.message);
                    }
                });

            } catch (UnusableEntryException e) {
                System.err.println(e.getMessage());
            }
        }
    };

}
