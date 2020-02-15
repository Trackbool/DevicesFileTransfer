package dft.services.discovery;

import dft.util.SystemUtils;
import dft.model.DeviceProperties;

public class DiscoveryProtocolListenerFactory {
    public static DiscoveryProtocolListener getDefault(int port) {
        DeviceProperties deviceProperties = getDeviceProperties();
        return new DiscoveryProtocolListener(new NetworkDataProvider(), deviceProperties, port);
    }

    public static DiscoveryProtocolListener getDefault(int port, DiscoveryProtocolListener.Callback callback) {
        DeviceProperties deviceProperties = getDeviceProperties();
        return new DiscoveryProtocolListener(new NetworkDataProvider(), deviceProperties, port, callback);
    }

    private static DeviceProperties getDeviceProperties() {
        String systemName = SystemUtils.getSystemName();
        String systemOs = System.getProperty("os.name");
        return new DeviceProperties(systemName, systemOs);
    }
}
