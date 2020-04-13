package dft.view.transfer.receiver;

import dft.domain.model.Transfer;
import dft.services.transfer.receiver.FileReceiverProtocol;
import dft.services.transfer.receiver.FilesReceiverListener;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ReceiveTransferPresenter implements ReceiveTransferContract.Presenter {
    private final static int TRANSFER_SERVICE_PORT = 5001;
    private final ReceiveTransferContract.View view;
    private FilesReceiverListener filesReceiverListener;
    private ThreadPoolExecutor fileReceivingExecutor;

    public ReceiveTransferPresenter(ReceiveTransferContract.View view) {
        this.view = view;
        fileReceivingExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    @Override
    public void onViewLoaded() {
        filesReceiverListener = new FilesReceiverListener(TRANSFER_SERVICE_PORT, inputStream -> {
            FileReceiverProtocol fileReceiver = createFileReceiver();
            fileReceivingExecutor.execute(() -> fileReceiver.receive(inputStream));
        });

        new Thread(() -> {
            try {
                filesReceiverListener.start();
            } catch (IOException e) {
                view.showError("Initialization error", e.getMessage());
                view.close();
            }
        }).start();
    }

    private FileReceiverProtocol createFileReceiver() {
        final FileReceiverProtocol fileReceiver = new FileReceiverProtocol(view.getDownloadsDirectory());
        fileReceiver.setCallback(new FileReceiverProtocol.Callback() {
            @Override
            public void onStart(Transfer transfer) {
                view.addReceptionTransfer(transfer);
            }

            @Override
            public void onFailure(Transfer transfer, Exception e) {
                view.refreshReceptionsData();
                view.showError("Receiving error", e.getMessage());
            }

            @Override
            public void onProgressUpdated(Transfer transfer) {
                view.refreshReceptionsData();
            }

            @Override
            public void onSuccess(Transfer transfer, File file) {
                view.refreshReceptionsData();
                view.showAlert("Receiving success", file.getName());
            }
        });

        return fileReceiver;
    }

    @Override
    public void onDestroy() {
        filesReceiverListener.stop();
        fileReceivingExecutor.shutdownNow();
    }
}
