package se.gorymoon.hdopen.handlers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.activities.MainActivity;

public final class NotificationHandler {

    private static final String CHANNEL_ID = "se.gorymoon.hdopen.general";

    private NotificationHandler() {}

    public static void sendNotification(String title, String text, int rgba) {
        createNotificationChannel();

        Intent intent = new Intent(App.getInstance(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(App.getInstance(), 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(App.getInstance(), CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.hd_logo_transparent)
                .setContentTitle(title)
                .setColor(rgba)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        if (text != null && !text.isEmpty()) {
            builder.setContentText(text);
        }

        NotificationManagerCompat nm = NotificationManagerCompat.from(App.getInstance());
        nm.notify(0, builder.build());
    }


    private static void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = App.getInstance().getString(R.string.channel_name);
            String description = App.getInstance().getString(R.string.channel_description);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = App.getInstance().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
