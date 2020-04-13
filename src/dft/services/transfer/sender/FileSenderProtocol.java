package dft.services.transfer.sender;

import com.google.gson.Gson;
import dft.domain.model.Device;
import dft.domain.model.DeviceFactory;
import dft.domain.model.Transfer;
import dft.domain.model.TransferFile;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileSenderProtocol {
    private static final int SOCKET_PORT = 5001;
    private final Device remoteDevice;
    private final TransferFile file;
    private Callback callback;
    private FileSender fileSender;
    private Transfer transfer;

    public FileSenderProtocol(Device remoteDevice, TransferFile file) {
        this.remoteDevice = remoteDevice;
        this.file = file;
        this.fileSender = new FileSender(file);
        transfer = new Transfer(remoteDevice, file.getName(), 0, false);
    }

    public FileSenderProtocol(Device remoteDevice, TransferFile file, Callback callback) {
        this(remoteDevice, file);
        this.callback = callback;
        this.fileSender = createFileSender(callback);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
        this.fileSender = createFileSender(callback);
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public boolean isSending() {
        return fileSender.isSending();
    }

    public int getSentPercentage() {
        return fileSender.getSentPercentage();
    }

    public void send() {
        if (!file.exists()) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            if (callback != null) {
                callback.onInitializationFailure(transfer,
                        new FileNotFoundException("File " + file.getPath() + " doesn´t exists"));
            }
            return;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(remoteDevice.getAddress(), SOCKET_PORT), 3000);
            InetAddress currentDeviceAddress = socket.getLocalAddress();
            OutputStream outputStream = socket.getOutputStream();
            sendFileData(currentDeviceAddress, outputStream);

            fileSender.send(outputStream);
        } catch (IOException e) {
            if (callback != null) {
                transfer.setStatus(Transfer.TransferStatus.FAILED);
                callback.onInitializationFailure(transfer, e);
            }
        }
    }

    private void sendFileData(InetAddress currentDeviceAddress, OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        Device currentDevice = DeviceFactory.getCurrentDevice(currentDeviceAddress);
        dataOutputStream.writeUTF(new Gson().toJson(currentDevice));
        dataOutputStream.writeUTF(file.getName());
        dataOutputStream.writeLong(file.length());
    }

    public void cancel() {
        fileSender.cancel();
    }

    private FileSender createFileSender(final Callback callback) {
        FileSender.Callback fileSenderCallback = new FileSender.Callback() {
            @Override
            public void onStart() {
                transfer.setStatus(Transfer.TransferStatus.TRANSFERRING);
                callback.onStart(transfer);
            }

            @Override
            public void onFailure(Exception e) {
                transfer.setStatus(Transfer.TransferStatus.FAILED);
                callback.onFailure(transfer, e);
            }

            @Override
            public void onProgressUpdated() {
                transfer.setProgress(fileSender.getSentPercentage());
                callback.onProgressUpdated(transfer);
            }

            @Override
            public void onSuccess(TransferFile file) {
                transfer.setStatus(Transfer.TransferStatus.COMPLETED);
                callback.onSuccess(transfer, file);
            }
        };
        return new FileSender(file, fileSenderCallback);
    }

    public interface Callback {
        void onInitializationFailure(Transfer transfer, Exception e);

        void onStart(Transfer transfer);

        void onFailure(Transfer transfer, Exception e);

        void onProgressUpdated(Transfer transfer);

        void onSuccess(Transfer transfer, TransferFile file);
    }
}
