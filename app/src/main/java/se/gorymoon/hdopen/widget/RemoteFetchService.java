package se.gorymoon.hdopen.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import java9.util.concurrent.CompletableFuture;
import se.gorymoon.hdopen.network.StatusRepository;
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

            Timber.d("Requesting data");
            try {
                CompletableFuture<JSONObject> future = StatusRepository.getInstance().refreshData();
                future.get();
            } catch (InterruptedException e) {
                Timber.v(e, "Error getting the future of widget update");
            } catch (ExecutionException e) {
                Timber.v(e, "Error getting the future of widget update");
            } catch (CancellationException e) {
                Timber.d(e, "Error getting the future of widget update");
            }
        }
        populateWidget();
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
}

