package dft.view.transfer.sender;

import dft.domain.model.Device;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;

public interface SendTransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void browseFile();

        void showFileAttachedName(String name);

        Device[] getSelectedDevices();

        void addSendingTransfer(Transfer transfer);

        void refreshSendingData();

        void close();
    }

    interface Presenter {
        void onBrowseFileButtonClicked();

        void onFileAttached(TransferFile file);

        void onSendFileButtonClicked();

        void onDestroy();
    }
}
