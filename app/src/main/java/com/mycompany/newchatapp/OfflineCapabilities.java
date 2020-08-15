package com.mycompany.newchatapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;

public class OfflineCapabilities extends Application {

    public static final String CHANNEL_1_ID = "channel 1";
    public static final String CHANNEL_2_ID = "channel 2";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        createNotification();
    }
    private void createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Message",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Show Message");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Uploading Media",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("Uploading Media");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(channel2);
        }
    }
}
