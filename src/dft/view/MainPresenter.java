package dft.view;

import dft.model.DeviceProperties;
import dft.services.discovery.*;

import java.net.InetAddress;
import java.net.SocketException;

public class MainPresenter implements MainContract.Presenter {
    private final static int DISCOVERY_SERVICE_PORT = 5000;
    private MainContract.View view;
    private DiscoveryProtocolListener discoveryListener;
    private DiscoveryProtocolSender discoverySender;

    public MainPresenter(MainContract.View view) {
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
                    }
                });
        try {
            discoveryListener.start();
        } catch (SocketException e) {
            this.view.showError("Initialization error", e.getMessage());
            this.view.close();
        }
        discoverySender = DiscoveryProtocolSenderFactory.getDefault(DISCOVERY_SERVICE_PORT);
    }

    @Override
    public void onDiscoverDevicesButtonClicked() {
        try {
            discoverySender.discover();
        } catch (SocketException e) {
            this.view.showError("Discover error", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        this.discoveryListener.stop();
        this.view = null;
    }
}
