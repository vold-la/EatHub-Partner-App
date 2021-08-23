package com.alam.eathub_staff.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alam.eathub_staff.Model.EventBus.UpdatedShipperEvent;
import com.alam.eathub_staff.Model.Shipper;
import com.alam.eathub_staff.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperAdapter extends RecyclerView.Adapter<MyShipperAdapter.MyViewHolder> {

    private Context context;
    private List<Shipper> shipperList;

    public MyShipperAdapter(Context context, List<Shipper> shipperList) {
        this.context = context;
        this.shipperList = shipperList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_shipper_restaurant , parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_name.setText(new StringBuilder(shipperList.get(position).getName()));
        holder.txt_phone.setText(new StringBuilder(shipperList.get(position).getPhone()));
        holder.btn_enable.setChecked(shipperList.get(position).isActive());

        //event
        holder.btn_enable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            EventBus.getDefault().postSticky(new UpdatedShipperEvent(shipperList.get(position),isChecked));
        });

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.btn_enable)
        SwitchCompat btn_enable;

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
        }
    }
}
