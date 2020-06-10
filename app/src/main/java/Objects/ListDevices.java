package Objects;

import java.util.ArrayList;
import java.util.List;

public class ListDevices {

    private List<Device> devices = new ArrayList<>();
    private  String routerMac = "";

    public ListDevices() {
    }

    public List<Device> getAll(){
        return devices;
    }

    public void clear(){
        devices.clear();
    }

    public int size(){
        return devices.size();
    }

    public Device get(int position){
        return devices.get(position);
    }

    public void set(int position, Device device){
        devices.get(position).set(device);
    }

    public void add(Device device){
        int size = devices.size();
        for(int i = 0; i < size; i++){
            Device tmp = devices.get(i);
            if(tmp.getAddress().equals(device.getAddress())) return;
        }
        devices.add(device);
    }

    public String getRouterMac() {
        return routerMac;
    }

    public void setRouterMac(String mac) {
        routerMac = mac;
    }
}
