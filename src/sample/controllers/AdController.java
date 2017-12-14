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
    BaseModel model;
    Ad ad;

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


    public AdController(Ad ad) {
        model = new BaseModel();
        this.ad = ad;
    }

    public void initialize(URL location, ResourceBundle resources){
        adTitle.setText(ad.name);
        adDescription.setText(ad.description);
        if (ad.type.equals("Auction")) {
            Bid template = new Bid();
            template.adID = ad.ID;
            template.highestBid = true;
            Bid highestBid = (Bid)model.readEntry(template);
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

    public void setAd(Ad ad){
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
        Comment template = new Comment();
        template.adID = ad.ID;
        MatchSet comments = model.readEntries(template);
        Comment comment;

        //check if there are any comments.
        if((comment = (Comment)comments.next()) == null){
            Label noComments = new Label("There are no comments for this ad");
            System.out.println("adding label");
            commentsContainer.getChildren().add(noComments);
        } else {
            //necessary to do this because if there is only one comment, the loop below will be null since next() has already been called on the collection.
            insertComment(comment);
        }
        //display all comments for the ad
        while ((comment = (Comment)comments.next()) != null) {
            System.out.println("Comment loop: " + comment.message);
            insertComment(comment);
        }

    }

    public void insertComment(Comment comment) {
        //get the comment author
        User commentAuthor = new User();
        commentAuthor.ID = comment.userID;
        User result = (User)model.readEntry(commentAuthor);

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
        Comment newComment = new Comment(adID, authorID, content);
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
        Bid template = new Bid();
        template.adID = ad.ID;
        template.highestBid = true;
        //create new bid object.
        Bid bidTemplate = new Bid(ad.ID, ActiveUser.getInstance().getID(), bid, true);

        Bid currentBid;
        //check if a bid has already been placed on this ad, if not we write the new bid object.
        if((currentBid = (Bid)model.readEntry(template)) == null){
            model.writeEntry(bidTemplate);
            Notification bidNotification = new Notification(ad.userID, "A new bid has been placed on your ad " +"'" + ad.name+ "'");
            model.writeEntry(bidNotification);
            Stage stage = (Stage)bidButton.getScene().getWindow();
            stage.close();
        } else {
            //if a bid already exists, we check if the new bid is higher than the old one.
            if(bidTemplate.bid > currentBid.bid){
                //take out the previous bid and indicate that it's no longer the highest.
                currentBid = (Bid)model.takeEntry(template);
                currentBid.highestBid = false;
                model.writeEntry(currentBid);
                model.writeEntry(bidTemplate);
                //create a notification for the ad poster.
                Notification bidNotification = new Notification(ad.userID, "A new bid has been placed on your ad " +"'" + ad.name+ "'");
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
        Notification sellerNotification = new Notification(ad.userID, "You have sold " + ad.name + " to " + ActiveUser.getInstance().getUsername() + " for £" + ad.price);
        model.writeEntry(sellerNotification);
        //create a notification for the buyer
        Notification buyerNotification = new Notification(ActiveUser.getInstance().getID(), "You have bought " + ad.name + " for £" + ad.price);
        model.writeEntry(buyerNotification);

        //remove ad with the current ad ID
       deleteAd(event);
    }
    //delete from space
    public void deleteAd(ActionEvent event) {
        Ad template = new Ad();
        template.ID = ad.ID;
        model.takeEntry(template);
        //close the window
        Button source = (Button)event.getSource();
        Stage stage = (Stage)source.getScene().getWindow();
        stage.close();
    }

    public void addCommentNotification(Comment comment, Ad ad) throws RemoteException, TransactionException {
        Notification commentNotification = new Notification(ad.userID, "A new comment has been posted on your add" + "'" + ad.name + "'");
        model.writeEntry(commentNotification);
    }
}
