package sample;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Alert {

    public static void displayNotification(String notification){
        Stage notificationWindow = new Stage();
        notificationWindow.setTitle("Notification");
        notificationWindow.initModality(Modality.WINDOW_MODAL);
        VBox vbox = new VBox();
        vbox.setStyle("-fx-background-color: #FCF4F2; -fx-padding: 20px;");
        Text message = new Text();
        message.setText(notification);
        message.setWrappingWidth(300);
        message.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        vbox.getChildren().add(message);
        System.out.println(message);
        notificationWindow.setScene(new Scene(vbox,300, 200));
        notificationWindow.setResizable(false);
        notificationWindow.show();
    }
}

