package dft.view.ui.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class WindowUtils {
    public static List<File> browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = new Stage();
        return fileChooser.showOpenMultipleDialog(stage);
    }
}
