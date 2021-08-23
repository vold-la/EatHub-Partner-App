package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Model.HotFood;
import com.alam.eathub_staff.Retrofit.IFCMService;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotFoodActivity extends AppCompatActivity {

    @BindView(R.id.pie_chart)
    PieChart pieChart;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    List<PieEntry> entryLists;
    android.app.AlertDialog dialog;

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
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
        setContentView(R.layout.activity_hot_food);

        init();
        initView();

        loadChart();

    }

    private void loadChart() {
        entryLists = new ArrayList<>();

        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.getHotFood(headers)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(hotFoodModel -> {
                    if(hotFoodModel.isSuccess()){
                        int i=0;
                        for(HotFood hotFood: hotFoodModel.getResult()) {
                            entryLists.add(new PieEntry(Float.parseFloat(String.valueOf(hotFood.getPercentage())), hotFood.getName()));
                            i++;
                        }
                        PieDataSet dataSet = new PieDataSet(entryLists , "Hottest Food");

                        PieData data = new PieData();
                        data.setDataSet(dataSet);
                        data.setValueTextSize(14f);
                        data.setValueFormatter(new PercentFormatter(pieChart));

                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                        pieChart.setData(data);
                        pieChart.animateXY(1000,1000);
                        pieChart.setUsePercentValues(true);
                        pieChart.getDescription().setEnabled(false);

                        pieChart.invalidate();
                    }
                    else{
                        Toast.makeText(this, ""+hotFoodModel.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } , throwable -> {
                    Toast.makeText(this, "[GET HOT FOOD]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })
        );
    }

    private void init() {
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }

    private void initView() {
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.order_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
}