package dft.model;

public class Device {
    private String name;
    private String os;
    private String ipAddress;

    public Device(String name, String os, String ipAddress) {
        this.name = name;
        this.os = os;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
