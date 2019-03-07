package se.gorymoon.hdopen.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class Boot extends BroadcastReceiver {

    private static final String WORK_TAG = "hdopen_work";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            addCheckWork();
        }
    }

    public static void addCheckWork() {
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(CheckWorker.class, 1, TimeUnit.HOURS);
        builder.addTag(WORK_TAG);
        WorkManager.getInstance().enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, builder.build());
    }
}
