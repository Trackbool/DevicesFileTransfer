package dft.services.transfer;

import com.google.gson.Gson;
import dft.model.Device;
import dft.model.DeviceFactory;
import dft.model.Transfer;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileSender {
    private final static int BUFFER_SIZE = 8192;
    private final Device device;
    private final File file;
    private Callback callback;
    private final AtomicBoolean sending;
    private AtomicLong sentCount;

    public FileSender(Device device, File file) {
        this.device = device;
        this.file = file;
        this.sending = new AtomicBoolean(false);
        this.sentCount = new AtomicLong(0);
    }

    public FileSender(Device device, File file, Callback callback) {
        this(device, file);
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isSending() {
        return sending.get();
    }

    public int getSentPercentage() {
        return (int) ((sentCount.get() * 100) / file.length());
    }

    public void send() {
        if (sending.get()) throw new IllegalStateException("Already sending the file");

        Socket socket;
        OutputStream outputStream;
        try {
            socket = new Socket(device.getAddress(), 5001);
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            if (callback != null)
                callback.onFailure(e);
            return;
        }

        sending.set(true);
        Transfer transfer = new Transfer(
                DeviceFactory.getCurrentDevice(socket.getLocalAddress()), file.getName(), 0);
        if (callback != null)
            callback.onStart(transfer);
        try (FileInputStream fileReader = new FileInputStream(file);
             DataOutputStream output = new DataOutputStream(outputStream)) {
            InetAddress currentDeviceAddress = socket.getLocalAddress();
            Device currentDevice = DeviceFactory.getCurrentDevice(currentDeviceAddress);
            output.writeUTF(new Gson().toJson(currentDevice));
            output.writeUTF(file.getName());
            output.writeLong(file.length());

            byte[] buffer = new byte[BUFFER_SIZE];
            sentCount.set(0);
            int sent;
            while ((sent = fileReader.read(buffer, 0, buffer.length)) != -1) {
                if (!sending.get() || Thread.interrupted()) return;
                output.write(buffer, 0, sent);
                sentCount.getAndAdd(sent);
                transfer.setPercentage(getSentPercentage());
                if (callback != null)
                    callback.onProgressUpdated();
            }
            if (callback != null) {
                if (sentCount.get() == file.length()) {
                    callback.onSuccess(file);
                } else {
                    callback.onFailure(new Exception("The file has not been completely transferred"));
                }
            }
        } catch (IOException e) {
            sending.set(false);
            if (callback != null)
                callback.onFailure(e);
        } finally {
            sending.set(false);
        }
    }

    public void cancel() {
        sending.set(false);
    }

    public interface Callback {
        void onStart(Transfer transfer);

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
