package com.example.wifimanager;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import Objects.Device;
import Objects.SavedDevice;

public class SaveFileManager {

    private static List<SavedDevice> devices = new ArrayList<>();

    //thành phần truy nhập file
    static AssetManager am;
    static BufferedReader reader;
    static String filename = "wifimanager_demo2.txt";

    //thành phần ghi file
    static BufferedWriter bufferedWriter;
    static PrintWriter writer;

    public static Boolean isNull(){
        return devices.isEmpty();
    }

    public static void getSavedName(Device device){
        int size = devices.size();
        for(int i = 0; i < size; i++){
            if (device.getMac().equals(devices.get(i).getMac())) device.setSavedName(devices.get(i).getName());
        }
    }

    public static void getSavedDevice(Context context){

        devices.clear();

        try {
            FileInputStream in = context.openFileInput(filename);

            reader = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] device = new String[2];
            while((line = readLine())!= null){
                device = line.split(" ", 2);
                if(device.length >1) device[1].trim();
                if(device.length >1 && !device[1].equals(""))
                    devices.add(new SavedDevice(device[0], device[1]));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveDevice(Context context, Device device, String newName){
        //if (device.getSavedName() == null) return;
        if (device.getSavedName().trim().equals("")) return;
        int size = devices.size();
        Boolean flag =false;
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < size; i++) {
            if(device.getMac().equals(devices.get(i).getMac())) {
                devices.get(i).setName(newName);
                flag = true;
            }
            String line = devices.get(i).getMac() + " " + devices.get(i).getName() + "\n";
            writer.write(line);
        }
        if(!flag) {
            String line = device.getMac() + " " + newName + "\n";
            writer.write(line);
            devices.add(new SavedDevice(device.getMac(), newName));
        }
        writer.close();
    }

    static String readLine(){
        String result = null;
        try {
            result = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result == null) return null;
        if(result.equals("")) return null;
        return result;
    }

}
