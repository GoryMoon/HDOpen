package se.gorymoon.hdopen.utils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import se.gorymoon.hdopen.version.VersionHandler;
import timber.log.Timber;

public class MessageHandler extends FirebaseMessagingService {

    public static final String VERSION_ACTION = "se.gory_moon.hdopen.message.VERSION";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Timber.d("From: %s", remoteMessage.getFrom());

        // Check if message contains a data payload.
        Map<String, String> messageData = remoteMessage.getData();
        if (messageData.size() > 0 && messageData.containsKey("action")) {
            String action = messageData.get("action");
            String rawData = messageData.get("data");
            JSONObject data = null;
            if (rawData != null) {
                try {
                    data = new JSONObject(rawData);
                } catch (JSONException e) {
                    Timber.v(e, "Error parsing json message data");
                }
            }

            if (action != null) {
                switch (action) {
                    case VERSION_ACTION:
                        if (data != null) {
                            try {
                                String version = data.getString("version");
                                JSONArray changelog = data.getJSONArray("changelog");

                                VersionHandler.handleVersionMessage(version, changelog);
                            } catch (JSONException e) {
                                Timber.v(e, "Error parsing changelog");
                            }
                        }
                        break;
                }
            }

            Timber.d("Message data payload: %s", messageData);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }
    }
}
