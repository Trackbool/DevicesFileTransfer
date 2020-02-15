package dft.services.discovery;

import dft.model.DeviceProperties;

public class DiscoveryProtocolSenderFactory {
    public static DiscoveryProtocolSender getDefault(int port) {
        return new DiscoveryProtocolSender(new NetworkDataProvider(), port);
    }
}
