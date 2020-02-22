package dft.view.discovery;

import dft.model.Device;

public interface DiscoveryContract {
    interface View {
        void addDevice(Device device);

        void showError(String title, String message);

        void clearDevicesList();

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDiscoverDevicesButtonClicked();

        void onDestroy();
    }
}
