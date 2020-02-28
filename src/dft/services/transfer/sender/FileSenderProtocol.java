package dft.services.transfer.sender;

import com.google.gson.Gson;
import dft.model.Device;
import dft.model.DeviceFactory;
import dft.model.Transfer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class FileSenderProtocol {
    private static final int SOCKET_PORT = 5001;
    private final Device remoteDevice;
    private final File file;
    private Callback callback;
    private FileSender fileSender;
    private Transfer transfer;

    public FileSenderProtocol(Device remoteDevice, File file) {
        this.remoteDevice = remoteDevice;
        this.file = file;
        this.fileSender = new FileSender(file);
    }

    public FileSenderProtocol(Device remoteDevice, File file, Callback callback) {
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
        transfer = new Transfer(remoteDevice, file.getName(), 0);

        try {
            Socket socket = new Socket(remoteDevice.getAddress(), SOCKET_PORT);
            InetAddress currentDeviceAddress = socket.getLocalAddress();
            OutputStream outputStream = socket.getOutputStream();
            sendFileData(currentDeviceAddress, outputStream);

            fileSender.send(outputStream);
        } catch (IOException e) {
            if (callback != null)
                callback.onFailure(e);
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

    private FileSender createFileSender(Callback callback) {
        FileSender.Callback fileSenderCallback = new FileSender.Callback() {
            @Override
            public void onStart() {
                transfer.setStatus(Transfer.TransferStatus.TRANSFERRING);
                callback.onStart();
            }

            @Override
            public void onFailure(Exception e) {
                transfer.setStatus(Transfer.TransferStatus.FAILED);
                callback.onFailure(e);
            }

            @Override
            public void onProgressUpdated() {
                transfer.setProgress(fileSender.getSentPercentage());
                callback.onProgressUpdated();
            }

            @Override
            public void onSuccess(File file) {
                transfer.setStatus(Transfer.TransferStatus.SUCCEEDED);
                callback.onSuccess(file);
            }
        };
        return new FileSender(file, fileSenderCallback);
    }

    public interface Callback {
        void onStart();

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
