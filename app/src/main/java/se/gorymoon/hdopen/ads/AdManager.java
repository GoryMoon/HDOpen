package se.gorymoon.hdopen.ads;

import com.android.volley.VolleyError;
import com.google.gson.annotations.SerializedName;

import java9.util.concurrent.CompletableFuture;
import se.gorymoon.hdopen.network.GsonRequest;
import se.gorymoon.hdopen.network.RequestSingleton;
import timber.log.Timber;

public class AdManager {

    public static String API_URL = "https://gorymoon.se/hdopen/api/ad";
    private static final Object REQ_TAG = new Object();

    private CompletableFuture<AdResponse> fetchFuture;

    public CompletableFuture<AdResponse> fetchAd() {
        Timber.d("Requesting ad");
        GsonRequest<AdResponse> request = new GsonRequest<>(API_URL, AdResponse.class, null, this::onSuccess, this::onError);
        request.setShouldCache(false);
        request.setTag(REQ_TAG);
        RequestSingleton.getInstance().addToRequestQueue(request);

        cancelFuture(null);
        fetchFuture = new CompletableFuture<>();
        return fetchFuture;
    }

    private void onError(VolleyError error) {
        Timber.v(error);
        cancelFuture(null);
    }

    private void onSuccess(AdResponse response) {
        Timber.d("Response: %s", response.toString());
        cancelFuture(response);
    }

    private void cancelFuture(AdResponse response) {
        if (fetchFuture != null && !fetchFuture.isDone() && !fetchFuture.isCancelled()) {
            fetchFuture.complete(response);
            fetchFuture = null;
        }
    }

    public static class AdResponse {
        @SerializedName("image")
        public String image;
    }
}
