package dft.view.transfer.sender;

import dft.domain.model.Device;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;

import java.util.List;

public interface SendTransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void browseFile();

        void showFilesAttachedName(List<TransferFile> files);

        Device[] getSelectedDevices();

        void addSendingTransfer(Transfer transfer);

        void refreshSendingData();

        void close();
    }

    interface Presenter {
        void onBrowseFileButtonClicked();

        void onFilesAttached(List<TransferFile> files);

        void onSendFileButtonClicked();

        void onDestroy();
    }
}
