package dft.services.transfer.receiver;

import dft.domain.model.Transfer;
import dft.services.ServiceConnectionCallback;

import java.util.List;

public interface FilesReceiverListenerReceiver {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(FileReceiverProtocol.Callback callback);

    List<Transfer> getInProgressTransfers();

    void receive();

    void stop();
}
