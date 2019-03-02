package com.frazycrazy.kappu.apptech2.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.frazycrazy.kappu.apptech2.BlogActivity;
import com.frazycrazy.kappu.apptech2.NavigationActivity;
import com.frazycrazy.kappu.apptech2.R;
import com.frazycrazy.kappu.apptech2.WebActivity;
import com.frazycrazy.kappu.apptech2.YoutubePlayActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MyFirebaseInstanceService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("------>>>>>>>", "onMessageReceived: remoteMessage getData: "+remoteMessage.getData());

        if(!remoteMessage.getData().isEmpty()) {
            showBigPictureNotification(remoteMessage.getData(),Objects.requireNonNull(remoteMessage.getNotification()).getTitle(), remoteMessage.getNotification().getBody());
        }else{
            // simple notification
            showSimpleNotification(Objects.requireNonNull(remoteMessage.getNotification()).getTitle(), remoteMessage.getNotification().getBody());
           }
    }

    private void showBigPictureNotification(Map<String,String> data,String title,String body) {

        Log.d("------------>>>>>>>>>>>", "showNotification: "+data);

        // Taking data from Server 5 ( title | des | img_url | url | type )

        String my_data_imageTitle = "" + data.get("title");
        String my_data_imageDest = "" + data.get("des");
        String my_data_imageUrl = "http://apptechinteractive.com/earnmoney" +data.get("img_url");
        String my_data_mainUrl = "" + data.get("url");
        String my_data_type = "" + data.get("type");

        // Checking data is Empty or Not
        if (my_data_imageDest.isEmpty() || my_data_imageTitle.isEmpty() || my_data_imageUrl.isEmpty()
                || my_data_mainUrl.isEmpty() || my_data_type.isEmpty()){

            // Simple Notification
            showSimpleNotification(title,body);
        }else {
            // Big Picture Notification
            // asign icon acc to type of data
            Bitmap large_icon;
            switch (my_data_type) {
                case "youtube":
                    large_icon = BitmapFactory.decodeResource(getResources(), R.drawable.youtube);
                    break;
                case "blog":
                    large_icon = BitmapFactory.decodeResource(getResources(), R.drawable.blog);
                    break;
                case "web":
                    large_icon = BitmapFactory.decodeResource(getResources(), R.drawable.www);
                    break;
                default:
                    large_icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_report_black_24dp);
                    break;
            }

            try {
                // convert img url to bitmap
                URL data_imageUrl = new URL(my_data_imageUrl);
                Bitmap image = BitmapFactory.decodeStream(data_imageUrl.openConnection().getInputStream());

                // notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "com.frazycrazy.kappu.apptech2.test";

                // checking version & notification Style
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                            "Notification", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.setDescription("description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                }

                // notification Builder
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setLargeIcon(large_icon)
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image))
                        .setContentTitle(my_data_imageTitle)
                        .setContentText(my_data_imageDest)
                        .setContentInfo(my_data_type);

                // Intent for action  ( youtube | Blog | Web )
                Intent noti_intent;
                switch (my_data_type) {
                    case "youtube":
                        noti_intent = new Intent(this, YoutubePlayActivity.class);
                        noti_intent.putExtra("url", my_data_mainUrl);
                        noti_intent.putExtra("image_title", my_data_imageTitle);
                        noti_intent.putExtra("image_desc", my_data_imageDest);
                        break;
                    case "blog":
                        noti_intent = new Intent(this, BlogActivity.class);
                        noti_intent.putExtra("image_url", my_data_imageUrl);
                        noti_intent.putExtra("image_title", my_data_imageTitle);
                        noti_intent.putExtra("image_desc", my_data_imageDest);
                        break;
                    case "web":
                        noti_intent = new Intent(this, WebActivity.class);
                        noti_intent.putExtra("url", my_data_mainUrl);
                        break;
                    default:
                        noti_intent = new Intent(this, NavigationActivity.class);
                        Toast.makeText(this, "Data is not Available !", Toast.LENGTH_SHORT).show();
                        break;
                }

                // Pending intent
                PendingIntent content_intent = PendingIntent.getActivity(this, 0, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(content_intent);

                // Create notification
                assert notificationManager != null;
                notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
            } catch (IOException e) {
                //System.out.println(e.);
            }
        }
    }

    private void showSimpleNotification(String title, String body) {

        NotificationManager notificationManager = ( NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "com.frazycrazy.kappu.apptech2.test";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Notification",NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("INFO");
        assert notificationManager != null;
        notificationManager.notify(new Random().nextInt(),notificationBuilder.build());
    }

}
