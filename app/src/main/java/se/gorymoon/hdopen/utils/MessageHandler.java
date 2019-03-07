package se.gorymoon.hdopen.utils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import se.gorymoon.hdopen.version.VersionHandler;
import timber.log.Timber;

public class MessageHandler extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Timber.d("From: %s", remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String version = remoteMessage.getData().get("version");
            String changelog = remoteMessage.getData().get("changelog");

            VersionHandler.handleVersionMessage(version, changelog);

            Timber.d("Message data payload: %s", remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }
    }
}
