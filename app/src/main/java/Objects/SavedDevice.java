package Objects;

public class SavedDevice {

    private String mac = "";
    private String name = "";

    public SavedDevice(String mac, String name) {
        this.mac = mac;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
