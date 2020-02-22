package dft.services.transfer;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileReceiver {
    private final static int BUFFER_SIZE = 8192;
    private final File dstFile;
    private final Callback callback;
    private final AtomicBoolean receiving;

    public FileReceiver(File dstFile, Callback callback) {
        this.dstFile = dstFile;
        this.callback = callback;
        this.receiving = new AtomicBoolean(false);
    }

    public boolean isReceiving() {
        return receiving.get();
    }

    public void receiveFile(FileInputStream inputStream) {
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(dstFile))) {
            receiving.set(true);
            byte[] buffer = new byte[BUFFER_SIZE];
            while (inputStream.read(buffer) != -1) {
                if (!receiving.get()) return;
                fileWriter.write(buffer);
            }
            callback.onSuccess();
        } catch (IOException e) {
            callback.onFailure();
        } finally {
            receiving.set(false);
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void cancel() {
        this.receiving.set(false);
    }

    public interface Callback {
        void onFailure();

        void onSuccess();
    }
}
