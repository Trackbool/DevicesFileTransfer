package dft.view.transfer.sender;

import dft.domain.model.Device;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;
import dft.services.transfer.sender.FileSenderProtocol;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class SendTransferPresenter implements SendTransferContract.Presenter {
    private SendTransferContract.View view;
    private ThreadPoolExecutor fileSendingExecutor;

    private TransferFile attachedFile;

    public SendTransferPresenter(SendTransferContract.View view) {
        this.view = view;
        fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onBrowseFileButtonClicked() {
        view.browseFile();
    }

    @Override
    public void onFileAttached(TransferFile file) {
        attachedFile = file;
        view.showFileAttachedName(attachedFile.getName());
    }

    @Override
    public void onSendFileButtonClicked() {
        if (attachedFile == null) {
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
            FileSenderProtocol fileSender = createFileSender(device, attachedFile);
            fileSender.send();
        });
    }

    private FileSenderProtocol createFileSender(Device device, TransferFile file) {
        FileSenderProtocol fileSender = new FileSenderProtocol(device, file);
        fileSender.setCallback(new FileSenderProtocol.Callback() {
            @Override
            public void onInitializationFailure(Transfer transfer, Exception e) {
                view.showError("Transfer error", e.getMessage());
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
                view.showAlert("Sending success", file.getName());
            }
        });

        return fileSender;
    }

    @Override
    public void onDestroy() {
        fileSendingExecutor.shutdownNow();
        view = null;
        attachedFile = null;
    }
}
