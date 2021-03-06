package dft.services.discovery;

import java.net.SocketException;

public interface DevicesDiscoveryExecutor {
    void start();

    void stop();

    void discover() throws SocketException;
}
