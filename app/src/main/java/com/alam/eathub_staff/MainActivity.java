package com.alam.eathub_staff;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Model.TokenModel;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int APP_REQUEST_CODE = 1234;
    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    AlertDialog dialog;

    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;


    @BindView(R.id.btn_sign_in)
    Button btn_sign_in;

    @OnClick(R.id.btn_sign_in)
    void loginUser() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), APP_REQUEST_CODE);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser staff = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);

        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        firebaseAuth = FirebaseAuth.getInstance();
        listener = firebaseAuth1 -> {
            FirebaseUser staff = firebaseAuth1.getCurrentUser();
            //if staff already logged in
            if (staff != null) {
                dialog.show();

                //onsuccess
               // Paper.book().write(Common.REMEMBER_FBID , );
                dialog.show();

                compositeDisposable.add(myRestaurantAPI.getKey(staff.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(getKeyModel -> {
                            if(getKeyModel.isSuccess()){
                                Common.API_KEY = getKeyModel.getToken();
                                FirebaseInstanceId.getInstance()
                                        .getInstanceId()
                                        .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "[GET TOKEN]"+e.getMessage(), Toast.LENGTH_SHORT).show())
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if(task.isSuccessful()){
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
                                                                                    startActivity(new Intent(MainActivity.this , HomeActivity.class));
                                                                                    finish();
                                                                                }
                                                                                else {
                                                                                    Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }
                                                                            else {
                                                                                //new user
                                                                                startActivity(new Intent(MainActivity.this , UpdateInformationActivity.class));
                                                                                finish();
                                                                            }
                                                                            dialog.dismiss();

                                                                        } , throwable ->{
                                                                            Toast.makeText(MainActivity.this, "[get restaurant owner]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        })
                                                                );
                                                            }, throwable -> {
                                                                Toast.makeText(MainActivity.this, "[UPDATE TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                            })
                                                    );
                                                }
                                            }
                                        });
                            }
                            else{
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, ""+getKeyModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } , throwable -> {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Cannot get json web token", Toast.LENGTH_SHORT).show();
                        })
                );
            }
            else{
                loginUser();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(listener != null && firebaseAuth != null)
            firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(listener != null && firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(listener);
        super.onStop();
    }
}