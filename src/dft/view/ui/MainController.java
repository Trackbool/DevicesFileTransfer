package dft.view.ui;

import dft.view.MainContract;
import dft.view.MainPresenter;
import dft.view.ui.util.AlertUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, MainContract.View {

    private MainContract.Presenter presenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.presenter = new MainPresenter(this);
        presenter.onViewLoaded();
    }

    @FXML
    private void discoverDevicesButtonClicked() {
        presenter.onDiscoverDevicesButtonClicked();
    }

    @Override
    public void showError(String title, String message) {
        AlertUtils.showError(title, message);
    }

    public void setOnCloseListener(Stage stage) {
        stage.setOnCloseRequest(e -> this.onQuit());
    }

    @Override
    public void close() {
        Platform.exit();
        System.exit(0);
    }

    private void onQuit(){
        this.presenter.onDestroy();
    }
}
