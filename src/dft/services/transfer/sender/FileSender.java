package dft.services.transfer.sender;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class FileSender {
    private final static int BUFFER_SIZE = 8192;
    private final File file;
    private final AtomicBoolean sending;
    private Callback callback;
    private AtomicLong sentCount;

    public FileSender(File file) {
        this.file = file;
        this.sending = new AtomicBoolean(false);
        this.sentCount = new AtomicLong(0);
    }

    public FileSender(File file, Callback callback) {
        this(file);
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

    public void send(OutputStream outputStream) {
        if (sending.get()) throw new IllegalStateException("Already sending the file");

        sending.set(true);
        if (callback != null)
            callback.onStart();
        try (FileInputStream fileReader = new FileInputStream(file);
             DataOutputStream output = new DataOutputStream(outputStream)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            sentCount.set(0);
            int sent;
            int currentPercentage = 0;
            while ((sent = fileReader.read(buffer, 0, buffer.length)) != -1) {
                if (!sending.get() || Thread.interrupted()) return;
                output.write(buffer, 0, sent);
                sentCount.getAndAdd(sent);

                int sentPercentage = getSentPercentage();
                if (callback != null && currentPercentage < sentPercentage) {
                    currentPercentage = sentPercentage;
                    callback.onProgressUpdated();
                }
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
        void onStart();

        void onFailure(Exception e);

        void onProgressUpdated();

        void onSuccess(File file);
    }
}
