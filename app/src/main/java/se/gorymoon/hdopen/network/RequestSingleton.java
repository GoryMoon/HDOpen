package se.gorymoon.hdopen.network;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java9.util.Lists;
import se.gorymoon.hdopen.App;


public class RequestSingleton {
    private static RequestSingleton instance;
    private RequestQueue requestQueue;

    /**
     * Sets up the RequestSingleton
     */
    private RequestSingleton() {
        requestQueue = getRequestQueue();
    }

    /**
     * Gets the instance of the {@link RequestSingleton}, if it doesn't exist it creates a new one
     * @return An instance of {@link RequestSingleton}
     */
    public static synchronized RequestSingleton getInstance() {
        if (instance == null) {
            instance = new RequestSingleton();
        }
        return instance;
    }

    /**
     * Get's the request queue, if it doesn't exist it creates a new one
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(App.getInstance().getApplicationContext(), new OkHttpStack(Lists.of()));
        }
        return requestQueue;
    }


    /**
     * Adds a request to the request queue
     * @param req The {@link Request} to add
     * @param <T> JSON type of response expected
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
