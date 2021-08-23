package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateInformationActivity extends AppCompatActivity {
    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    @BindView(R.id.edit_user_name)
    EditText edt_user_name;
    @BindView(R.id.btn_update)
    Button btn_update;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_information);

        ButterKnife.bind(this);
        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        toolbar.setTitle(getString(R.string.update_information));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                FirebaseUser staff = FirebaseAuth.getInstance().getCurrentUser();
                if(staff != null){

                    //Todo: qwer all if fixed
                    Map<String , String> headers = new HashMap<>();
                    headers.put("Authorization" , Common.buildJWT(Common.API_KEY));
                    compositeDisposable.add(myRestaurantAPI.updateRestaurantOwner(headers,
                            staff.getPhoneNumber(),
                            edt_user_name.getText().toString())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(updateUserModel -> {
                                        if(updateUserModel.isSuccess()){
                                            compositeDisposable.add(myRestaurantAPI.getRestaurantOwner(headers)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(restaurantOwnerModel -> {
                                                        if (restaurantOwnerModel.isSuccess()) {
                                                            Common.currentRestaurantOwner = restaurantOwnerModel.getResult().get(0);
                                                            //if user already in database ,check permission of user
                                                            if (Common.currentRestaurantOwner.isStatus()) {
                                                                startActivity(new Intent(UpdateInformationActivity.this, HomeActivity.class));
                                                                finish();
                                                            } else {
                                                                Toast.makeText(UpdateInformationActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                        else {
                                                            Toast.makeText(UpdateInformationActivity.this, ""+restaurantOwnerModel.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                        dialog.dismiss();

                                                    }, throwable -> {
                                                        Toast.makeText(UpdateInformationActivity.this, "[get restaurant owner]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                            );

                                        }
                                        else {
                                            dialog.dismiss();
                                            Toast.makeText(UpdateInformationActivity.this, "[UPDATE STAFF API RETURN]"+updateUserModel.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    },
                                    throwable -> {
                                        dialog.dismiss();
                                        Toast.makeText(UpdateInformationActivity.this, "[UPDATE STAFF API]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                    );
                }
                else{
                    Toast.makeText(UpdateInformationActivity.this, "Not signed In! Please sign in", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UpdateInformationActivity.this , MainActivity.class));
                    finish();
                }
            }
        });

        if(Common.currentRestaurantOwner !=null && !TextUtils.isEmpty(Common.currentRestaurantOwner.getName()))
            edt_user_name.setText(Common.currentRestaurantOwner.getName());
    }

    private void init() {
        /*
        Paper.init(this);
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();

        listener = firebaseAuth1 -> {
            FirebaseUser user = firebaseAuth1.getCurrentUser();
            //if user already logged in
            if(user != null) {
                dialog.show();


                compositeDisposable.add(myRestaurantAPI.getKey(user.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getKeyModel -> {
                            if(getKeyModel.isSuccess()){

                                //write jwt to variable
                                Common.API_KEY = getKeyModel.getToken();

                                //After we have account , we will get Uid and update token
                                Map<String , String> headers = new HashMap<>();
                                headers.put("Authorization" , Common.buildJWT(Common.API_KEY));
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(UpdateInfoActivity.this, ""+getKeyModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }, throwable -> {
                            dialog.dismiss();
                            Toast.makeText(UpdateInfoActivity.this, "Cannot get json web token", Toast.LENGTH_SHORT).show();
                        })
                );


                //Save FBID
                Paper.book().write(Common.REMEMBER_FBID, user.getUid());
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Common.buildJWT(Common.API_KEY));
                FirebaseInstanceId.getInstance().getInstanceId().addOnFailureListener(e -> Toast.makeText(UpdateInfoActivity.this, "[GET TOKEN]" + e.getMessage(), Toast.LENGTH_SHORT).show()).addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        compositeDisposable.add(myRestaurantAPI.updateTokenToServer(headers, task.getResult().getToken())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(tokenModel -> {
                                    if (!tokenModel.isSuccess())
                                        Toast.makeText(UpdateInfoActivity.this, "[UPDATE TOKEN ERROR]" + tokenModel.getMessage(), Toast.LENGTH_SHORT).show();

                                    compositeDisposable.add(myRestaurantAPI.getUser(headers)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(userModel -> {
                                                        if (userModel.isSuccess()) // user available in database
                                                        {
                                                            Common.currentUser = userModel.getResult().get(0);
                                                            Intent intent = new Intent(UpdateInfoActivity.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else // user not available
                                                        {
                                                            Intent intent = new Intent(UpdateInfoActivity.this, UpdateInfoActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        dialog.dismiss();
                                                    },
                                                    throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(UpdateInfoActivity.this, "[GET USER API] " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));

                                }, throwable -> {
                                    Toast.makeText(UpdateInfoActivity.this, "[UPDATE TOKEN]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                })
                        );
                    }
                });
            }*/
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
    }
}