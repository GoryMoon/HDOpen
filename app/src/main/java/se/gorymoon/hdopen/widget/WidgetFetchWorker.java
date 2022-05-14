package se.gorymoon.hdopen.widget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.work.CoroutineWorker;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkerParameters;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;
import kotlin.coroutines.Continuation;
import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.utils.Status;
import timber.log.Timber;

public class WidgetFetchWorker extends CoroutineWorker {
    public static final int NOTIFICATION_ID = 1000;
    private static final String CHANNEL_ID = "se.gorymoon.hdopen.background";

    private int tries = 1;

    public WidgetFetchWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Nullable
    @Override
    public Object getForegroundInfo(@NonNull Continuation<? super ForegroundInfo> $completion) {
        return new ForegroundInfo(NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = App.getInstance().getString(R.string.background_channel_name);
            String description = App.getInstance().getString(R.string.background_channel_description);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(App.getInstance().getString(R.string.fetching_data))
                .setSmallIcon(R.mipmap.hd_logo_transparent)
                .setColorized(true)
                .setLocalOnly(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build();
    }

    @Nullable
    @Override
    public Object doWork(@NonNull Continuation<? super Result> continuation) {
        App.verifyLogging();
        StatusRepository.StatusMessage jsonObject = null;
        try {
            CompletableFuture<StatusRepository.StatusMessage> future = StatusRepository.getInstance().refreshData(getApplicationContext());
            jsonObject = future.get();
        } catch (InterruptedException | ExecutionException e) {
            Timber.v(e, "Error getting the future of backgroundtask");
        } catch (CancellationException ignored) {}

        if (jsonObject == null) {
            Timber.d("Retrying background work: #%d", tries++);
            return Result.retry();
        }

        Status status = StatusRepository.getInstance().getStatus();
        Status storedStatus = PrefHandler.Pref.STATUS.get(Status.UNDEFINED);
        if (status == Status.UNDEFINED) {
            Timber.d("Status undefined, retrying quicker");
            return Result.retry();
        }

        if (storedStatus != status) {
            PrefHandler.Pref.STATUS.set(status);
        }

        Timber.d("Changed status, updating widget: %s", status);

        populateWidget();

        return Result.success();
    }

    private void populateWidget() {
        Data data = getInputData();
        Intent widgetUpdateIntent = new Intent(getApplicationContext(), WidgetOpenProvider.class);
        widgetUpdateIntent.setAction(WidgetOpenProvider.DATA_FETCHED);
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, data.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, 0));
        widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, data.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS));
        widgetUpdateIntent.putExtra(WidgetOpenProvider.CLICKED_EXTRA, data.getBoolean(WidgetOpenProvider.CLICKED_EXTRA, false));
        getApplicationContext().sendBroadcast(widgetUpdateIntent);
    }
}
