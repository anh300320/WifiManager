package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wifimanager.R;

import java.util.ArrayList;
import java.util.List;

import Objects.Device;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.MyViewHolder> {

    private List<Device> devices = new ArrayList<>();

    public ListDevicesAdapter(){}

    public ListDevicesAdapter(List<Device> devices) {
        this.devices = devices;
    }

    public void add(Device device){
        devices.add(device);
    }

    public void set(List<Device> devices){
        this.devices = devices;
    }

    public Device get(int position){
        return devices.get(position);
    }

    public List<Device> get(){
        return devices;
    }

    public void clear(){
        devices.clear();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        MyViewHolder vh = new MyViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvDeviceName.setText("Tên thiết bị: " + devices.get(position).getDeviceName());
        holder.tvVendor.setText(""+devices.get(position).getVendor());
        holder.tvAddress.setText("Địa chỉ IP: " + devices.get(position).getAddress());
        holder.tvMAC.setText("Địa chỉ MAC: " + devices.get(position).getMac());
        switch (devices.get(position).getType()){
            case "laptop":
                holder.imgType.setImageResource(R.drawable.ic_laptop_85dp);
                break;
            case "smartphone":
                holder.imgType.setImageResource(R.drawable.ic_smartphone_85dp);
                break;
            case "pc":
                holder.imgType.setImageResource(R.drawable.ic_laptop_85dp);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvDeviceName;
        TextView tvAddress;
        TextView tvMAC;
        TextView tvVendor;
        ImageView imgType;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.item_device_name);
            tvAddress = itemView.findViewById(R.id.item_device_address);
            tvMAC = itemView.findViewById(R.id.item_device_mac);
            tvVendor = itemView.findViewById(R.id.item_device_vendor);
            imgType = itemView.findViewById(R.id.item_device_icon);
        }

        @Override
        public void onClick(View v) {

        }
    }
}