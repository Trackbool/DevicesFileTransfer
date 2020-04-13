package dft.services.discovery;

import dft.services.ServiceConnectionCallback;

public interface DevicesDiscoveryReceiver {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(DiscoveryProtocolListener.Callback callback);

    void receive();

    void stop();
}
