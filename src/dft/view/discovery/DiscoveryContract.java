package dft.view.discovery;

import dft.domain.model.Device;

import java.util.List;

public interface DiscoveryContract {
    interface View {
        void addDevice(Device device);

        void showError(String title, String message);

        List<Device> getDevicesList();

        void clearDevicesList();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDiscoverDevicesEvent();

        void onDestroy();
    }
}
