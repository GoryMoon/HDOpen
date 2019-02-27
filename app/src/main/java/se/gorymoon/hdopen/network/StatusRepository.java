package se.gorymoon.hdopen.network;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import se.gorymoon.hdopen.status.ErrorListener;
import se.gorymoon.hdopen.status.Status;
import se.gorymoon.hdopen.status.StatusListener;
import timber.log.Timber;

public class StatusRepository {

    private static StatusRepository instance;

    private static final String REMOTE_JSON_URL = "https://hd.chalmers.se/getstatus";

    private ConcurrentHashMap<String, StatusListener> listeners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ErrorListener> errorListeners = new ConcurrentHashMap<>();

    private Status status = Status.UNDEFINED;
    private String updateMessage;

    private static final Object REQ_TAG = new Object();

    public static StatusRepository getInstance() {
        if (instance == null) {
            instance = new StatusRepository();
        }
        return instance;
    }

    public void addListener(String id, StatusListener listener) {
        listeners.put(id, listener);
    }

    public void addErrorListener(String id, ErrorListener listener) {
        errorListeners.put(id, listener);
    }

    public void removeListener(String id) {
        listeners.remove(id);
    }

    public void removeErrorListener(String id) {
        errorListeners.remove(id);
    }

    public void refreshData() {
        Timber.d("Requesting data");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, REMOTE_JSON_URL, null, this::onSuccess, this::error);
        request.setShouldCache(false);
        request.setTag(REQ_TAG);
        RequestSingleton.getInstance().addToRequestQueue(request);
    }

    private void error(VolleyError error) {
        Timber.e(error);

        for (ErrorListener listener: errorListeners.values()) {
            listener.onError(error);
        }
    }

    public void stopRequest() {
        RequestSingleton.getInstance().getRequestQueue().cancelAll(REQ_TAG);
        onSuccess(new JSONObject());
    }

    private void onSuccess(JSONObject json) {
        if (json.has("status")) {
            try {
                int status = json.optInt("status", -1);
                this.status = status == 0 ? Status.CLOSED: status == 1 ? Status.OPEN: Status.UNDEFINED;
                this.updateMessage = json.getString("updated");
            } catch (JSONException e) {
                this.status = Status.UNDEFINED;
                this.updateMessage = "";
                Timber.wtf(e, "An error occurred while getting values. Possible API change?");
            }
        } else {
            this.status = Status.UNDEFINED;
            this.updateMessage = "";
        }

        for (StatusListener listener: listeners.values()) {
            listener.accept(status, updateMessage);
        }
    }

    public Status getStatus() {
        return status != null ? status: Status.UNDEFINED;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }
}
