package dft.services.transfer;

import java.io.*;

public class FileReceiver {
    private final static int BUFFER_SIZE = 8192;
    private final File dstFile;
    private final Callback callback;

    public FileReceiver(File dstFile, Callback callback) {
        this.dstFile = dstFile;
        this.callback = callback;
    }

    public void receiveFile(InputStream inputStream) {
        byte[] buffer = new byte[BUFFER_SIZE];
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(dstFile))) {
            while (inputStream.read(buffer) != -1) {
                fileWriter.write(buffer);
            }
            callback.onSuccess();
        } catch (IOException e) {
            callback.onFailure();
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public interface Callback {
        void onFailure();

        void onSuccess();
    }
}
