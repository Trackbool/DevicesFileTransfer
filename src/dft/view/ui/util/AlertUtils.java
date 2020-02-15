package dft.view.ui.util;

import javafx.scene.control.Alert;

public class AlertUtils {
    /**
     * Show an error message
     *
     * @param header  header of the error.
     * @param message message of the error.
     */
    public static void showError(String header, String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Error");
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * Show an information message
     *
     * @param header  the header
     * @param message the message
     */
    public static void showMessage(String header, String message) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Info");
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        dialog.showAndWait();
    }
}
