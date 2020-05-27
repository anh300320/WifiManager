package com.example.wifimanager;

import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.sccomponents.gauges.library.ScArcGauge;
import com.sccomponents.gauges.library.ScGauge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Stack;

import Adapters.ListDevicesAdapter;
import Objects.Device;
import Objects.ListDevices;
import Objects.Vendor;
import Retrofit.RetrofitBuilder;
import Retrofit.RetrofitService;
import Utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import Utils.NetworkUtil;

public class MainActivity extends AppCompatActivity {

    ListDevices devices;
    Stack<Device> incomplete = new Stack<>();

    long received;
    long transmitted;
    float rx, tx;
    float maxRx = 3000;
    float maxTx = 3000;
    int max1 = 1000, max2 = 200000, max3 = 1000000;
    int progress = 0;
    float valueRx;
    float valueTx;
    String sRx = "Bps";
    String sTx = "Bps";

    //làm mượt gauge
    int intervalValue, intervalGauge;
    long currentValueRx, currentValueTx;
    long gaugeValueRx, gaugeValueTx;

    ExtendedFloatingActionButton btnCheck;
    TextView tvConStatus;
    TextView tvIP;
    TextView tvNumDevice;
    String addressPrefix = "";
    ScArcGauge gaugeRx;
    ScArcGauge gaugeTx;
    TextView tvRx;
    TextView tvTx;
    ImageView indicatorRx;
    ImageView indicatorTx;
    ProgressBar progressBar;
    FindDevices findDevices;

    RecyclerView rvDevices;
    ListDevicesAdapter mAdapter;
    RecyclerView.LayoutManager layoutManager;

    RetrofitService retrofitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitService = RetrofitBuilder.build("https://api.maclookup.app/");

