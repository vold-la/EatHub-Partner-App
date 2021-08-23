package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.alam.eathub_staff.Adapter.MyOrderAdapter;
import com.alam.eathub_staff.Adapter.MyOrderNeedShipAdapter;
import com.alam.eathub_staff.Adapter.MyShipperAdapter;
import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Interface.ILoadMore;
import com.alam.eathub_staff.Model.EventBus.UpdatedShipperEvent;
import com.alam.eathub_staff.Model.Shipper;
import com.alam.eathub_staff.Model.ShipperOrder;
import com.alam.eathub_staff.Retrofit.IFCMService;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetroFitFCMClient;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipperOrderActivity extends AppCompatActivity {


    @BindView(R.id.recycler_shippers)
    RecyclerView recycler_shippers;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    int maxData = 0;
    MyShipperAdapter adapter;
    List<Shipper> shipperList;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_order);

        init();
        initView();

        getShippersByRestaurant();
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.order_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this , R.anim.layout_item_from_left);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_shippers.setLayoutManager(layoutManager);
        recycler_shippers.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

    }

    private void getShippersByRestaurant() {
        dialog.show();
        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.getShipperByRestaurant(headers,
                Common.currentRestaurantOwner.getRestaurantId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shipperModel -> {
                   if(shipperModel.isSuccess()){
                       if(shipperModel.getResult().size() >0){
                           //create adapter
                           shipperList = new ArrayList<>();
                           shipperList = shipperModel.getResult();
                           adapter = new MyShipperAdapter(ShipperOrderActivity.this , shipperList);
                           recycler_shippers.setAdapter(adapter);
                           recycler_shippers.setLayoutAnimation(layoutAnimationController);
                       }
                   }
                   dialog.dismiss();
                } , throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET MAX ORDER NEED SHIP]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true , threadMode = ThreadMode.ASYNC)
    public void onUpdateShipperEvent(UpdatedShipperEvent event){

        dialog.show();
        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.updateShipper(headers,
                event.getShipper().getRestaurantId() ,
                event.isActive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shipperModel -> {
                    if(shipperModel.isSuccess())
                        Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, ""+shipperModel.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } , throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET MAX ORDER NEED SHIP]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );

    }
}