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
import java.util.List;

public class FileSenderProtocol {
    private static final int SOCKET_PORT = 5001;
    private final Device remoteDevice;
    private Device currentDevice;
    private final List<TransferFile> files;
    private Callback callback;
    private boolean isSending;

    public FileSenderProtocol(Device remoteDevice, List<TransferFile> files) {
        isSending = false;
        this.remoteDevice = remoteDevice;
        this.files = files;
    }

    public FileSenderProtocol(Device remoteDevice, List<TransferFile> files, Callback callback) {
        this(remoteDevice, files);
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isSending() {
        return isSending;
    }

    public void send() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(remoteDevice.getAddress(), SOCKET_PORT), 3000);
            InetAddress currentDeviceAddress = socket.getLocalAddress();
            currentDevice = DeviceFactory.getCurrentDevice(currentDeviceAddress);
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            isSending = true;
            outputStream.writeInt(files.size());
            for (TransferFile file : files) {
                final Transfer transfer = new Transfer(
                        remoteDevice, file, 0, false);
                if (!file.exists()) {
                    transfer.setStatus(Transfer.TransferStatus.FAILED);
                    if (callback != null) {
                        callback.onTransferInitializationFailure(transfer,
                                new FileNotFoundException("File " + file.getPath() + " doesn´t " +
                                        "exists or cannot be accessed"));
                    }
                    continue;
                }
                final FileSender fileSender = createFileSender(transfer, callback);

                try {
                    sendFileData(file, outputStream);
                    fileSender.send(outputStream);
                } catch (IOException e) {
                    if (callback != null) {
                        callback.onFailure(transfer, e);
                    }
                }
            }
        } catch (IOException e) {
            if (callback != null) {
                callback.onInitializationFailure();
            }
        } finally {
            isSending = false;
        }
    }

    private void sendFileData(TransferFile file, DataOutputStream outputStream) throws IOException {
        outputStream.writeUTF(new Gson().toJson(currentDevice));
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());
    }

    private FileSender createFileSender(final Transfer transfer, final Callback callback) {
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
            public void onProgressUpdated(int percentage) {
                transfer.setProgress(percentage);
                callback.onProgressUpdated(transfer);
            }

            @Override
            public void onSuccess(TransferFile file) {
                transfer.setStatus(Transfer.TransferStatus.COMPLETED);
                callback.onSuccess(transfer, file);
            }
        };
        return new FileSender(transfer.getFile(), fileSenderCallback);
    }

    public interface Callback {
        void onInitializationFailure();

        void onTransferInitializationFailure(Transfer transfer, Exception e);

        void onStart(Transfer transfer);

        void onFailure(Transfer transfer, Exception e);

        void onProgressUpdated(Transfer transfer);

        void onSuccess(Transfer transfer, TransferFile file);
    }
}