        findViewById();
        setRecyclerView();
        setGauge(maxRx, maxTx);
        setTextBlock2();
        getTrafficStatus();
        setAddressPrefix();
        getRouter();
        findDevices = new FindDevices();
        findDevices.execute();
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FindDevices().execute();
            }
        });
    }

    public class FindDevices extends AsyncTask<Void, Integer, Void>{

        @Override
        protected void onCancelled() {
            progressBar.setVisibility(View.GONE);
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            pingAll();

            try {
                devices.clear();
                incomplete.clear();

                Process process = Runtime.getRuntime().exec("ip neigh");
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                Device tmp;
                while((line = br.readLine()) != null) {
                    if(isCancelled()) return null;
                    Log.d("hehe", "" + line);
                    tmp = new Device(line);
                    tmp.setRouter(tmp.getMac().equals(devices.getRouterMac()));
                    if(!tmp.getState().equals("FAILED") && !tmp.isRouter()){
                        if(!tmp.getState().equals("INCOMPLETE")) {
                            InetAddress inetAddress = InetAddress.getByName(tmp.getAddress());
                            String hostName = inetAddress.getHostName();
                            if(!hostName.equals(tmp.getAddress())) tmp.setDeviceName(hostName);
                            else tmp.setDeviceName("Thiết bị");
                            devices.add(tmp);
                        } else incomplete.push(tmp);
                        progress+= devices.size()*2;
                        publishProgress(progress);
                    } else {
                        if(isCancelled()) return null;
                        progress+=2;
                        publishProgress(progress);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            for(int i = 0; i < 2; i++){
//                if(isCancelled()) return null;
//                getListDevice();
//                progress++;
//                publishProgress(progress);
//            }
            publishProgress(510);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
            progress = progress * 100 / 510;
            progressBar.setProgress(progress);
            updateListDevices();
            super.onProgressUpdate(values);
        }
    }

    public void getListDevice(){
        int size = incomplete.size();
        for(int i = 0; i < size; i++){
            try {
                InetAddress inetAddress = InetAddress.getByName(incomplete.get(i).getAddress());
                //Log.d("hehe_incomplete", "" + incomplete.get(i).getAddress());
                if(inetAddress.isReachable(5000)) {
                    incomplete.get(i).setDeviceName(inetAddress.getHostName());
                    incomplete.remove(i);
                    devices.add(incomplete.get(i));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateListDevices(){
        getDevicesVendor();
        mAdapter.set(devices.getAll());
        mAdapter.notifyDataSetChanged();
        tvNumDevice.setText(""+mAdapter.getItemCount());
    }

    void getDevicesVendor(){
        int size = devices.size();
        for(int i = 0; i < size; i++){
            final Device device = devices.get(i);
            String macAddress = device.getMac();

            Call<Vendor> getVendor = retrofitService.getVendor(macAddress);
            getVendor.enqueue(new Callback<Vendor>() {
                @Override
                public void onResponse(Call<Vendor> call, Response<Vendor> response) {
                    Vendor deviceVendor = response.body();
                    if(deviceVendor != null) device.setVendor("" + deviceVendor.getCompany());
                    device.setType();
                    mAdapter.set(devices.getAll());
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<Vendor> call, Throwable t) {}
            });
        }

    }

    void pingAll(){
        for(int i = 1; i < 256; i++){
            String cmd = "ping -c 10 "+ addressPrefix + i;
            try {
                Process process = Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void setTextBlock2(){
        tvConStatus.setText(""+NetworkUtil.getNetworkType(getBaseContext()));
        tvNumDevice.setText(""+mAdapter.getItemCount());
        tvIP.setText("" +NetworkUtil.getIPAddress(getBaseContext()));
    }

    void setRecyclerView(){
        rvDevices.setItemViewCacheSize(20);
        rvDevices.setDrawingCacheEnabled(true);
        rvDevices.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvDevices.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getBaseContext());
        rvDevices.setLayoutManager(layoutManager);
        mAdapter = new ListDevicesAdapter();
        rvDevices.setAdapter(mAdapter);
    }

    void getTrafficStatus(){

        intervalValue = 1000;
        intervalGauge = 10;
        //final boolean mStopHandler = false;
        received = TrafficStats.getTotalRxBytes();
        transmitted = TrafficStats.getTotalTxBytes();
        gaugeValueRx = 0;
        gaugeValueTx = 0;
        final Handler mHandler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(intervalValue != 0) {
                    gaugeValueRx += (currentValueRx - gaugeValueRx) * intervalGauge / intervalValue;
                    gaugeValueTx += (currentValueTx - gaugeValueTx) * intervalGauge / intervalValue;
                }
                if(intervalValue <= 0){
                    intervalValue = 1000;
                    Log.d("hehe_status", "Received: "+ currentValueRx);
                    Log.d("hehe_status", "Transmitted: " + currentValueTx);
                    currentValueRx = TrafficStats.getTotalRxBytes() - received;
                    currentValueTx = TrafficStats.getTotalTxBytes() - transmitted;
                    received = TrafficStats.getTotalRxBytes();
                    transmitted = TrafficStats.getTotalTxBytes();
                } else intervalValue -= intervalGauge;
                updateGauge(gaugeValueRx, gaugeValueTx);
                mHandler.postDelayed(this, intervalGauge);
//                rx = TrafficStats.getTotalRxBytes() - received;
//                tx = TrafficStats.getTotalTxBytes() - transmitted;
//                updateGauge(rx*2, tx*2);
//                Log.d("hehe_status", "" + rx);
//                Log.d("hehe_status", "" + tx);
//                received = TrafficStats.getTotalRxBytes();
//                transmitted = TrafficStats.getTotalTxBytes();
//                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.post(runnable);
    }

    void findViewById(){
        btnCheck = findViewById(R.id.activity_main_btn_check);
        rvDevices = findViewById(R.id.activity_main_recyclerview_devices);
        tvConStatus = findViewById(R.id.activity_main_connectto);
        tvIP = findViewById(R.id.activity_main_yourip);
        tvNumDevice = findViewById(R.id.activity_main_numberdevices);
        gaugeRx = findViewById(R.id.activity_main_gauge_rx);
        gaugeTx = findViewById(R.id.activity_main_gauge_tx);
        tvRx = findViewById(R.id.activity_main_text_rx);
        tvTx = findViewById(R.id.activity_main_text_tx);
        progressBar = findViewById(R.id.activity_main_progress);
        indicatorRx = findViewById(R.id.activity_main_indicator_rx);
        indicatorTx = findViewById(R.id.activity_main_indicator_tx);
        progressBar.setVisibility(View.GONE);
    }

    void setGauge(float maxRx, float maxTx){
        indicatorRx.setPivotX(30f);
        indicatorRx.setPivotY(30f);
        indicatorTx.setPivotX(30f);
        indicatorTx.setPivotY(30f);

        gaugeRx.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning) {
                float angle = -180 + highValue * 180 / 100;
                indicatorRx.setRotation(angle);

                valueRx =(float) Math.round(valueRx * 100) / 100;
                tvRx.setText("Tải xuống: " +  valueRx + " " + sRx);
            }
        });

        gaugeTx.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning) {
                float angle = -180 + highValue * 180 / 100;
                indicatorTx.setRotation(angle);

                valueTx =(float) Math.round(valueTx * 100) / 100;
                tvTx.setText("Tải lên: " + valueTx + " " + sTx);
            }
        });
    }

    void updateGauge(float rx, float tx){

        float pRx = 0, pTx = 0;

        valueRx = rx;
        valueTx = tx;
        if(valueRx < 1000) sRx = "Bps";
        if(valueTx < 1000) sTx = "Bps";
        if(valueRx > 1000) {valueRx/=1000; sRx = "KBps";}
        if(valueRx > 1000) {valueRx/=1000; sRx = "MBps";}
        if(valueTx > 1000) {valueTx/=1000; sTx = "KBps";}
        if(valueTx > 1000) {valueTx/=1000; sTx = "MBps";}

        if(rx <= max1) pRx = rx/max1 * (200/7);
        if(max1 < rx && rx <= max2) pRx = 200/7 + (rx - max1) / (max2 - max1) * 400/7;
        if(max2 < rx) pRx = 3 + 600/7 + (rx-max2) / (max3 - max2) * 100/7;

        if(tx <= max1) pTx = tx/max1 * (200/7);
        if(max1 < tx && tx <= max2) pTx = 200/7 + (tx - max1) / (max2 - max1) * 400/7;
        if(max2 < tx) pTx =3 + 600/7 + (tx-max2) / (max3 - max2) * 100/7;

        gaugeRx.setHighValue(pRx);
        gaugeTx.setHighValue(pTx);
    }

    void setAddressPrefix(){
        addressPrefix = NetworkUtil.getIPAddress(getBaseContext());
        if(addressPrefix == null) return;
        int length = addressPrefix.length();
        length--;
        while((length >= 0) && addressPrefix.charAt(length)!= '.'){
            addressPrefix = addressPrefix.substring(0, length);
            length--;
        }
        Log.d("hehehe", ""+ addressPrefix);
    }

    void getRouter(){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ip neigh");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            Device tmp;
            while((line = br.readLine()) != null){
                tmp = new Device(line);
                if(tmp.isRouter()) devices.setRouterMac(tmp.getMac());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}