package dft.model;

public class Transfer {
    private Device device;
    private String fileName;
    private int percentage;

    public Transfer(Device device, String fileName, int percentage) {
        this.device = device;
        this.fileName = fileName;
        this.percentage = percentage;
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

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getProgressPercentage() {
        return percentage + "%";
    }
}
