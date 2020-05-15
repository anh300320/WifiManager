package Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkUtil {
    public static final int NETWORK_TYPE_INVALID = 0;
    public static final int NETWORK_TYPE_WIFI = 1;
    public static final int NETWORK_TYPE_MOBILE = 2;

    public static int getNetworkStatus(Context context){
        int mNetworkStatus = 0;
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            String type = networkInfo.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                mNetworkStatus = NETWORK_TYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                mNetworkStatus = NETWORK_TYPE_MOBILE;
            }
        } else {
            mNetworkStatus = NETWORK_TYPE_INVALID;
        }
        return mNetworkStatus;
    }

    public static String getNetworkType(Context context){
        int type = getNetworkStatus(context);
        String result = "";
        switch (type){
            case 0:
                result = "Không có kết nối";
                break;
            case 1:
                result = "WiFi";
                break;
            case 2:
                result = "MOBILE";
                break;
        }
        return result;
    }

    public static boolean isNetWorkInvalid(Context context) {
        return getNetworkStatus(context) == NETWORK_TYPE_INVALID;
    }

    public static boolean isWiFi(Context context) {
        return getNetworkStatus(context) == NETWORK_TYPE_WIFI;
    }

    public static boolean isMobile(Context context) {
        return getNetworkStatus(context) == NETWORK_TYPE_MOBILE;
    }

    public static String getIPAddress(Context context) {
        String ipAddress = null;
        if (!isNetWorkInvalid(context)) {
            if (isWiFi(context)) {
                ipAddress = getWIFILocalIpAdress(context);
            } else if (isMobile(context)) {
                ipAddress = getGPRSLocalIpAddress();
            }
        }
        if (TextUtils.isEmpty(ipAddress)) {
            ipAddress = getHostIp();
        }
        if (!TextUtils.isEmpty(ipAddress) &&
                !Patterns.IP_ADDRESS.matcher(ipAddress).matches()) {
            return null;
        }
        return ipAddress;
    }

    public static String getMacAddress(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getMacAddress();
    }

    public static String getGPRSLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                        return inetAddress.getHostAddress();
                }
            }
        } catch (SocketException ex) {
            Log.e("GPRS IpAddress", ex.toString());
        }
        return null;
    }

    public static String getWIFILocalIpAdress(Context mContext) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return formatIpAddress(ipAddress);
    }

    public static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = ipAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String formatIpAddress(int ipAdress) {
        if (ipAdress == 0) {
            return null;
        }
        return (ipAdress & 0xFF) + "." +
                ((ipAdress >> 8) & 0xFF) + "." +
                ((ipAdress >> 16) & 0xFF) + "." +
                (ipAdress >> 24 & 0xFF);
    }
}
