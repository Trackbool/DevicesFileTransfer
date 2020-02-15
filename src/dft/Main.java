package dft;

import dft.view.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ui/main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setOnCloseListener(primaryStage);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Device Files Transfer");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
