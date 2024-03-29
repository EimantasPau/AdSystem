package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    Stage stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("views/LoginRegistration.fxml"));
        stage = primaryStage;
        primaryStage.setTitle("AdSystem");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(600);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
