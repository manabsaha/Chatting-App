package com.infiam.firstbottomnav;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Manab on 07-04-2019.
 */

public class FirebaseMessagingServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notyTitle = remoteMessage.getNotification().getTitle();
        String notyMsg = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String fromUser = remoteMessage.getData().get("fromUserId");

        /*Toast.makeText(getApplicationContext(),notyTitle,Toast.LENGTH_LONG).show();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"ch_id1")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notyTitle)
                .setContentText(notyMsg);*/

        //Declare Notification Manager:

        final NotificationManager mNotify=
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        CharSequence name="Hello";
        String desc="This is notification";
        int imp=NotificationManager.IMPORTANCE_HIGH;
        final String ChannelID="my_channel_01";

    //Notification Channel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel mChannel = new NotificationChannel(ChannelID, name,
                    imp);
            mChannel.setDescription(desc);
            mChannel.setLightColor(Color.CYAN);
            mChannel.canShowBadge();
            mChannel.setShowBadge(true);
            mNotify.createNotificationChannel(mChannel);
        }

        //Intent

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id", fromUser);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        //Notification Builder

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,ChannelID)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(notyTitle)
                    .setContentText(notyMsg)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);


            int mNotificationId = (int) System.currentTimeMillis();

            mNotify.notify(mNotificationId, mBuilder.build());
    }
}
