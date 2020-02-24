package dft.view.transfer;

import dft.model.Device;
import dft.model.Transfer;
import dft.services.transfer.FileReceiver;
import dft.services.transfer.FileSender;
import dft.services.transfer.FilesReceiverListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TransferPresenter implements TransferContract.Presenter {
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private TransferContract.View view;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileSendingExecutor;
    private ThreadPoolExecutor fileReceivingExecutor;

    private File fileToSend;

    public TransferPresenter(TransferContract.View view) {
        this.view = view;
        this.fileSendingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        this.fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onViewLoaded() {
        filesReceiverListener = new FilesReceiverListener(TRANSFER_SERVICE_PORT, inputStream -> {
            FileReceiver fileReceiver = this.createFileReceiver();
            fileReceivingExecutor.execute(() -> fileReceiver.receive(inputStream));
        });

        new Thread(() -> {
            try {
                filesReceiverListener.start();
            } catch (IOException e) {
                this.view.showError("Initialization error", e.getMessage());
                this.view.close();
            }
        }).start();
    }

    @Override
    public void onBrowseFileButtonClicked() {
        view.browseFile();
    }

    @Override
    public void onFileAttached(File file) {
        this.fileToSend = file;
        view.showFileAttachedName(fileToSend.getName());
    }

    @Override
    public void onSendFileButtonClicked() {
        if (fileToSend == null) {
            view.showAlert("No file attached", "You must attach a file");
            return;
        }

        Device[] devices = view.getSelectedDevices();
        if (devices.length == 0) {
            view.showAlert("No device selected", "You must select one or more devices to send the file");
        }

        for (Device device : devices) {
            fileSendingExecutor.execute(() -> this.sendFile(device));
        }
    }

    private void sendFile(Device device) {
        FileSender fileSender = this.createFileSender(device, fileToSend);
        fileSender.send();
    }

    private FileReceiver createFileReceiver() {
        FileReceiver fileReceiver = new FileReceiver();
        fileReceiver.setCallback(new FileReceiver.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                view.addReceptionTransfer(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                view.showError("Receiving error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {

            }

            @Override
            public void onSuccess(File file) {
                view.showAlert("Receiving success", file.getName());
            }
        });

        return fileReceiver;
    }

    private FileSender createFileSender(Device device, File file) {
        FileSender fileSender = new FileSender(device, file);
        fileSender.setCallback(new FileSender.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                view.addSendingTransfer(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                view.showError("Sending error", e.getMessage());
            }

            @Override
            public void onProgressUpdated() {
                fileSender.getSentPercentage();
            }

            @Override
            public void onSuccess(File file) {
                view.showAlert("Sending success", file.getName());
            }
        });

        return fileSender;
    }

    @Override
    public void onDestroy() {
        this.view = null;
        fileToSend = null;
        filesReceiverListener.stop();
        fileSendingExecutor.shutdownNow();
        fileReceivingExecutor.shutdownNow();
    }
}
