package com.example.wifimanager;

import android.app.Activity;
import android.app.admin.DeviceAdminService;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
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
import Objects.OnItemClickListener;
import Objects.Vendor;
import Retrofit.RetrofitBuilder;
import Retrofit.RetrofitService;
import Utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//import Utils.NetworkUtil;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EXAMPLE = 0x9345;

    Stack<Device> incomplete = new Stack<>();

    long received;
    long transmitted;
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
    ViewGroup vgBlock2;

    RecyclerView rvDevices;
    ListDevicesAdapter devices;
    RecyclerView.LayoutManager layoutManager;

    RetrofitService retrofitService;

    private AdView mAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrofitService = RetrofitBuilder.build("https://api.maclookup.app/");

        if(SaveFileManager.isNull()) SaveFileManager.getSavedDevice(getBaseContext());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("hehe_ad", "onInitializationComplete: ");
            }
        });

        mAd = findViewById(R.id.activity_main_adview);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAd.loadAd(adRequest);

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
        vgBlock2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignalActivity.class);
                intent.putExtra("prefix", addressPrefix);
                startActivity(intent);
            }
        });
    }

    public class FindDevices extends AsyncTask<Void, Device, Void>{

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
                            SaveFileManager.getSavedName(tmp);
                            InetAddress inetAddress = InetAddress.getByName(tmp.getAddress());
                            String hostName = inetAddress.getHostName();
                            if(!hostName.equals(tmp.getAddress())) tmp.setDeviceName(hostName);
                            else tmp.setDeviceName("Thiết bị");
                            publishProgress(tmp);
                        } else incomplete.push(tmp);
                        progress+= devices.size()*2;
                    } else {
                        if(isCancelled()) return null;
                        progress+=2;
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
            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Device... devicess) {
            if(devicess.length != 0){
                devices.add(devicess[0]);
                devices.notifyItemInserted(devices.size() - 1);
                getDevicesVendor(devices.size() - 1);
            } else {
                devices.notifyDataSetChanged();
            }
            tvNumDevice.setText(""+devices.getItemCount());
            super.onProgressUpdate(devicess);
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

    void getDevicesVendor(int position){
            final Device device = devices.get(position);
            String macAddress = device.getMac();
            int pos = position;
            Call<Vendor> getVendor = retrofitService.getVendor(macAddress);
            getVendor.enqueue(new Callback<Vendor>() {
                @Override
                public void onResponse(Call<Vendor> call, Response<Vendor> response) {
                    Vendor deviceVendor = response.body();
                    if(deviceVendor != null) device.setVendor("" + deviceVendor.getCompany());
                    device.setType();
                    devices.update(pos, device);
                    devices.notifyItemChanged(pos);
                }

                @Override
                public void onFailure(Call<Vendor> call, Throwable t) {}
            });

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
        tvNumDevice.setText(""+devices.getItemCount());
        tvIP.setText("" +NetworkUtil.getIPAddress(getBaseContext()));
    }

    void setRecyclerView(){
        rvDevices.setItemViewCacheSize(20);
        rvDevices.setDrawingCacheEnabled(true);
        rvDevices.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rvDevices.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getBaseContext());
        rvDevices.setLayoutManager(layoutManager);
        devices = new ListDevicesAdapter();
        devices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent intent = new Intent(getBaseContext(), SaveDeviceActivity.class);
                intent.putExtra("device", devices.get(position));
                intent.putExtra("position", position);
                startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
            }
        });
        rvDevices.setAdapter(devices);
    }

    void getTrafficStatus(){

        intervalValue = 1000;
        intervalGauge = 50;
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
                    if(TrafficStats.getTotalRxBytes() - received > (currentValueRx / 10))
                        currentValueRx = TrafficStats.getTotalRxBytes() - received;
                    if(TrafficStats.getTotalTxBytes() - transmitted > (currentValueTx / 10))
                        currentValueTx = TrafficStats.getTotalTxBytes() - transmitted;
                    received = TrafficStats.getTotalRxBytes();
                    transmitted = TrafficStats.getTotalTxBytes();
                } else intervalValue -= intervalGauge;
                updateGauge(gaugeValueRx, gaugeValueTx);
                mHandler.postDelayed(this, intervalGauge);
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
        vgBlock2 = findViewById(R.id.activity_main_block2);
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
                tvRx.setText("" +  valueRx + " " + sRx);
            }
        });

        gaugeTx.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(ScGauge gauge, float lowValue, float highValue, boolean isRunning) {
                float angle = -180 + highValue * 180 / 100;
                indicatorTx.setRotation(angle);

                valueTx =(float) Math.round(valueTx * 100) / 100;
                tvTx.setText("" + valueTx + " " + sTx);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item_measure_signal:
                Intent intent = new Intent(getBaseContext(), SignalActivity.class);
                intent.putExtra("prefix", addressPrefix);
                startActivity(intent);
                break;
            case R.id.menu_item_manage_router:
                String url = "http://" + addressPrefix + "1/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EXAMPLE) {
            if(resultCode == Activity.RESULT_OK){
                int position = data.getIntExtra("result", -1);
                if(position != -1){
                    SaveFileManager.getSavedName(devices.get(position));
                    Device device = devices.get(position);
                    //Log.d("hehe_onResult", device.getSavedName());
                    devices.notifyItemChanged(position);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("hehe", "onActivityResult: ");
                //Write your code if there's no result
            }
        }
    }
}