package com.alam.eathub_staff.Services;

import android.widget.Toast;

import com.alam.eathub_staff.Common.Common;
import com.alam.eathub_staff.Retrofit.IMyRestaurantAPI;
import com.alam.eathub_staff.Retrofit.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import io.paperdb.Paper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    IMyRestaurantAPI myRestaurantAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        myRestaurantAPI = RetrofitClient.getInstance(Common.API_RESTAURANT_ENDPOINT).create(IMyRestaurantAPI.class);
        compositeDisposable = new CompositeDisposable();
        Paper.init(this);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        //Here we will update token , for this we need FBID but Common.currentUser is null
        //so we need to save a signed fbid by 'paper' & get when needed
        String fbid = Paper.book().read(Common.REMEMBER_FBID);
        String apiKey = Paper.book().read(Common.API_KEY_TAG);


        Map<String , String> headers = new HashMap<>();
        headers.put("Authorization" , Common.buildJWT(Common.API_KEY));
        compositeDisposable.add(myRestaurantAPI.updateTokenToServer(headers , s)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenModel -> {
                    //Do nothing
                } , throwable -> {
                    Toast.makeText(this, "[REFRESH TOKEN]"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Get notification object from Fire..CM
        //since we want to retrieve notification while app is killed , we use Data payload
        Map<String , String> dataRecv = remoteMessage.getData();
        if(dataRecv != null) {
            Common.showNotification(this,
                    new Random().nextInt(),
                    dataRecv.get(Common.NOTIFI_TITLE) ,
                    dataRecv.get(Common.NOTIFI_CONTENT) ,
                    null);
        }
    }
}
