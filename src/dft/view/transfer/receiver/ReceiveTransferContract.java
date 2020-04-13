package dft.view.transfer.receiver;

import dft.domain.model.Transfer;

import java.io.File;

public interface ReceiveTransferContract {
    interface View {
        void showError(String title, String message);

        void showAlert(String title, String message);

        void addReceptionTransfer(Transfer transfer);

        void refreshReceptionsData();

        File getDownloadsDirectory();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDestroy();
    }
}
