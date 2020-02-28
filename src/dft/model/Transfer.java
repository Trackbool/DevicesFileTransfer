package dft.model;

public class Transfer {
    private Device device;
    private String fileName;
    private int progress;

    public Transfer(Device device, String fileName, int progress) {
        this.device = device;
        this.fileName = fileName;
        this.progress = progress;
    }

    public Device getDevice() {
        return device;
    }

    public String getDeviceName() {
        return device.getName();
    }

    public String getDeviceIpAddress() {
        return device.getIpAddress();
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFileName() {
        return fileName;
    }

    public int getProgress() {
        return progress;
    }

    public String getProgressPercentage() {
        return progress + "%";
    }
}
