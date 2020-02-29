package dft.model;

import java.net.InetAddress;
import java.util.Objects;

public class Device {
    private String name;
    private String os;
    private InetAddress address;

    public Device(String name, String os, InetAddress address) {
        this.name = name;
        this.os = os;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getOs() {
        return os;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getIpAddress() {
        return address.getHostAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return name.equals(device.name) &&
                os.equals(device.os) &&
                address.equals(device.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, os, address);
    }
}
