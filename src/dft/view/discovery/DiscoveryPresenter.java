package dft.view.discovery;

import dft.model.Device;
import dft.model.DeviceProperties;
import dft.services.discovery.*;

import java.net.InetAddress;
import java.net.SocketException;

public class DiscoveryPresenter implements DiscoveryContract.Presenter {
    private final static int DISCOVERY_SERVICE_PORT = 5000;
    private DiscoveryContract.View view;
    private DiscoveryProtocolListener discoveryListener;
    private DiscoveryProtocolSender discoverySender;

    public DiscoveryPresenter(DiscoveryContract.View view) {
        this.view = view;
    }

    @Override
    public void onViewLoaded() {
        discoveryListener = DiscoveryProtocolListenerFactory
                .getDefault(DISCOVERY_SERVICE_PORT, new DiscoveryProtocolListener.Callback() {
                    @Override
                    public void discoveryRequestReceived(InetAddress senderAddress, int senderPort) {
                        System.out.println("Received request from: " + senderAddress.getHostAddress());
                    }

                    @Override
                    public void discoveryResponseReceived(InetAddress senderAddress, int senderPort, DeviceProperties deviceProperties) {
                        System.out.println("Received response: " + senderAddress.getHostAddress() + " - " + deviceProperties.getName() + ", " + deviceProperties.getOs());
                        String deviceName = deviceProperties.getName();
                        String os = deviceProperties.getOs();
                        view.addDevice(new Device(deviceName, os, senderAddress));
                    }
                });
        try {
            discoveryListener.start();
        } catch (SocketException e) {
            view.showError("Initialization error", e.getMessage());
            view.close();
        }
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(DISCOVERY_SERVICE_PORT);
        discoverDevices();
    }

    @Override
    public void onDiscoverDevicesEvent() {
        discoverDevices();
    }

    private void discoverDevices() {
        try {
            discoverySender.discover();
            view.clearDevicesList();
        } catch (SocketException e) {
            view.showError("Discover error", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        discoveryListener.stop();
        view = null;
    }
}
