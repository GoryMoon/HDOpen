package se.gorymoon.hdopen.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Boot extends BroadcastReceiver {

    private static final String WORK_TAG = "hdopen_work";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            addCheckWork();
        }
    }

    public static void addCheckWork() {
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(CheckWorker.class, PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
        builder.addTag(WORK_TAG);
        builder.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS);
        WorkManager.getInstance().enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, builder.build());
    }
}
