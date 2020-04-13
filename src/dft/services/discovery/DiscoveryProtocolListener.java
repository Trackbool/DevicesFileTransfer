package dft.services.discovery;

import com.google.gson.Gson;
import dft.domain.model.Device;
import dft.domain.model.DeviceFactory;
import dft.domain.model.DeviceProperties;
import dft.domain.model.DiscoveryOperation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiscoveryProtocolListener {
    private final NetworkDataProvider networkDataProvider;
    private final int port;
    private Callback callback;
    private DatagramSocket serverSocket;
    private AtomicBoolean listening;

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     int port) {
        this.networkDataProvider = networkDataProvider;
        this.port = port;
        this.listening = new AtomicBoolean(false);
    }

    public DiscoveryProtocolListener(NetworkDataProvider networkDataProvider,
                                     int port, Callback callback) {
        this(networkDataProvider, port);
        this.callback = callback;
    }

    public void start() {
        if (listening.get()) {
            throw new IllegalStateException("Listener already listening");
        }

        try {
            serverSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            if (callback != null) {
                callback.initializationFailure(e);
            }
            return;
        }
        listening.set(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                listen();
            }
        }).start();
    }

    public void stop() {
        listening.set(false);
        serverSocket.close();
    }

    private void listen() {
        while (listening.get()) {
            try {
                DatagramPacket receivePacket = receiveRequest();
                InetAddress senderAddress = receivePacket.getAddress();
                String receivedMessage = new String(receivePacket.getData(),
                        receivePacket.getOffset(),
                        receivePacket.getLength());

                if (receivedIpIsCurrentDeviceIp(senderAddress)) {
                    continue;
                }

                DiscoveryOperation discoveryOperation;
                try {
                    discoveryOperation = new Gson()
                            .fromJson(receivedMessage, DiscoveryOperation.class);
                } catch (Exception e) {
                    continue;
                }

                String operation = discoveryOperation.getName();
                DeviceProperties deviceProperties = discoveryOperation.getDeviceProperties();
                if (operation.equals("discovery")) {
                    if (callback != null) {
                        notifyDiscoveryRequest(senderAddress, deviceProperties);
                    }
                    sendResponse(senderAddress);
                } else if (operation.equals("response")) {
                    if (callback != null) {
                        notifyDiscoveryResponse(senderAddress, deviceProperties);
                    }
                } else {
                    if (callback != null) {
                        notifyDiscoveryDisconnect(senderAddress, deviceProperties);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    private DatagramPacket receiveRequest() throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        return receivePacket;
    }

    private void sendResponse(InetAddress senderAddress) throws IOException {
        DiscoveryOperation discoveryOperation =
                new DiscoveryOperation("response", DeviceFactory.getCurrentDeviceProperties());
        byte[] sendData = new Gson().toJson(discoveryOperation).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, senderAddress, port);
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

    private void notifyDiscoveryRequest(InetAddress senderAddress, DeviceProperties deviceProperties) {
        String name = deviceProperties.getName();
        String os = deviceProperties.getOs();
        Device device = new Device(name, os, senderAddress);
        callback.discoveryRequestReceived(device);
    }

    private void notifyDiscoveryResponse(InetAddress senderAddress, DeviceProperties deviceProperties) {
        String name = deviceProperties.getName();
        String os = deviceProperties.getOs();
        Device device = new Device(name, os, senderAddress);
        callback.discoveryResponseReceived(device);
    }

    private void notifyDiscoveryDisconnect(InetAddress senderAddress, DeviceProperties deviceProperties) {
        String name = deviceProperties.getName();
        String os = deviceProperties.getOs();
        Device device = new Device(name, os, senderAddress);
        callback.discoveryDisconnect(device);
    }

    public interface Callback {
        void initializationFailure(Exception e);

        void discoveryRequestReceived(Device device);

        void discoveryResponseReceived(Device device);

        void discoveryDisconnect(Device device);
    }
}
