package dft.services.transfer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileReceiverListener {
    private ServerSocket serverSocket;
    private final int port;
    private final AtomicBoolean listening;

    public FileReceiverListener(int port) {
        this.port = port;
        this.listening = new AtomicBoolean(false);
    }

    public void listen() {
        listening.set(true);
        try {
            serverSocket = new ServerSocket(port);
            while (listening.get()) {
                Socket socket = serverSocket.accept();
                //new Thread(() -> new FileReceiver()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listening.set(false);
        }
    }

    public void stopListening() {
        listening.set(false);
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}
