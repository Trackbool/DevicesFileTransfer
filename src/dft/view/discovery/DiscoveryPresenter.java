package dft.view.discovery;

import dft.domain.model.Device;
import dft.services.discovery.DiscoveryProtocolListener;
import dft.services.discovery.DiscoveryProtocolListenerFactory;
import dft.services.discovery.DiscoveryProtocolSender;
import dft.services.discovery.DiscoveryProtocolSenderFactory;

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
                    public void initializationFailure(Exception e) {
                        view.showError("Discovery error", e.getMessage());
                    }

                    @Override
                    public void discoveryRequestReceived(Device device) {
                        if (!view.getDevicesList().contains(device)) {
                            view.addDevice(device);
                        }
                    }

                    @Override
                    public void discoveryResponseReceived(Device device) {
                        if (!view.getDevicesList().contains(device)) {
                            view.addDevice(device);
                        }
                    }

                    @Override
                    public void discoveryDisconnect(Device device) {
                        view.getDevicesList().remove(device);
                    }
                });
        discoveryListener.start();
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
        try {
            discoverySender.noticeDisconnect();
        } catch (SocketException ignored) {
        }
        discoveryListener.stop();
        view = null;
    }
}
