package dft.view.ui;

import dft.model.Device;
import dft.util.SystemUtils;
import dft.view.discovery.DiscoveryContract;
import dft.view.discovery.DiscoveryPresenter;
import dft.model.Transfer;
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

    //Sending
    private ObservableList<Transfer> sendingTransfers;
    @FXML
    private TableView<Transfer> sendingTableView;
    @FXML
    private TableColumn<Transfer, String> sendingDeviceNameColumn;
    @FXML
    private TableColumn<Transfer, InetAddress> sendingIpAddressColumn;
    @FXML
    private TableColumn<Transfer, String> sendingFileNameColumn;
    @FXML
    private TableColumn<Transfer, String> sendingProgressColumn;
    @FXML
    private TableColumn<Transfer, String> sendingStatusColumn;

    //Receptions
    private ObservableList<Transfer> receptionsTransfers;
    @FXML
    private TableView<Transfer> receptionsTableView;
    @FXML
    private TableColumn<Transfer, String> receptionsDeviceNameColumn;
    @FXML
    private TableColumn<Transfer, InetAddress> receptionsIpAddressColumn;
    @FXML
    private TableColumn<Transfer, String> receptionsFileNameColumn;
    @FXML
    private TableColumn<Transfer, String> receptionsProgressColumn;
    @FXML
    private TableColumn<Transfer, String> receptionsStatusColumn;

    private DiscoveryContract.Presenter discoveryPresenter;
    private TransferContract.Presenter transferPresenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        devices = FXCollections.observableArrayList();
        devicesTableView.setItems(devices);
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        osColumn.setCellValueFactory(new PropertyValueFactory<>("os"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        sendingTransfers = FXCollections.observableArrayList();
        sendingTableView.setItems(sendingTransfers);
        sendingDeviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        sendingIpAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deviceIpAddress"));
        sendingFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        sendingProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progressPercentage"));
        sendingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));

        receptionsTransfers = FXCollections.observableArrayList();
        receptionsTableView.setItems(receptionsTransfers);
        receptionsDeviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        receptionsIpAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deviceIpAddress"));
        receptionsFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        receptionsProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progressPercentage"));
        receptionsStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));

        this.discoveryPresenter = new DiscoveryPresenter(this);
        discoveryPresenter.onViewLoaded();
        this.transferPresenter = new TransferPresenter(this);
        transferPresenter.onViewLoaded();
    }

    @FXML
    private void discoverDevicesButtonClicked() {
        discoveryPresenter.onDiscoverDevicesEvent();
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
        runOnUiThread(() -> AlertUtils.showError(title, message));
    }

    @Override
    public List<Device> getDevicesList() {
        return devices;
    }

    @Override
    public void showAlert(String title, String message) {
        runOnUiThread(() -> AlertUtils.showMessage(title, message));
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

    @Override
    public void addSendingTransfer(Transfer transfer) {
        runOnUiThread(() -> sendingTransfers.add(transfer));
    }

    @Override
    public void refreshSendingData() {
        sendingTableView.refresh();
    }

    @Override
    public void addReceptionTransfer(Transfer transfer) {
        runOnUiThread(() -> receptionsTransfers.add(transfer));
    }

    @Override
    public void refreshReceptionsData() {
        receptionsTableView.refresh();
    }

    @Override
    public File getDownloadsDirectory() {
        return SystemUtils.getDownloadsDirectory();
    }

    private void onQuit() {
        this.discoveryPresenter.onDestroy();
        this.transferPresenter.onDestroy();
    }

    private void runOnUiThread(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
