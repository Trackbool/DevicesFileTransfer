package dft.view.transfer;

import dft.model.Device;
import dft.model.Transfer;

import java.io.File;

public interface TransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void browseFile();

        void showFileAttachedName(String name);

        Device[] getSelectedDevices();

        void addSendingTransfer(Transfer transfer);

        void refreshSendingData();

        void addReceptionTransfer(Transfer transfer);

        void refreshReceptionsData();

        File getDownloadsDirectory();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onBrowseFileButtonClicked();

        void onFileAttached(File file);

        void onSendFileButtonClicked();

        void onDestroy();
    }
}
