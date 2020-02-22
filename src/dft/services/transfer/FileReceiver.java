package dft.services.transfer;

import java.io.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileReceiver {
    private final static int BUFFER_SIZE = 8192;
    private final Callback callback;
    private final AtomicBoolean receiving;
    private long fileSize;
    private long receivedCount;

    public FileReceiver(Callback callback) {
        this.callback = callback;
        this.receiving = new AtomicBoolean(false);
    }

    public boolean isReceiving() {
        return receiving.get();
    }

    public int getReceivedPercentage() {
        return (int) ((receivedCount * 100) / fileSize);
    }

    public void receiveFile(FileInputStream inputStream) {
        if (receiving.get()) throw new IllegalStateException("Already receiving the file");

        DataInputStream input = new DataInputStream(inputStream);
        receiving.set(true);
        String fileName;
        try {
            fileName = input.readUTF();
            fileSize = input.readLong();
        } catch (IOException e) {
            callback.onFailure(e);
            return;
        }

        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(fileName))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            receivedCount = 0;
            int received;
            while ((received = input.read(buffer)) != -1) {
                if (!receiving.get()) return;
                fileWriter.write(buffer);
                receivedCount += received;
            }
            callback.onSuccess(new File(fileName));
        } catch (IOException e) {
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

        void onSuccess(File file);
    }
}
