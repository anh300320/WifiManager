package com.example.wifimanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;

import Dialogs.SaveDeviceDialog;
import Objects.Device;
import Objects.OnSavedDevice;

public class SaveDeviceActivity extends AppCompatActivity {

    private Device device;
    private int position;
    Button btnSave;
    TextView tvDeviceName;
    TextView tvIPAddress;
    TextView tvMac;
    TextView tvVendor;
    TextView tvSavedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_device);
        findViewById();

        device = (Device) getIntent().getSerializableExtra("device");
        position = getIntent().getIntExtra("position", -1);
        tvDeviceName.setText(device.getDeviceName());
        tvSavedName.setText(device.getSavedName());
        tvIPAddress.setText(device.getAddress());
        tvMac.setText(device.getMac());
        tvVendor.setText(device.getVendor());

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveDeviceDialog dialog = new SaveDeviceDialog();
                dialog.setOnSavedDevice(new OnSavedDevice() {
                    @Override
                    public void onSaved(String name) {
                        device.setSavedName(name);
                        SaveFileManager.saveDevice(getBaseContext(), device, name);
                        tvSavedName.setText(device.getSavedName());
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result",position);
                        setResult(Activity.RESULT_OK,returnIntent);
                    }
                });
                dialog.show(getSupportFragmentManager(), null);
            }
        });
    }

    private void findViewById(){
        btnSave = findViewById(R.id.activity_device_button_save);
        tvDeviceName = findViewById(R.id.activity_device_text_devicename);
        tvIPAddress = findViewById(R.id.activity_device_text_ip);
        tvMac = findViewById(R.id.activity_device_text_mac);
        tvVendor = findViewById(R.id.activity_device_text_vendor);
        tvSavedName = findViewById(R.id.activity_device_text_savedname);
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
}
