package sample.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.MatchSet;
import sample.ActiveUser;
import sample.domain.*;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

public class AdController implements Initializable {
    U1363000BaseModel model;
    U1363000Ad ad;

    @FXML
    Label adTitle;
    @FXML
    Label adDescription;
    @FXML
    Label adPrice;
    @FXML
    VBox commentsContainer;
    @FXML
    TextArea commentTextarea;
    @FXML
    Button postCommentButton;
    @FXML
    HBox actionsBox;
    @FXML
    Button buyAdButton;
    @FXML
    Button bidButton;
    @FXML
    TextField bidSize;
    @FXML
    VBox adDetailsBox;


    public AdController(U1363000Ad ad) {
        model = new U1363000BaseModel();
        this.ad = ad;
    }

    public void initialize(URL location, ResourceBundle resources){
        adTitle.setText(ad.name);
        adDescription.setText(ad.description);
        if (ad.type.equals("Auction")) {
            U1363000Bid template = new U1363000Bid();
            template.adID = ad.ID;
            template.highestBid = true;
            U1363000Bid highestBid = (U1363000Bid)model.readEntry(template);
            if(highestBid != null){
                adPrice.setText("Highest bid: " + highestBid.bid.toString());
            } else {
                adPrice.setText("There are no bids yet.");
            }
            //display ending date
            Text endingDate = new Text("This auction ends: " + ad.closingTime.toString());
            endingDate.setText("This auction ends: " + ad.closingTime.toString());
            adDetailsBox.getChildren().add(endingDate);

        } else {
            adPrice.setText(ad.price.toString());
        }

        initializeActionButtons();
        try {
            initializeComments();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (UnusableEntryException e) {
            e.printStackTrace();
        }
    }

    public void setAd(U1363000Ad ad){
        this.ad = ad;
    }

    public void initializeActionButtons(){
        //check if the ad belongs to the logged in user and display appropriate buttons.
        if(ad.userID == ActiveUser.getInstance().getID()){
            final Button deleteAdButton = new Button("Delete ad");
            deleteAdButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    deleteAd(actionEvent);
                    System.out.println("deleted ad");
                    Stage stage = (Stage)deleteAdButton.getScene().getWindow();
                    stage.close();
                }
            });
            deleteAdButton.getStyleClass().add("ad-button");
            actionsBox.getChildren().add(deleteAdButton);
        } else {
            //check if fixed price add or auction
            if(ad.type.equals("Sell")){
                buyAdButton = new Button("Buy");
                buyAdButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            removeAd(actionEvent);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (TransactionException e) {
                            e.printStackTrace();
                        }
                    }
                });
                buyAdButton.getStyleClass().add("ad-button");
                actionsBox.getChildren().add(buyAdButton);
            } else {
                bidButton = new Button("Place a bid");
                bidButton.getStyleClass().add("ad-button");
                bidButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            placeBid();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (TransactionException e) {
                            e.printStackTrace();
                        }
                    }
                });
                bidSize = new TextField();
                bidSize.getStyleClass().add("text-field");
                actionsBox.getChildren().addAll(bidSize, bidButton);
            }
        }
    }

    public void initializeComments() throws RemoteException, TransactionException, UnusableEntryException {
        //get the comments that belong to current ad
        U1363000Comment template = new U1363000Comment();
        template.adID = ad.ID;
        MatchSet comments = model.readEntries(template);
        U1363000Comment comment;

        //check if there are any comments.
        if((comment = (U1363000Comment)comments.next()) == null){
            Label noComments = new Label("There are no comments for this ad");
            System.out.println("adding label");
            commentsContainer.getChildren().add(noComments);
        } else {
            //necessary to do this because if there is only one comment, the loop below will be null since next() has already been called on the collection.
            insertComment(comment);
        }
        //display all comments for the ad
        while ((comment = (U1363000Comment)comments.next()) != null) {
            System.out.println("Comment loop: " + comment.message);
            insertComment(comment);
        }

    }

    public void insertComment(U1363000Comment comment) {
        //get the comment author
        U1363000User commentAuthor = new U1363000User();
        commentAuthor.ID = comment.userID;
        U1363000User result = (U1363000User)model.readEntry(commentAuthor);

        //prepare text field
        Text message = new Text(result.username + ":" + comment.message);
        message.getStyleClass().add("comment");
        message.setWrappingWidth(470);
        message.setStyle("-fx-padding: 10");
        Separator separator = new Separator();
        //add to comments container
        commentsContainer.getChildren().addAll(message, separator);
    }

    public void addComment() throws RemoteException, TransactionException {
        //get user input
        String content = commentTextarea.getText();
        //get current user
        int authorID = ActiveUser.getInstance().getID();
        int adID = ad.ID;
        //create and write the object
        U1363000Comment newComment = new U1363000Comment(adID, authorID, content);
        model.writeEntry(newComment);
        Stage stage = (Stage)postCommentButton.getScene().getWindow();
        stage.close();
        //create a notification for a newly added comment.
        addCommentNotification(newComment, ad);
    }

    public void placeBid() throws RemoteException, TransactionException {
        //get user input.
        Double bid = Double.parseDouble(bidSize.getText());
        //prepare template for highest bid for this ad.
        U1363000Bid template = new U1363000Bid();
        template.adID = ad.ID;
        template.highestBid = true;
        //create new bid object.
        U1363000Bid bidTemplate = new U1363000Bid(ad.ID, ActiveUser.getInstance().getID(), bid, true);

        U1363000Bid currentBid;
        //check if a bid has already been placed on this ad, if not we write the new bid object.
        if((currentBid = (U1363000Bid)model.readEntry(template)) == null){
            model.writeEntry(bidTemplate);
            U1363000Notification bidNotification = new U1363000Notification(ad.userID, "A new bid has been placed on your ad " +"'" + ad.name+ "'");
            model.writeEntry(bidNotification);
            Stage stage = (Stage)bidButton.getScene().getWindow();
            stage.close();
        } else {
            //if a bid already exists, we check if the new bid is higher than the old one.
            if(bidTemplate.bid > currentBid.bid){
                //take out the previous bid and indicate that it's no longer the highest.
                currentBid = (U1363000Bid)model.takeEntry(template);
                currentBid.highestBid = false;
                model.writeEntry(currentBid);
                model.writeEntry(bidTemplate);
                //create a notification for the ad poster.
                U1363000Notification bidNotification = new U1363000Notification(ad.userID, "A new bid has been placed on your ad " +"'" + ad.name+ "'");
                model.writeEntry(bidNotification);
                Stage stage = (Stage)bidButton.getScene().getWindow();
                stage.close();
            } else {
                System.out.println("new bid must be higher than the old one.");
            }
        }


    }

    //when item is bought
    public void removeAd(ActionEvent event) throws RemoteException, TransactionException {
        //create a notification for the seller
        U1363000Notification sellerNotification = new U1363000Notification(ad.userID, "You have sold " + ad.name + " to " + ActiveUser.getInstance().getUsername() + " for £" + ad.price);
        model.writeEntry(sellerNotification);
        //create a notification for the buyer
        U1363000Notification buyerNotification = new U1363000Notification(ActiveUser.getInstance().getID(), "You have bought " + ad.name + " for £" + ad.price);
        model.writeEntry(buyerNotification);

        //remove ad with the current ad ID
       deleteAd(event);
    }
    //delete from space
    public void deleteAd(ActionEvent event) {
        U1363000Ad template = new U1363000Ad();
        template.ID = ad.ID;
        model.takeEntry(template);
        //close the window
        Button source = (Button)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }

    public void addCommentNotification(U1363000Comment comment, U1363000Ad ad) throws RemoteException, TransactionException {
        U1363000Notification commentNotification = new U1363000Notification(ad.userID, "A new comment has been posted on your ad " + "'" + ad.name + "'");
        model.writeEntry(commentNotification);
    }
}
