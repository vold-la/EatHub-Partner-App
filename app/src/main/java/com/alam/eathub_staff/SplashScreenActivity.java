package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Model.TokenModel;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        Dexter.withActivity(this)
                .withPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION ,Manifest.permission.WRITE_EXTERNAL_STORAGE })
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        FirebaseInstanceId.getInstance()
                                .getInstanceId()
                                .addOnFailureListener(e -> Toast.makeText(SplashScreenActivity.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if(task.isSuccessful()){
                                    //onsuccess
                                   // Paper.book().write(Common.REMEMBER_FBID , );
                                    dialog.show();
                                    FirebaseUser staff = FirebaseAuth.getInstance().getCurrentUser();
                                    if(staff != null){

                                        compositeDisposable.add(myRestaurantAPI.getKey(staff.getUid())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(getKeyModel -> {
                                                    if(getKeyModel.isSuccess()){
                                                        Common.API_KEY = getKeyModel.getToken();
                                                        Map<String , String> headers = new HashMap<>();
                                                        headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                                                        compositeDisposable.add(myRestaurantAPI.updateTokenToServer(headers, task.getResult().getToken())
                                                                .subscribeOn(Schedulers.io())
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(tokenModel -> {
                                                                    compositeDisposable.add(myRestaurantAPI.getRestaurantOwner(headers)
                                                                            .subscribeOn(Schedulers.io())
                                                                            .observeOn(AndroidSchedulers.mainThread())
                                                                            .subscribe(restaurantOwnerModel -> {
                                                                                if(restaurantOwnerModel.isSuccess()){
                                                                                    Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                                                    //if user already in database ,check permission of user
                                                                                    if(Common.currentRestaurantOwner.isStatus()){
                                                                                        startActivity(new Intent(SplashScreenActivity.this , HomeActivity.class));
                                                                                        finish();
                                                                                    }
                                                                                    else {
                                                                                        Toast.makeText(SplashScreenActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                                else {
                                                                                    //new user
                                                                                    startActivity(new Intent(SplashScreenActivity.this , MainActivity.class));
                                                                                    finish();
                                                                                }
                                                                                dialog.dismiss();

                                                                            } , throwable ->{
                                                                                Toast.makeText(SplashScreenActivity.this, "[get restaurant owner]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                            })
                                                                    );
                                                                }, throwable -> {
                                                                    Toast.makeText(SplashScreenActivity.this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                })
                                                        );
                                                    }
                                                    else{
                                                        dialog.dismiss();
                                                        Toast.makeText(SplashScreenActivity.this, ""+getKeyModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                } , throwable -> {
                                                    dialog.dismiss();
                                                    Toast.makeText(SplashScreenActivity.this, "Cannot get json web token", Toast.LENGTH_SHORT).show();
                                                })
                                        );
                                    }
                                    else{
                                        Toast.makeText(SplashScreenActivity.this, "Not sign in! Please sign in", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SplashScreenActivity.this , MainActivity.class));
                                        finish();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();


    }

    private void init() {
        Paper.init(this);
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

    }
}