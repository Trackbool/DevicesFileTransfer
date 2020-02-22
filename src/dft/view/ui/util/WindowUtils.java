package dft.view.ui.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class WindowUtils {
    public static String browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);
        String chosenFile = null;
        if (selectedFile != null) chosenFile = selectedFile.getName();
        return chosenFile;
    }
}
