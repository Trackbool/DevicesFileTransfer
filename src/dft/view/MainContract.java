package dft.view;

import dft.model.Device;

public interface MainContract {
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
