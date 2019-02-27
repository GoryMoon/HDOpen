package se.gorymoon.hdopen.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import com.android.volley.VolleyError;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.status.Status;
import timber.log.Timber;

public class RemoteFetchService extends JobIntentService {

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int[] appWidgetIds = new int[0];

    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, RemoteFetchService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            WidgetOpenProvider.updating = true;
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            StatusRepository.getInstance().addListener("widget", this::success);
            StatusRepository.getInstance().addErrorListener("widget", this::onErrorResponse);
            Timber.d("Requesting data");
            StatusRepository.getInstance().refreshData();
        } else {
            populateWidget();
        }
    }

    @Override
    public void onDestroy() {
        StatusRepository.getInstance().stopRequest();
        super.onDestroy();
    }

    /**
     * Method which sends broadcast to WidgetProvider
     * so that widget is notified to do necessary action
     * and here action == WidgetProvider.DATA_FETCHED
     */
    private void populateWidget() {
        Intent widgetUpdateIntent = new Intent(getApplicationContext(), WidgetOpenProvider.class);
        widgetUpdateIntent.setAction(WidgetOpenProvider.DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        sendBroadcast(widgetUpdateIntent);

        this.stopSelf();
    }

    private void onErrorResponse(VolleyError error) {
        populateWidget();
    }

    private void success(Status status, String update) {
        Timber.d("Got response from server: %s %s", status.name(), update);
        populateWidget();
    }
}

