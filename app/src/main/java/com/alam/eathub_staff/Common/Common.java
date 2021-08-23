package com.alam.eathub_staff.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import com.alam.eathub_staff.Model.Order;
import com.alam.eathub_staff.Model.RestaurantOwner;
import com.alam.eathub_staff.OrderDetailActivity;
import com.alam.eathub_staff.R;

import java.io.File;

import androidx.core.app.NotificationCompat;

public class Common {
    public static final String API_RESTAURANT_ENDPOINT = "";
    public static  String API_KEY = "";
    public static final String REMEMBER_FBID = "REMEMBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFI_TITLE ="title" ;
    public static final String NOTIFI_CONTENT = "content";

    public static RestaurantOwner currentRestaurantOwner;
    public static Order currentOrder;

    public static String getTopicChannel(int id) {
        return  new StringBuilder("Restaurant_").append(id).toString();
    }


    public static String convertStatusToString(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static int convertStatusToIndex(int orderStatus) {
        if(orderStatus == -1)
            return 3;
        else
            return orderStatus;
    }

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context,notiId,intent , PendingIntent.FLAG_UPDATE_CURRENT);

        String NOTIFICATION_CHANNEL_ID = "felngss_eathub_staff";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel  = new NotificationChannel(NOTIFICATION_CHANNEL_ID , "EatHub Notification" , NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("EatHub Staff App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification mNotification = builder.build();

        notificationManager.notify(notiId , mNotification);

    }


    public static int convertStringToStatus(String status) {
        if(status.equals("Placed"))
            return 0;
        else if(status.equals("Shipping"))
            return 1;
        else if(status.equals("Shipped"))
            return 2;
        else if(status.equals("Cancelled"))
            return -1;
        return  -1;
    }

    public static String getAppPath(Context context) {
        File dir = new File(android.os.Environment.getExternalStorageDirectory()
                +File.separator
                +context.getResources().getString(R.string.app_name)
                +File.separator);
        if(!dir.exists())
            dir.mkdir();
        return dir.getPath();
    }

    public static String buildJWT(String apiKey) {
        return new StringBuilder("Bearer").append(" ").append(apiKey).toString();
    }
}
