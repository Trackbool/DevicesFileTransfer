package dft.view.ui;

import dft.model.Device;
import dft.view.MainContract;
import dft.view.MainPresenter;
import dft.view.ui.util.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, MainContract.View {
    private ObservableList<Device> devices;
    @FXML
    private TableView<Device> devicesTableView;
    @FXML
    private TableColumn<Device, String> deviceNameColumn;
    @FXML
    private TableColumn<Device, String> osColumn;
    @FXML
    private TableColumn<Device, String> ipAddressColumn;

    private MainContract.Presenter presenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        devices = FXCollections.observableArrayList();
        devicesTableView.setItems(devices);
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        osColumn.setCellValueFactory(new PropertyValueFactory<>("os"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        this.presenter = new MainPresenter(this);
        presenter.onViewLoaded();
    }

    @FXML
    private void discoverDevicesButtonClicked() {
        presenter.onDiscoverDevicesButtonClicked();
    }

    @Override
    public void addDevice(Device device) {
        devices.add(device);
    }

    @Override
    public void showError(String title, String message) {
        AlertUtils.showError(title, message);
    }

    @Override
    public void clearDevicesList() {
        devices.clear();
    }

    public void setOnCloseListener(Stage stage) {
        stage.setOnCloseRequest(e -> this.onQuit());
    }

    @Override
    public void close() {
        Platform.exit();
        System.exit(0);
    }

    private void onQuit() {
        this.presenter.onDestroy();
    }
}
