package com.example.wifimanager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import org.w3c.dom.Text;

import Utils.NetworkUtil;

public class SignalActivity extends AppCompatActivity {

    WifiManager wifiManager;
    TextView tvFrequency;
    TextView tvValue;
    TextView tvRx;
    TextView tvTx;
    TextView tvIP;
    final Handler mHandler = new Handler(Looper.getMainLooper());
    ImageView imgSignal;
    Button btnRouter;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        findViewById();
        setText();
        mHandler.post(measure);
        String addressPrefix = getIntent().getStringExtra("prefix");
        btnRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://" + addressPrefix + "1/";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        UnifiedNativeAdView adView = findViewById(R.id.activity_signal_adview);
                        TextView headLineView = adView.findViewById(R.id.activity_signal_ad_headline);
                        headLineView.setText(unifiedNativeAd.getHeadline());
                        adView.setHeadlineView(headLineView);

                        adView.setNativeAd(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener(){
                    @Override
                    public void onAdFailedToLoad(int i) {
                        Log.d("hehe_load_ad_failed", "onAdFailedToLoad: ");
                        super.onAdFailedToLoad(i);
                    }
                })
                .build();


    }

    void populateUnifiedNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView adView){

    }

    Thread measure = new Thread(){
        @Override
        public void run() {
            int value = wifiManager.getConnectionInfo().getRssi();
            value = WifiManager.calculateSignalLevel(value, 1001);
            Log.d("hehe_measure", "" + value);
            float val = value;
            val = (float) 300* (val / 1000);
            Log.d("hehe_measure", "" + val);

            int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());

            imgSignal.getLayoutParams().height = dimensionInDp;
            imgSignal.requestLayout();
            value = value / 10;
            tvValue.setText("" + value + "%");
            mHandler.postDelayed(measure, 20);

            String signalLevel = "Yếu";
            if(value > 50) signalLevel = "Trung bình";
            if(value > 80) signalLevel = "Tốt";
            if(value > 90) signalLevel = "Mạnh";
            tvTx.setText("" + signalLevel);
        }
    };
    void findViewById(){
        tvRx = findViewById(R.id.activity_signal_text_receive_speed);
        tvTx = findViewById(R.id.activity_signal_text_transmit_speed);
        tvIP = findViewById(R.id.activity_signal_text_ip);
        tvValue = findViewById(R.id.activity_signal_text_value);
        tvFrequency = findViewById(R.id.activity_signal_text_frequency);
        imgSignal = findViewById(R.id.activity_signal_value);
        btnRouter = findViewById(R.id.activity_signal_button_router);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    void setText(){
        tvIP.setText("" + NetworkUtil.getIPAddress(getBaseContext()));
        tvRx.setText("" + NetworkUtil.getLinkSpeed(getBaseContext()) + " Mbps");
        tvFrequency.setText("WiFi " + NetworkUtil.getFrequency(getBaseContext()) + " GHz" );
    }
}
