package com.example.wifimanager;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.InetAddresses;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ekn.gruzer.gaugelibrary.ArcGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import Adapters.ListDevicesAdapter;
import Objects.Device;
import Objects.Vendor;
import Retrofit.RetrofitBuilder;
import Retrofit.RetrofitService;
import Utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//import Utils.NetworkUtil;

import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity {

    List<Device> devices = new ArrayList<>();
    Stack<Device> incomplete = new Stack<>();

    long received;
    long transmitted;
    long rx, tx;
    int progress = 0;

    ExtendedFloatingActionButton btnCheck;
    TextView tvConStatus;
    TextView tvIP;
    TextView tvNumDevice;
    String addressPrefix = "";
    ArcGauge gaugeRx;
    ArcGauge gaugeTx;
    TextView tvRx;
    TextView tvTx;
    ProgressBar progressBar;

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
        setGauge();
        setTextBlock2();
        getTrafficStatus();
        setAddressPrefix();
        new findDevices().execute();
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new findDevices().execute();
            }
        });
    }

    public class findDevices extends AsyncTask<Void, Integer, Void>{

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
                    Log.d("hehe", "" + line);
                    tmp = new Device(line);
                    if(!tmp.getState().equals("FAILED") && !tmp.isRouter()){
                        if(!tmp.getState().equals("INCOMPLETE")) {
                            InetAddress inetAddress = InetAddress.getByName(tmp.getAddress());
                            String hostName = inetAddress.getHostName();
                            if(!hostName.equals(tmp.getAddress())) tmp.setDeviceName(hostName);
                            else tmp.setDeviceName("Không xác định!");
                            devices.add(tmp);
                        } else incomplete.push(tmp);
                        progress+= devices.size();
                        publishProgress(progress);
                    } else {
                        progress++;
                        publishProgress(progress);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < 5; i++){
                getListDevice();
                publishProgress(255 - incomplete.size());
            }
            publishProgress(255);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int progress = values[0];
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
                Log.d("hehe_incomplete", "" + incomplete.get(i).getAddress());
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
        mAdapter.set(devices);
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
        layoutManager = new LinearLayoutManager(getBaseContext());
        rvDevices.setLayoutManager(layoutManager);
        mAdapter = new ListDevicesAdapter();
        rvDevices.setAdapter(mAdapter);
    }

    void getTrafficStatus(){

        //final boolean mStopHandler = false;
        received = TrafficStats.getTotalRxBytes();
        transmitted = TrafficStats.getTotalTxBytes();
        final Handler mHandler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                rx = TrafficStats.getTotalRxBytes() - received;
                tx = TrafficStats.getTotalTxBytes() - transmitted;
                updateGauge(rx, tx);
                Log.d("hehe_status", "" + rx);
                Log.d("hehe_status", "" + tx);
                received = TrafficStats.getTotalRxBytes();
                transmitted = TrafficStats.getTotalTxBytes();
                mHandler.postDelayed(this, 500);
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
        tvRx = findViewById(R.id.activity_main_block1_rx);
        tvTx = findViewById(R.id.activity_main_block1_tx);
        progressBar = findViewById(R.id.activity_main_progress);
    }

    void setGauge(){
        Range range1 = new Range();
        range1.setColor(Color.parseColor("#2828F7"));
        range1.setFrom(0.0);
        range1.setTo(100);

        Range range2 = new Range();
        range2.setColor(Color.parseColor("#2828F7"));
        range2.setFrom(100);
        range2.setTo(300);

        Range range3 = new Range();
        range3.setColor(Color.parseColor("#2828F7"));
        range3.setFrom(300);
        range3.setTo(500);

        gaugeRx.addRange(range1); gaugeTx.addRange(range1);
        gaugeRx.addRange(range2); gaugeTx.addRange(range2);
        gaugeRx.addRange(range3); gaugeTx.addRange(range3);

        gaugeRx.setMinValue(0); gaugeTx.setMinValue(0);
        gaugeRx.setMaxValue(500); gaugeTx.setMaxValue(500);
    }

    void updateGauge(double rx, double tx){
        rx = rx/1000;
        tx = tx/1000;
        rx = (double) Math.round(rx*10)/10;
        tx = (double) Math.round(tx*10)/10;
        rx= rx*2;
        tx=tx*2;
        gaugeRx.setValue(rx);
        gaugeTx.setValue(tx);
        tvRx.setText("Tải xuống: " + rx + "KBps");
        tvTx.setText("Tải lên: " + tx + "KBps");
    }

    void setAddressPrefix(){
        addressPrefix = NetworkUtil.getIPAddress(getBaseContext());
        int length = addressPrefix.length();
        length--;
        while((length >= 0) && addressPrefix.charAt(length)!= '.'){
            addressPrefix = addressPrefix.substring(0, length);
            length--;
        }
        Log.d("hehehe", ""+ addressPrefix);
    }
}