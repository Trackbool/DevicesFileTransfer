package dft.services.transfer.sender;

import dft.domain.model.Device;
import dft.domain.model.TransferFile;

import java.util.List;

public interface FileSenderServiceExecutor {
    void send(List<Device> devices, TransferFile file);
}
