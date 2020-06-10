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
import Objects.ListDevices;
import Objects.OnItemClickListener;

public class ListDevicesAdapter extends RecyclerView.Adapter<ListDevicesAdapter.MyViewHolder> {

    private ListDevices devices = new ListDevices();
    private OnItemClickListener onItemClickListener;

    public ListDevicesAdapter(){}

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public Device get(int position){
        return devices.get(position);
    }

    public void add(Device device){
        devices.add(device);
    }

    public void update(int position, Device device){
        devices.set(position, device);
    }

    public void clear(){
        devices.clear();
    }

    public String getRouterMac(){
        return devices.getRouterMac();
    }

    public void setRouterMac(String mac){
        devices.setRouterMac(mac);
    }

    public int size(){
        return devices.size();
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
        if(devices.get(position).getSavedName() == null)
            holder.tvDeviceName.setText("" + devices.get(position).getDeviceName());
            else holder.tvDeviceName.setText("" + devices.get(position).getSavedName());
        holder.tvVendor.setText(""+devices.get(position).getVendor());
        holder.tvAddress.setText("" + devices.get(position).getAddress());
        holder.setOnItemClickListener(onItemClickListener);
//        switch (devices.get(position).getType()){
//            case "laptop":
//                holder.imgType.setImageResource(R.drawable.ic_laptop_85dp);
//                break;
//            case "smartphone":
//                holder.imgType.setImageResource(R.drawable.ic_smartphone_85dp);
//                break;
//            case "pc":
//                holder.imgType.setImageResource(R.drawable.ic_laptop_85dp);
//                break;
//        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvDeviceName;
        TextView tvAddress;
        TextView tvVendor;
        ImageView imgType;
        OnItemClickListener onItemClickListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvDeviceName = itemView.findViewById(R.id.item_device_name);
            tvAddress = itemView.findViewById(R.id.item_device_address);
            tvVendor = itemView.findViewById(R.id.item_device_vendor);
            imgType = itemView.findViewById(R.id.item_device_icon);
        }

        void setOnItemClickListener(OnItemClickListener onItemClickListener){
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onClick(v, getAdapterPosition());
        }
    }
}