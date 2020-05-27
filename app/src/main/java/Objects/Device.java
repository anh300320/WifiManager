package Objects;

import android.util.Log;

public class Device {

    private String address;
    private String name;
    private String lladdress;
    private String mac;
    private String state;
    private String deviceName = "Thiết bị";
    private String vendor = "";
    private String type = "laptop";
    private boolean isRouter = false;
    private boolean isRouter2 = false;

    public Device(String str){
        String[] s1 = str.split("\\s");
        int size = s1.length;
        address = s1[0];
        state = s1[size-1];
        if(!s1[size-2].equals("router")) mac = s1[size-2];
        else {
            mac = s1[size - 3];
            isRouter = true;
        }
    }

    public void show(){
        Log.d("hehe", " -Device:" + deviceName + " -Address:" + address + " -State:" + state);
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getLladdress() {
        return lladdress;
    }

    public String getMac() {
        return mac;
    }

    public String getState() {
        return state;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLladdress(String lladdress) {
        this.lladdress = lladdress;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isRouter() {
        return isRouter;
    }

    public void setRouter(boolean router) {
        isRouter = router;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getType(){
        return type;
    }

    public void setType(){
        String vendor = this.vendor.toLowerCase();
        if(vendor.contains("samsung") || vendor.contains("huawei") || vendor.contains("xiaomi")) type = "smartphone";
        if(vendor.contains("hon hai") || vendor.contains("intel")) type = "pc";
    }

    public boolean isRouter2() {
        return isRouter2;
    }

    public void setRouter2(boolean router2) {
        isRouter2 = router2;
    }
}
