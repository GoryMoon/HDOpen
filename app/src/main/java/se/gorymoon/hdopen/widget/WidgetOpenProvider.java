package se.gorymoon.hdopen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Arrays;

import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.status.Status;
import timber.log.Timber;

public class WidgetOpenProvider extends AppWidgetProvider {

    public static final String DATA_FETCHED = "se.gorymoon.hdopen.DATA_FETCHED";
    public static final String CLICKED_EXTRA = "se.gorymoon.hdopen.CLICKED_WIDGET";

    public static boolean updating = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
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

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
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
            RemoteFetchService.enqueueWork(context, broadcastIntent);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
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
