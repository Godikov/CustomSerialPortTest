package com.humu.cspt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kongqw.serialportlibrary.Device;

import java.util.List;

/**
 * @author humu
 * @since 2020/3/1
 */
public class SerialPortDeviceAdapter extends RecyclerView.Adapter<SerialPortDeviceAdapter.MyViewHolder> {

    private final Context context;
    private final List<Device> devices;
    private OnSerialportDeviceClickListener onSerialportDeviceClickListener;

    public SerialPortDeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(context, R.layout.item_serialport_device, null);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        final Device device = devices.get(i);
        myViewHolder.tvSerialportName.setText(device.getName() + " " + device.getFile().getAbsolutePath());
        myViewHolder.tvSerialportName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onSerialportDeviceClickListener != null){
                    onSerialportDeviceClickListener.onDeviceClick(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (devices == null || devices.isEmpty()) ? 0 : devices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSerialportName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSerialportName = itemView.findViewById(R.id.tv_serialport_name);
        }

    }

    public void setOnSerialportDeviceClickListener(OnSerialportDeviceClickListener onSerialportDeviceClickListener){
        this.onSerialportDeviceClickListener = onSerialportDeviceClickListener;
    }

}
