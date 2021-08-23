package com.alam.eathub_staff.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alam.eathub_staff.Model.Addon;
import com.alam.eathub_staff.Model.OrderDetail;
import com.alam.eathub_staff.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDetailOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<OrderDetail> orderDetailList;

    public MyDetailOrderAdapter(Context context, List<OrderDetail> orderDetailList) {
        this.context = context;
        this.orderDetailList = orderDetailList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == 0 ? new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_detail_item , parent,false))
                : new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_detail_addon , parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){

            MyViewHolder myViewHolder = (MyViewHolder) holder;
            Picasso.get().load(orderDetailList.get(position).getImage()).into(myViewHolder.img_food_image);
            myViewHolder.txt_food_name.setText(orderDetailList.get(position).getName());
            myViewHolder.txt_food_quantity.setText(new StringBuilder("Quantity :").append(orderDetailList.get(position).getQuantity()));
            myViewHolder.txt_food_size.setText(new StringBuilder("Size :").append(orderDetailList.get(position).getSize()));

        }
        else if(holder instanceof MyViewHolderAddon){

            MyViewHolderAddon myViewHolderAddon = (MyViewHolderAddon) holder;
            Picasso.get().load(orderDetailList.get(position).getImage()).into(myViewHolderAddon.img_food_image);
            myViewHolderAddon.txt_food_name.setText(orderDetailList.get(position).getName());
            myViewHolderAddon.txt_food_quantity.setText(new StringBuilder("Quantity :").append(orderDetailList.get(position).getQuantity()));
            myViewHolderAddon.txt_food_size.setText(new StringBuilder("Size :").append(orderDetailList.get(position).getSize()));

            List<Addon> addons = new Gson().fromJson(orderDetailList.get(position).getAddOn() ,
                    new TypeToken<List<Addon>>(){}.getType());
            StringBuilder addon_text = new StringBuilder();
            for(Addon addon : addons)
                addon_text.append(addon.getName()).append("\n");
            myViewHolderAddon.txt_addon.setText(addon_text);
        }

    }

    @Override
    public int getItemCount() {
        return orderDetailList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(orderDetailList.get(position).getAddOn().toLowerCase().equals("none") || orderDetailList.get(position).getAddOn().toLowerCase().equals("normal"))
            return 0;
        else
            return 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;

        Unbinder unbinder;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
        }
    }

    public class MyViewHolderAddon extends RecyclerView.ViewHolder{

        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;
        @BindView(R.id.txt_addon)
        TextView txt_addon;

        Unbinder unbinder;


        public MyViewHolderAddon(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
        }
    }
}
