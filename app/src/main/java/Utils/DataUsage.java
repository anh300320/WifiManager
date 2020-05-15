package Utils;

import android.net.TrafficStats;

public class DataUsage {

    static private TrafficStats trafficStats = new TrafficStats();
    static public long getReceivedBytes(){
        return TrafficStats.getTotalRxBytes();
    }

}
