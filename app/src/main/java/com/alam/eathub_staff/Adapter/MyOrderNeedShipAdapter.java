package com.alam.eathub_staff.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Interface.ILoadMore;
import com.alam.eathub_staff.Interface.IRecyclerItemClickListener;
import com.alam.eathub_staff.Model.Order;
import com.alam.eathub_staff.Model.ShipperOrder;
import com.alam.eathub_staff.OrderDetailActivity;
import com.alam.eathub_staff.R;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderNeedShipAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    Context context;
    List<ShipperOrder> orderList;
    SimpleDateFormat simpleDateFormat;

    RecyclerView recyclerView ;
    ILoadMore iLoadMore;
    boolean isLoading = false;
    int totalItemCount = 0 , lastVisibleItem = 0 ,visibleThreshold = 10;

    public MyOrderNeedShipAdapter(Context context, List<ShipperOrder> orderList, RecyclerView recyclerView) {
        this.context = context;
        this.orderList = orderList;
        this.recyclerView = recyclerView;
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");

        //Init
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem =linearLayoutManager.findLastVisibleItemPosition();

                if(!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold){
                    if(iLoadMore != null)
                        iLoadMore.onLoadMore();
                    isLoading = true;
                }
            }
        });
    }

    public void setLoaded(){ isLoading = false;}

    public void addItem(List<ShipperOrder> addedItems) {
        int startInsertedIndex = orderList.size();
        orderList.addAll(addedItems);
        notifyItemInserted(startInsertedIndex);
    }

    public void setiLoadMore(ILoadMore iLoadMore) {
        this.iLoadMore = iLoadMore;
    }

    @Override
    public int getItemViewType(int position) {
        if(orderList.get(position) == null)
            return VIEW_TYPE_LOADING;
        else
            return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == VIEW_TYPE_ITEM){
            itemView = (LayoutInflater.from(context).inflate(R.layout.layout_order , parent , false));
            return new MyViewHolder(itemView);
        }
        else {
            itemView = (LayoutInflater.from(context).inflate(R.layout.layout_loading_item , parent , false));
            return new MyLoadingHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){

            MyViewHolder myViewHolder = (MyViewHolder) holder;

            myViewHolder.txt_num_of_item.setText(new StringBuilder("Num Of Items : ").append(orderList.get(position).getNumOfItem()));
            myViewHolder.txt_order_Address.setText(new StringBuilder(orderList.get(position).getOrderAddress()));
            myViewHolder.txt_order_date.setText(new StringBuilder(simpleDateFormat.format(orderList.get(position).getOrderDate())));
            myViewHolder.txt_order_number.setText(new StringBuilder("Order Number : #").append(orderList.get(position).getOrderId()));
            myViewHolder.txt_order_phone.setText(new StringBuilder(orderList.get(position).getOrderPhone()));
            myViewHolder.txt_order_total_price.setText(new StringBuilder(context.getString(R.string.money_sign)).append(orderList.get(position).getTotalPrice()));
            myViewHolder.txt_order_status.setText(Common.convertStatusToString(orderList.get(position).getOrderStatus()));

            if(orderList.get(position).isCod())
                myViewHolder.txt_payment_method.setText(new StringBuilder("Cash On Deliver"));
            else
                myViewHolder.txt_payment_method.setText(new StringBuilder("TransID : ").append(orderList.get(position).getTransactionId()));

            myViewHolder.btn_ship.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //later
                }
            });
        }
        else if(holder instanceof MyLoadingHolder){
            MyLoadingHolder myLoadingHolder = (MyLoadingHolder) holder;

            myLoadingHolder.progress_bar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txt_order_number)
        TextView txt_order_number;
        @BindView(R.id.txt_order_status)
        TextView txt_order_status;
        @BindView(R.id.txt_order_phone)
        TextView txt_order_phone;
        @BindView(R.id.txt_order_Address)
        TextView txt_order_Address;
        @BindView(R.id.txt_order_date)
        TextView txt_order_date;
        @BindView(R.id.txt_order_total_price)
        TextView txt_order_total_price;
        @BindView(R.id.txt_num_of_item)
        TextView txt_num_of_item;
        @BindView(R.id.txt_payment_method)
        TextView txt_payment_method;
        @BindView(R.id.btn_ship)
        Button btn_ship;

        IRecyclerItemClickListener listener;

        public void setListener(IRecyclerItemClickListener listener) {
            this.listener = listener;
        }

        Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClickListener(v , getAdapterPosition());
        }
    }

    public class MyLoadingHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.progress_bar)
        ProgressBar progress_bar;


        Unbinder unbinder;
        public MyLoadingHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this , itemView);
        }
    }
}

