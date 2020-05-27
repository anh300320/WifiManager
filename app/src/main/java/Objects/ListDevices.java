package Objects;

import java.util.ArrayList;
import java.util.List;

public class ListDevices {

    private static List<Device> devices = new ArrayList<>();
    private static String routerMac = "";

    public static List<Device> getAll(){
        return devices;
    }

    public static void clear(){
        devices.clear();
    }

    public static int size(){
        return devices.size();
    }

    public static Device get(int position){
        return devices.get(position);
    }

    public static void add(Device device){
        int size = devices.size();
        for(int i = 0; i < size; i++){
            Device tmp = devices.get(i);
            if(tmp.getAddress().equals(device.getAddress())) return;
        }
        devices.add(device);
    }

    public static String getRouterMac() {
        return routerMac;
    }

    public static void setRouterMac(String mac) {
        routerMac = mac;
    }
}
