//package com.l227879.stayswift;
//
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.os.Build;
//
//import androidx.core.app.NotificationCompat;
//
//import com.google.firebase.messaging.FirebaseMessagingService;
//import com.google.firebase.messaging.RemoteMessage;
//
//public class MyFirebaseMessagingService extends FirebaseMessagingService {
//
//    @Override
//    public void onMessageReceived(RemoteMessage message) {
//        String title = message.getNotification() != null ? message.getNotification().getTitle() : "StaySwift";
//        String body = message.getNotification() != null ? message.getNotification().getBody() : "";
//
//        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        String channelId = "stayswift";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel ch = new NotificationChannel(channelId, "StaySwift", NotificationManager.IMPORTANCE_HIGH);
//            nm.createNotificationChannel(ch);
//        }
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
//               // .setSmallIcon(R.drawable.ic_notification)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true);
//
//        nm.notify((int) System.currentTimeMillis(), builder.build());
//    }
//}
package com.l227879.stayswift;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "stayswift";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = "StaySwift";
        String body = "";

        // Notification payload (when sent as "notification")
        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) title = message.getNotification().getTitle();
            if (message.getNotification().getBody() != null) body = message.getNotification().getBody();
        }

        // Data payload fallback (when sent as "data")
        if (body.isEmpty() && message.getData() != null) {
            if (message.getData().containsKey("title")) title = message.getData().get("title");
            if (message.getData().containsKey("body")) body = message.getData().get("body");
            if (message.getData().containsKey("message")) body = message.getData().get("message");
        }

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "StaySwift",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(ch);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // REQUIRED
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}