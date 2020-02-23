package dft.view.ui;

import dft.model.Device;
import dft.view.discovery.DiscoveryContract;
import dft.view.discovery.DiscoveryPresenter;
import dft.view.transfer.TransferContract;
import dft.view.transfer.TransferPresenter;
import dft.view.ui.util.AlertUtils;
import dft.view.ui.util.WindowUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable, DiscoveryContract.View, TransferContract.View {
    private ObservableList<Device> devices;
    @FXML
    private TableView<Device> devicesTableView;
    @FXML
    private TableColumn<Device, String> deviceNameColumn;
    @FXML
    private TableColumn<Device, String> osColumn;
    @FXML
    private TableColumn<Device, InetAddress> ipAddressColumn;
    @FXML
    private Label labelFileAttached;

    private DiscoveryContract.Presenter discoveryPresenter;
    private TransferContract.Presenter transferPresenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        devices = FXCollections.observableArrayList();
        devicesTableView.setItems(devices);
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        osColumn.setCellValueFactory(new PropertyValueFactory<>("os"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        this.discoveryPresenter = new DiscoveryPresenter(this);
        discoveryPresenter.onViewLoaded();
        this.transferPresenter = new TransferPresenter(this);
        transferPresenter.onViewLoaded();
    }

    @FXML
    private void discoverDevicesButtonClicked() {
        discoveryPresenter.onDiscoverDevicesButtonClicked();
    }

    @FXML
    private void browseFileButtonClicked() {
        transferPresenter.onBrowseFileButtonClicked();
    }

    @FXML
    private void sendFileButtonClicked() {
        transferPresenter.onSendFileButtonClicked();
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
    public void showAlert(String title, String message) {
        AlertUtils.showMessage(title, message);
    }

    @Override
    public void browseFile() {
        File file = WindowUtils.browseFile();
        if (file != null) {
            transferPresenter.onFileAttached(file);
        }
    }

    @Override
    public void showFileAttachedName(String name) {
        labelFileAttached.setText(name);
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

    @Override
    public Device[] getSelectedDevices() {
        ObservableList<Device> observableList = devicesTableView.getSelectionModel().getSelectedItems();
        List<Device> devicesList = new ArrayList<>(observableList);
        Device[] devices = new Device[devicesList.size()];
        return devicesList.toArray(devices);
    }

    private void onQuit() {
        this.discoveryPresenter.onDestroy();
        this.transferPresenter.onDestroy();
    }
}
