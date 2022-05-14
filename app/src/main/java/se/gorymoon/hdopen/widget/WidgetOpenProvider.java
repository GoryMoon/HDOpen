package se.gorymoon.hdopen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.utils.Status;
import timber.log.Timber;

public class WidgetOpenProvider extends AppWidgetProvider {

    public static final String WIDGET_TAG = "hdopen_widget_work";
    public static final String DATA_FETCHED = "se.gorymoon.hdopen.DATA_FETCHED";
    public static final String CLICKED_EXTRA = "se.gorymoon.hdopen.CLICKED_WIDGET";

    public static boolean updating = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        App.verifyLogging();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                boolean clicked = intent.getBooleanExtra(CLICKED_EXTRA, false);
                if (appWidgetIds != null && appWidgetIds.length > 0) {
                    for (int appWidgetId : appWidgetIds) {
                        updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId, appWidgetIds, false, clicked);
                    }
                }
            }
        } else if (DATA_FETCHED.equals(action)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            boolean clicked = intent.getBooleanExtra(CLICKED_EXTRA, false);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds, true, clicked);
        } else {
            super.onReceive(context, intent);
        }
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] appWidgetIds, boolean dataReceived, boolean clicked) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setImageViewResource(R.id.button, R.drawable.yellow_button);
        views.setViewVisibility(R.id.button, View.GONE);
        views.setViewVisibility(R.id.loading, View.VISIBLE);

        Intent broadcastIntent = new Intent(context, WidgetOpenProvider.class);
        broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        broadcastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        broadcastIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        broadcastIntent.setData(Uri.parse(broadcastIntent.toUri(Intent.URI_INTENT_SCHEME)));

        Intent clickIntent = new Intent(broadcastIntent);
        clickIntent.putExtra(CLICKED_EXTRA, true);
        broadcastIntent.putExtra(CLICKED_EXTRA, clicked);

        int flag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PendingIntent.FLAG_IMMUTABLE: 0;
        PendingIntent intent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | flag);
        views.setOnClickPendingIntent(R.id.button, intent);

        if (dataReceived) {
            String time = StatusRepository.getInstance().getUpdateMessage();
            Status status = StatusRepository.getInstance().getStatus();
            if (updating && clicked) {
                Toast.makeText(context, time != null && !time.isEmpty() ? String.format(context.getText(R.string.last_updated).toString(), context.getString(status.getStatus()), time) : context.getText(R.string.updated), Toast.LENGTH_SHORT).show();
            }


            switch (status) {
                case CLOSED:
                    setButton(views, R.drawable.red_button);
                    break;
                case OPEN:
                    setButton(views, R.drawable.green_button);
                    break;
                default:
                    setButton(views, R.drawable.yellow_button);
                    break;
            }

            views.setViewVisibility(R.id.button, View.VISIBLE);
            views.setViewVisibility(R.id.loading, View.GONE);
            Timber.d("Got status: %s", status.name());
            updating = false;
        } else {
            Timber.d("Updating status");
            updateWork(context, clicked, appWidgetId, appWidgetIds);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateWork(Context context, boolean clicked, int appWidgetId, int[] appWidgetIds) {
        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(WidgetFetchWorker.class);
        builder.addTag(WIDGET_TAG);

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putBoolean(CLICKED_EXTRA, clicked);
        dataBuilder.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        dataBuilder.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        builder.setInputData(dataBuilder.build());
        builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST);
        builder.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS);
        WorkManager.getInstance(context).enqueueUniqueWork(WIDGET_TAG, ExistingWorkPolicy.REPLACE, builder.build());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Timber.d("App ids: %s", Arrays.toString(appWidgetIds));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds, false, false);
        }
    }

    static void setButton(RemoteViews views, int id) {
        views.setImageViewResource(R.id.button, id);
    }
}
