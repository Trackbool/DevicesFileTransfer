package dft.services.transfer;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileReceiver {
    private final static int BUFFER_SIZE = 8192;
    private Callback callback;
    private final AtomicBoolean receiving;
    private AtomicLong fileSize;
    private AtomicLong receivedCount;

    public FileReceiver() {
        this.receiving = new AtomicBoolean(false);
        this.fileSize = new AtomicLong(0);
        this.receivedCount = new AtomicLong(0);
    }

    public FileReceiver(Callback callback) {
        this();
        this.callback = callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public boolean isReceiving() {
        return receiving.get();
    }

    public int getReceivedPercentage() {
        return (int) ((receivedCount.get() * 100) / fileSize.get());
    }

    public void receive(InputStream inputStream) {
        if (receiving.get()) throw new IllegalStateException("Already receiving the file");

        DataInputStream input = new DataInputStream(inputStream);
        receiving.set(true);
        String fileName;
        try {
            fileName = input.readUTF();
            fileSize.set(input.readLong());
        } catch (IOException e) {
            receiving.set(false);
            if (callback != null)
                callback.onFailure(e);
            return;
        }

        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(fileName))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            receivedCount.set(0);
            int received;
            while ((received = input.read(buffer, 0, buffer.length)) != -1) {
                if (!receiving.get() || Thread.interrupted()) return;
                fileWriter.write(buffer, 0, received);
                receivedCount.getAndAdd(received);
                if (callback != null)
                    callback.onProgressUpdated();
            }
            if (callback != null) {
                if(getReceivedPercentage() == 100) {
                    callback.onSuccess(new File(fileName));
                } else {
                    callback.onFailure(new Exception("The file has not been completely transferred"));
                }
            }
        } catch (IOException e) {
            receiving.set(false);
            if (callback != null)
                callback.onFailure(e);
        } finally {
            receiving.set(false);
            try {
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void cancel() {
        this.receiving.set(false);
    }

    public interface Callback {
        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
