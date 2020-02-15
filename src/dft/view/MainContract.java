package dft.view;

public interface MainContract {
    interface View {
        void showError(String title, String message);

        void close();
    }

    interface Presenter {
        void onViewLoaded();

        void onDiscoverDevicesButtonClicked();

        void onDestroy();
    }
}
