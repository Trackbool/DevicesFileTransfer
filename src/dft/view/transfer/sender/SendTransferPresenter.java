package dft.view.transfer.sender;

import dft.domain.model.Device;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;
import dft.services.transfer.sender.FileSenderProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SendTransferPresenter implements SendTransferContract.Presenter {
    private SendTransferContract.View view;
    private ThreadPoolExecutor fileSendingExecutor;

    private List<TransferFile> attachedFiles;

    public SendTransferPresenter(SendTransferContract.View view) {
        this.view = view;
        attachedFiles = new ArrayList<>();
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onBrowseFileButtonClicked() {
        view.browseFile();
    }

    @Override
    public void onFilesAttached(List<TransferFile> file) {
        attachedFiles = file;
        view.showFilesAttachedName(attachedFiles);
    }

    @Override
    public void onSendFileButtonClicked() {
        if (attachedFiles.isEmpty()) {
            view.showAlert("No file attached", "You must attach a file");
            return;
        }

        Device[] devices = view.getSelectedDevices();
        if (devices.length == 0) {
            view.showAlert("No device selected", "You must select one or more devices to send the file");
        }

        for (Device device : devices) {
            sendFile(device);
        }
    }

    private void sendFile(Device device) {
        fileSendingExecutor.execute(() -> {
            FileSenderProtocol fileSender = createFileSender(device, attachedFiles);
            fileSender.send();
        });
    }

    private FileSenderProtocol createFileSender(Device device, List<TransferFile> files) {
        FileSenderProtocol fileSender = new FileSenderProtocol(device, files);
        fileSender.setCallback(new FileSenderProtocol.Callback() {

            @Override
            public void onInitializationFailure() {

            }

            @Override
            public void onTransferInitializationFailure(Transfer transfer, Exception e) {

            }

            @Override
            public void onStart(Transfer transfer) {
                view.refreshSendingData();
                view.addSendingTransfer(transfer);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                view.refreshSendingData();
                view.showError("Sending error", e.getMessage());
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                view.refreshSendingData();
            }

            @Override
            public void onSuccess(Transfer transfer, TransferFile file) {
                view.refreshSendingData();
            }
        });

        return fileSender;
    }

    @Override
    public void onDestroy() {
        fileSendingExecutor.shutdownNow();
        view = null;
        attachedFiles = null;
    }
}
