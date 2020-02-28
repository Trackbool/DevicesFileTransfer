package dft.services.transfer.receiver;

import com.google.gson.Gson;
import dft.model.Device;
import dft.model.Transfer;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileReceiverProtocol {
    private Callback callback;
    private FileReceiver fileReceiver;
    private Transfer transfer;

    public FileReceiverProtocol() {
        fileReceiver = new FileReceiver();
    }

    public FileReceiverProtocol(Callback callback) {
        this.callback = callback;
        this.fileReceiver = createFileReceiver(callback);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
        this.fileReceiver = createFileReceiver(callback);
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public boolean isReceiving() {
        return fileReceiver.isReceiving();
    }

    public int getSentPercentage() {
        return fileReceiver.getReceivedPercentage();
    }

    public void receive(InputStream inputStream) {
        try {
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            String deviceJson = dataInputStream.readUTF();
            Device device = new Gson().fromJson(deviceJson, Device.class);
            String fileName = dataInputStream.readUTF();
            long fileSize = dataInputStream.readLong();
            transfer = new Transfer(device, fileName, 0);

            fileReceiver.receive(fileName, fileSize, inputStream);
        } catch (IOException e) {
            if (callback != null)
                callback.onFailure(e);
        }
    }

    public void cancel() {
        fileReceiver.cancel();
    }

    private FileReceiver createFileReceiver(Callback callback) {
        FileReceiver.Callback fileReceiverCallback = new FileReceiver.Callback() {
            @Override
            public void onStart() {
                callback.onStart();
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }

            @Override
            public void onProgressUpdated() {
                transfer.setProgress(fileReceiver.getReceivedPercentage());
                callback.onProgressUpdated();
            }

            @Override
            public void onSuccess(File file) {
                callback.onSuccess(file);
            }
        };
        return new FileReceiver(fileReceiverCallback);
    }

    public interface Callback {
        void onStart();

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}