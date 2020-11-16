package se.gorymoon.hdopen.network;

import android.content.Context;
import android.text.format.DateUtils;

import com.android.volley.VolleyError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import java9.util.concurrent.CompletableFuture;
import java9.util.function.BiConsumer;
import java9.util.function.Consumer;
import java9.util.stream.Stream;
import se.gorymoon.hdopen.utils.Status;
import timber.log.Timber;

public class StatusRepository {
    private static StatusRepository instance;

    private static final String REMOTE_JSON_URL = "https://hd.chalmers.se/api/door";

    private final ConcurrentHashMap<String, BiConsumer<Status, String>> listeners = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<VolleyError>> errorListeners = new ConcurrentHashMap<>();

    private Status status = Status.UNDEFINED;
    private String updateMessage;

    private static final Object REQ_TAG = new Object();
    private CompletableFuture<StatusMessage> refreshFuture;

    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        INPUT_FORMAT.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
    }

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

    public CompletableFuture<StatusMessage> refreshData(Context context) {
        Timber.d("Requesting data");
        GsonRequest<StatusMessage> request = new GsonRequest<>(REMOTE_JSON_URL, StatusMessage.class, null, response -> onSuccess(response, context), this::onError);
        request.setShouldCache(false);
        request.setTag(REQ_TAG);
        RequestSingleton.getInstance().addToRequestQueue(request);

        cancelFuture(null);
        refreshFuture = new CompletableFuture<>();
        return refreshFuture;
    }

    private void onError(VolleyError error) {
        Timber.v(error);
        cancelFuture(null);
        this.status = Status.UNDEFINED;
        this.updateMessage = "";
        //noinspection unchecked
        Stream.of(errorListeners.values().toArray(new Consumer[0])).forEach(consumer -> consumer.accept(error));
    }

    public void stopRequest() {
        RequestSingleton.getInstance().getRequestQueue().cancelAll(REQ_TAG);
        //noinspection unchecked
        Stream.of(listeners.values().toArray(new BiConsumer[0])).forEach(consumer -> consumer.accept(status, updateMessage));
    }

    private void onSuccess(StatusMessage statusMessage, Context context) {
        Timber.d(statusMessage.toString());
        if (statusMessage != null) {
            cancelFuture(statusMessage);
            this.status = statusMessage.status ? Status.CLOSED: Status.OPEN;
            String updateString = this.updateMessage = statusMessage.updated;
            try {
                Date inTime = INPUT_FORMAT.parse(updateString);
                if (inTime != null) {
                    this.updateMessage = DateUtils.formatDateTime(context, inTime.getTime(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                }
            } catch (ParseException e) {
                Timber.e(e, "Failed to format update time");
                this.updateMessage = updateString;
            }
        } else {
            cancelFuture(null);
            this.status = Status.UNDEFINED;
            this.updateMessage = "";
        }

        //noinspection unchecked
        Stream.of(listeners.values().toArray(new BiConsumer[0])).forEach(consumer -> consumer.accept(status, updateMessage));
    }

    private void cancelFuture(StatusMessage json) {
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

    public static class StatusMessage {
        public String updated;
        public boolean status;
        public int duration;
        public String duration_str;

        @Override
        public String toString() {
            return "StatusMessage{" +
                    "updated='" + updated + '\'' +
                    ", status=" + status +
                    ", duration=" + duration +
                    ", duration_str='" + duration_str + '\'' +
                    '}';
        }
    }
}
