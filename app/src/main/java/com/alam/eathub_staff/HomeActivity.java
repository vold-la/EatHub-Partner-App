package com.alam.eathub_staff;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.alam.eathub_staff.Adapter.MyOrderAdapter;
import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Interface.ILoadMore;
import com.alam.eathub_staff.Model.Order;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
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

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ILoadMore {

    TextView txt_user_name,txt_user_phone;
    private AppBarConfiguration mAppBarConfiguration;

    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    android.app.AlertDialog dialog;

    int maxData = 0;
    MyOrderAdapter adapter;
    List<Order> orderList;

    LayoutAnimationController layoutAnimationController;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Restaurant Order");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this , drawer , toolbar , R.string.navigation_drawer_open , R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

       // NavController navController = Navigation.findNavController(this, R.id.nav_host);
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        View headerView = navigationView.getHeaderView(0);
        txt_user_name = (TextView)headerView.findViewById(R.id.txt_user_name);
        txt_user_phone = (TextView)headerView.findViewById(R.id.txt_user_phone);

        txt_user_name.setText(Common.currentRestaurantOwner.getName());
        txt_user_phone.setText(Common.currentRestaurantOwner.getUserPhone());

        init();
        initView();

        subscribeToTopic(Common.getTopicChannel(Common.currentRestaurantOwner.getRestaurantId()));

        getMaxOrder();
    }

    private void subscribeToTopic(String topicChannel) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicChannel)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Subscribe fail! You may not receive message", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //own choice to toast below
                        }
                        else {
                            Toast.makeText(HomeActivity.this, "[Subscribe Task Failed]"+task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void getMaxOrder() {
        dialog.show();
        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.getMaxOrder(headers ,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxOrderModel -> {
                    if(maxOrderModel.isSuccess()){
                        maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                        dialog.dismiss();

                        getAllOrder(0 , 10 , false);
                    }
                } , throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET MAX ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void getAllOrder(int from , int to , boolean isRefresh) {
        dialog.show();
        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.getOrder(headers ,
                String.valueOf(Common.currentRestaurantOwner.getRestaurantId()) , from , to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                    if(orderModel.isSuccess()){
                        if(orderModel.getResult().size() > 0){
                            if(adapter == null){
                                orderList = new ArrayList<>();
                                orderList = orderModel.getResult();
                                adapter = new MyOrderAdapter(this , orderList , recycler_order);
                                adapter.setiLoadMore(this);

                                recycler_order.setAdapter(adapter);
                                recycler_order.setLayoutAnimation(layoutAnimationController);
                            }
                            else{
                                if(!isRefresh) {
                                    orderList.remove(orderList.size() - 1);
                                    orderList = orderModel.getResult();
                                    adapter.addItem(orderList);
                                }
                                else{

                                    orderList = new ArrayList<>();
                                    orderList = orderModel.getResult();
                                    adapter = new MyOrderAdapter(this , orderList , recycler_order);
                                    adapter.setiLoadMore(this);

                                    recycler_order.setAdapter(adapter);
                                    recycler_order.setLayoutAnimation(layoutAnimationController);
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                } , throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, "[GET ORDER]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }


    private void initView() {
        ButterKnife.bind(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_order.setLayoutManager(layoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(this,layoutManager.getOrientation()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(this , R.anim.layout_item_from_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            getAllOrder( 0 , 10 , true);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    // @Override
    //public boolean onSupportNavigateUp() {
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //return NavigationUI.navigateUp(navController, mAppBarConfiguration)
           //     || super.onSupportNavigateUp();
   // }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        Log.d("onNavigationItemSelect" , "== "+id);
        if(id == R.id.nav_sign_out){
            SignOut();
        }
        else if(id == R.id.nav_hot_food){
            startActivity(new Intent(HomeActivity.this , HotFoodActivity.class));
        }
        else if(id == R.id.nav_shipper){
            startActivity(new Intent(HomeActivity.this , ShipperOrderActivity.class));
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private  void SignOut(){

        AlertDialog confirmDialog = new AlertDialog.Builder(this)
                .setTitle("SignOut")
                .setMessage("Do you really want to Sign Out?")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Common.currentRestaurantOwner = null;
                        Common.currentRestaurantOwner = null;

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .create();

        confirmDialog.show();


    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    @Override
    public void onLoadMore() {
        if(adapter.getItemCount() < maxData){
            orderList.add(null);
            adapter.notifyItemInserted(orderList.size()- 1);

            getAllOrder(adapter.getItemCount()+1 , adapter.getItemCount()+10 , false);

            adapter.notifyDataSetChanged();
            adapter.setLoaded();
        }
        else{
            Toast.makeText(this, "Max data to load", Toast.LENGTH_SHORT).show();
        }

    }
}