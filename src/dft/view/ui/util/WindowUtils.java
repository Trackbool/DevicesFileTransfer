package dft.view.ui.util;

import dft.util.SystemUtils;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class WindowUtils {
    public static List<File> browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = new Stage();
        return fileChooser.showOpenMultipleDialog(stage);
    }

    public static void openFolder(File folderPath) {
        try {
            Desktop.getDesktop().open(folderPath);
        } catch (IOException ignored) {}
    }
}
