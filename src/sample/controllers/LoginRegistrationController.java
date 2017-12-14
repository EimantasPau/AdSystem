package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.jini.core.transaction.TransactionException;
import sample.ActiveUser;
import sample.Alert;
import sample.domain.BaseModel;
import sample.domain.User;
import sample.domain.UserCounter;

import java.io.IOException;
import java.rmi.RemoteException;

public class LoginRegistrationController {
    public BaseModel model;

    @FXML
    Button loginButton;
    @FXML
    Button registerButton;
    @FXML
    Stage stage;
    @FXML
    TextField registrationUsername;
    @FXML
    TextField registrationPassword;
    @FXML
    TextField registrationPasswordConfirm;
    @FXML
    Button increment;
    @FXML
    TextField loginUsername;
    @FXML
    PasswordField loginPassword;

    public LoginRegistrationController(){
        model = new BaseModel();
        //check if a counter is already in the space
        if(model.readEntry(new UserCounter()) == null){
            try {
                model.writeEntry(new UserCounter(0));
                System.out.println("writing counter");
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (TransactionException e) {
                e.printStackTrace();
            }
        }
    }

    public void attemptLogin() throws IOException {
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        if(username.isEmpty() || password.isEmpty()){
            Alert.displayNotification("Please enter your credentials.");
            return;
        }
        User user = new User(username, password);
        User result = (User)model.readEntry(user);
        if(result == null){
           Alert.displayNotification("Incorrect username or password.");
        } else {
            ActiveUser.getInstance().setID(result.ID);
            ActiveUser.getInstance().setUsername(result.username);
            displayMainView();
        }
    }

    public void registerUser() throws RemoteException, TransactionException {
        //get user input
        String username = registrationUsername.getText();
        String password = registrationPassword.getText();
        String confirmPassword = registrationPasswordConfirm.getText();
        //validate the input
        if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Alert.displayNotification("Please enter all of the field to register.");
            return;
        }
        if(!password.equals(confirmPassword)) {
            Alert.displayNotification("Passwords do not match.");
            return;
        }

        //prepare a template with the intended username
        User existingUser = new User();
        existingUser.username = username;

        //check if username is available
        if((model.readEntry(existingUser)) == null) {
            UserCounter counter = (UserCounter)model.takeEntry(new UserCounter());
            counter.incrementID();
            int userID = counter.nextUserID;
            User user = new User(userID, username, password);
            model.writeEntry(user);
            model.writeEntry(counter);
            Alert.displayNotification("User successfully registered.");
        } else {
            Alert.displayNotification("User already registered.");
        }

    }

    public void displayMainView() throws IOException{
        stage=(Stage)loginButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("../views/Main.fxml"));
        Scene scene = new Scene(root,800, 600);
        stage.setScene(scene);
        stage.show();
    }
}
