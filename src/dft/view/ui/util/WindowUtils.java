package dft.view.ui.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class WindowUtils {
    public static File browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = new Stage();
        return fileChooser.showOpenDialog(stage);
    }
}
