package dft.view.ui;

import dft.domain.model.Device;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;
import dft.domain.model.TransferFileFactory;
import dft.util.SystemUtils;
import dft.view.discovery.DiscoveryContract;
import dft.view.discovery.DiscoveryPresenter;
import dft.view.transfer.receiver.ReceiveTransferContract;
import dft.view.transfer.receiver.ReceiveTransferPresenter;
import dft.view.transfer.sender.SendTransferContract;
import dft.view.transfer.sender.SendTransferPresenter;
import dft.view.ui.util.AlertUtils;
import dft.view.ui.util.WindowUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable, DiscoveryContract.View,
        ReceiveTransferContract.View, SendTransferContract.View {
    private ObservableList<Device> devices;
    @FXML
    private ScrollPane rootLayout;
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
    private ReceiveTransferContract.Presenter receiveTransferPresenter;
    private SendTransferContract.Presenter sendTransferPresenter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        devices = FXCollections.observableArrayList();
        devicesTableView.setItems(devices);
        devicesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        sendingTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Transfer clickedTransfer = sendingTableView.getSelectionModel().getSelectedItem();
                String path = clickedTransfer.getFile().getPath();
                try {
                    WindowUtils.openFolder(new File(path).getParentFile());
                } catch (IOException e) {
                    showAlert("Error", "Could not open folder");
                }
            }
        });

        receptionsTransfers = FXCollections.observableArrayList();
        receptionsTableView.setItems(receptionsTransfers);
        receptionsDeviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        receptionsIpAddressColumn.setCellValueFactory(new PropertyValueFactory<>("deviceIpAddress"));
        receptionsFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        receptionsProgressColumn.setCellValueFactory(new PropertyValueFactory<>("progressPercentage"));
        receptionsStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statusValue"));
        receptionsTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                Transfer clickedTransfer = receptionsTableView.getSelectionModel().getSelectedItem();
                String path = clickedTransfer.getFile().getPath();
                try {
                    WindowUtils.openFolder(new File(path).getParentFile());
                } catch (IOException e) {
                    showAlert("Error", "Could not open folder");
                }
            }
        });

        this.discoveryPresenter = new DiscoveryPresenter(this);
        discoveryPresenter.onViewLoaded();
        this.receiveTransferPresenter = new ReceiveTransferPresenter(this);
        receiveTransferPresenter.onViewLoaded();
        this.sendTransferPresenter = new SendTransferPresenter(this);

        rootLayout.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            }
            event.consume();
        });
        rootLayout.setOnDragDropped(event -> {
            List<File> files = event.getDragboard().getFiles();
            attachFiles(files);
            event.consume();
        });
    }

    @FXML
    private void discoverDevicesButtonClicked() {
        discoveryPresenter.onDiscoverDevicesEvent();
    }

    @FXML
    private void browseFileButtonClicked() {
        sendTransferPresenter.onBrowseFileButtonClicked();
    }

    @FXML
    private void sendFileButtonClicked() {
        sendTransferPresenter.onSendFileButtonClicked();
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
        List<File> files = WindowUtils.browseFile();
        if (files != null && !files.isEmpty()) {
            attachFiles(files);
        }
    }

    private void attachFiles(List<File> files) {
        List<TransferFile> transferFiles = new ArrayList<>();
        for (File file : files) {
            TransferFile transferFile = TransferFileFactory.getFromFile(file);
            transferFiles.add(transferFile);
        }
        sendTransferPresenter.onFilesAttached(transferFiles);
    }

    @Override
    public void showFilesAttachedName(List<TransferFile> files) {
        StringBuilder sb = new StringBuilder();
        for (TransferFile file : files) {
            sb.append(file.getName()).append(", ");
        }
        String resultText = sb.substring(0, sb.length() - 2);
        labelFileAttached.setText(resultText);
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
        this.receiveTransferPresenter.onDestroy();
        this.sendTransferPresenter.onDestroy();
    }

    private void runOnUiThread(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
