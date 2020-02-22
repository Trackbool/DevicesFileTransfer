package dft.services.transfer;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileSender {
    private final static int BUFFER_SIZE = 8192;
    private final File file;
    private Callback callback;
    private final AtomicBoolean sending;
    private long sentCount;

    public FileSender(File file, Callback callback) {
        this.file = file;
        sending = new AtomicBoolean(false);
        this.callback = callback;
    }

    public boolean isSending() {
        return sending.get();
    }

    public int getSentPercentage() {
        return (int) ((sentCount * 100) / file.length());
    }

    public void send(FileOutputStream outputStream) {
        if (sending.get()) throw new IllegalArgumentException("Already sending the file");

        try (FileInputStream fileReader = new FileInputStream(file);
             DataOutputStream output = new DataOutputStream(outputStream)) {
            sending.set(true);
            output.writeUTF(file.getName());
            output.writeLong(file.length());

            byte[] buffer = new byte[BUFFER_SIZE];
            sentCount = 0;
            int sent;
            while ((sent = fileReader.read(buffer)) != -1) {
                if (!sending.get()) return;
                output.write(buffer);
                sentCount += sent;
            }
            callback.onSuccess(file);
        } catch (IOException e) {
            callback.onFailure(e);
        } finally {
            sending.set(false);
        }
    }

    public void cancel() {
        sending.set(false);
    }

    public interface Callback {
        void onFailure(IOException e);

        void onSuccess(File file);
    }
}
