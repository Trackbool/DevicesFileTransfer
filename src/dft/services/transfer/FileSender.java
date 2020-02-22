package dft.services.transfer;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileSender {
    private final static int BUFFER_SIZE = 8192;
    private final File file;
    private Callback callback;
    private final AtomicBoolean sending;
    private AtomicLong sentCount;

    public FileSender(File file, Callback callback) {
        this.file = file;
        this.sending = new AtomicBoolean(false);
        this.callback = callback;
        this.sentCount = new AtomicLong();
    }

    public boolean isSending() {
        return sending.get();
    }

    public int getSentPercentage() {
        return (int) ((sentCount.get() * 100) / file.length());
    }

    public void send(FileOutputStream outputStream) {
        if (sending.get()) throw new IllegalStateException("Already sending the file");

        sending.set(true);
        try (FileInputStream fileReader = new FileInputStream(file);
             DataOutputStream output = new DataOutputStream(outputStream)) {
            output.writeUTF(file.getName());
            output.writeLong(file.length());

            byte[] buffer = new byte[BUFFER_SIZE];
            sentCount.set(0);
            int sent;
            while ((sent = fileReader.read(buffer)) != -1) {
                if (!sending.get()) return;
                output.write(buffer);
                sentCount.getAndAdd(sent);
            }
            callback.onSuccess(file);
        } catch (IOException e) {
            sending.set(false);
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
