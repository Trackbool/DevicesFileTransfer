package dft.services.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileSender {
    private final static int BUFFER_SIZE = 8192;
    private final File file;
    private final int port;
    private Callback callback;
    private final AtomicBoolean sending;

    public FileSender(File file, int port) {
        this.file = file;
        this.port = port;
        sending = new AtomicBoolean(false);
    }

    public FileSender(File file, int port, Callback callback) {
        this(file, port);
        this.callback = callback;
    }

    public boolean isSending() {
        return sending.get();
    }

    public void send(InetAddress address) {
        try (FileInputStream fileReader = new FileInputStream(file);
             Socket socket = new Socket(address, port);
             OutputStream out = socket.getOutputStream()) {

            sending.set(true);
            byte[] buffer = new byte[BUFFER_SIZE];
            while (fileReader.read() != -1) {
                if (!sending.get()) return;
                out.write(buffer);
            }
            callback.onSuccess();
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

        void onSuccess();
    }
}
