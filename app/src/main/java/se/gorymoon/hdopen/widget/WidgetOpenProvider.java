package se.gorymoon.hdopen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Arrays;

import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.status.Status;
import timber.log.Timber;

public class WidgetOpenProvider extends AppWidgetProvider {

    public static final String DATA_FETCHED = "se.gorymoon.openhd.DATA_FETCHED";

    public static boolean updating = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (DATA_FETCHED.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds, true);
        }
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int[] appWidgetIds, boolean dataReceived) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setImageViewResource(R.id.button, R.drawable.yellow_button);
        views.setViewVisibility(R.id.button, View.GONE);
        views.setViewVisibility(R.id.loading, View.VISIBLE);

        Intent broadcast = new Intent(context, WidgetOpenProvider.class);
        broadcast.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        broadcast.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        broadcast.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        broadcast.setData(Uri.parse(broadcast.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent intent = PendingIntent.getBroadcast(context, 0, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button, intent);

        if (dataReceived) {
            String time = StatusRepository.getInstance().getUpdateMessage();
            if (updating) {
                Toast.makeText(context, time != null && !time.isEmpty() ? String.format(context.getText(R.string.last_updated).toString(), time) : context.getText(R.string.updated), Toast.LENGTH_SHORT).show();
            }

            Status status = StatusRepository.getInstance().getStatus();
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
            RemoteFetchService.enqueueWork(context, broadcast);
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Timber.d("App ids: %s", Arrays.toString(appWidgetIds));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetIds, false);
        }
    }

    static void setButton(RemoteViews views, int id) {
        views.setImageViewResource(R.id.button, id);
    }
}
