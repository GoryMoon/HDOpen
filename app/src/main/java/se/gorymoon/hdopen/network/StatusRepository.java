package se.gorymoon.hdopen.network;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.BiConsumer;
import java9.util.function.Consumer;
import java9.util.stream.Stream;
import se.gorymoon.hdopen.status.Status;
import timber.log.Timber;

public class StatusRepository {

    private static StatusRepository instance;

    private static final String REMOTE_JSON_URL = "https://hd.chalmers.se/getstatus";

    private ConcurrentHashMap<String, BiConsumer<Status, String>> listeners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Consumer<VolleyError>> errorListeners = new ConcurrentHashMap<>();

    private Status status = Status.UNDEFINED;
    private String updateMessage;

    private static final Object REQ_TAG = new Object();
    private CompletableFuture<JSONObject> refreshFuture;

    public static StatusRepository getInstance() {
        if (instance == null) {
            instance = new StatusRepository();
        }
        return instance;
    }

    public void addListener(String id, BiConsumer<Status, String> listener) {
        listeners.put(id, listener);
    }

    public void addErrorListener(String id, Consumer<VolleyError> listener) {
        errorListeners.put(id, listener);
    }

    public void removeListener(String id) {
        listeners.remove(id);
    }

    public void removeErrorListener(String id) {
        errorListeners.remove(id);
    }

    public CompletableFuture<JSONObject> refreshData() {
        Timber.d("Requesting data");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, REMOTE_JSON_URL, null, this::onSuccess, this::error);
        request.setShouldCache(false);
        request.setTag(REQ_TAG);
        RequestSingleton.getInstance().addToRequestQueue(request);

        cancelFuture(null);
        refreshFuture = new CompletableFuture<>();
        return refreshFuture;
    }

    private void error(VolleyError error) {
        Timber.v(error);
        cancelFuture(null);
        //noinspection unchecked
        Stream.of(errorListeners.values().toArray(new Consumer[0])).forEach(consumer -> consumer.accept(error));
    }

    public void stopRequest() {
        RequestSingleton.getInstance().getRequestQueue().cancelAll(REQ_TAG);
        //noinspection unchecked
        Stream.of(listeners.values().toArray(new BiConsumer[0])).forEach(consumer -> consumer.accept(status, updateMessage));
    }

    private void onSuccess(JSONObject json) {
        Timber.d(json.toString());
        if (json.has("status")) {
            cancelFuture(json);
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
            cancelFuture(null);
            this.status = Status.UNDEFINED;
            this.updateMessage = "";
        }

        //noinspection unchecked
        Stream.of(listeners.values().toArray(new BiConsumer[0])).forEach(consumer -> consumer.accept(status, updateMessage));
    }

    private void cancelFuture(JSONObject json) {
        if (refreshFuture != null && !refreshFuture.isDone() && !refreshFuture.isCancelled()) {
            refreshFuture.complete(json);
            refreshFuture = null;
        }
    }

    public Status getStatus() {
        return status != null ? status: Status.UNDEFINED;
    }

    public String getUpdateMessage() {
        return updateMessage;
    }
}
