package dft.services.transfer.sender;

import dft.domain.model.Transfer;
import dft.services.ServiceConnectionCallback;

import java.util.List;

public interface FileSenderReceiver {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(FileSenderProtocol.Callback callback);

    List<Transfer> getInProgressTransfers();

    void receive();

    void stop();
}
