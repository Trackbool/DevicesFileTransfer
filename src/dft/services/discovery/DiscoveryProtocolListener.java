package dft.services.discovery;

import com.google.gson.Gson;
import dft.model.DeviceProperties;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscoveryProtocolListener {
    private final NetworkDataProvider networkDataProvider;
    private final String devicePropertiesJson;
    private final int port;
    private Callback callback;
    private DatagramSocket serverSocket;
    private AtomicBoolean listening;

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     DeviceProperties deviceProperties,
                                     int port) {
        this.networkDataProvider = networkDataProvider;
        this.devicePropertiesJson = new Gson().toJson(deviceProperties);
        this.port = port;
        this.listening = new AtomicBoolean(false);
    }

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     DeviceProperties deviceProperties,
                                     int port, Callback callback) {
        this(networkDataProvider, deviceProperties, port);
        this.callback = callback;
    }

    public void start() throws SocketException {
        if (this.listening.get()) {
            throw new IllegalStateException("Listener already listening");
        }

        this.serverSocket = new DatagramSocket(port);
        this.listening.set(true);

        new Thread(this::listen).start();
    }

    public void stop() {
        listening.set(false);
        this.serverSocket.close();
    }

    private void listen() {
        while (listening.get()) {
            try {
                DatagramPacket receivePacket = receiveRequest();
                InetAddress senderAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();

                if (callback != null) {
                    this.notifyRequestReceivedIfNotEqualsCurrentDeviceAddress(senderAddress, senderPort);
                }

                sendResponse(senderAddress, senderPort);
            } catch (IOException ignored) {
                listening.set(false);
            }
        }
    }

    private DatagramPacket receiveRequest() throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        this.serverSocket.receive(receivePacket);
        return receivePacket;
    }

    private void notifyRequestReceivedIfNotEqualsCurrentDeviceAddress(InetAddress address, int port) {
        if (!this.receivedIpIsCurrentDeviceIp(address)) {
            callback.discoveryRequestReceived(address, port);
        }
    }

    private void sendResponse(InetAddress address, int port) throws IOException {
        byte[] sendData = devicePropertiesJson.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        serverSocket.send(sendPacket);
    }

    private boolean receivedIpIsCurrentDeviceIp(InetAddress receivedAddress) {
        Set<InetAddress> currentDeviceAddresses = networkDataProvider.getDeviceIpv4Addresses();
        String receivedIp = receivedAddress.getHostAddress();
        for (InetAddress a : currentDeviceAddresses) {
            String currentDeviceIp = a.getHostAddress();
            if (receivedIp.equals(currentDeviceIp)) {
                return true;
            }
        }
        return false;
    }

    public interface Callback {
        void discoveryRequestReceived(InetAddress senderAddress, int senderPort);
    }
}
