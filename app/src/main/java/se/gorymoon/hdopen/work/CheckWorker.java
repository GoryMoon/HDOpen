package se.gorymoon.hdopen.work;

import android.content.Context;

import org.json.JSONObject;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java9.util.concurrent.CompletableFuture;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.status.Status;
import se.gorymoon.hdopen.utils.NotificationHandler;
import se.gorymoon.hdopen.utils.PrefHandler;
import timber.log.Timber;

public class CheckWorker extends Worker {

    private static int tries = 0;

    public CheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        JSONObject jsonObject = null;
        try {
            CompletableFuture<JSONObject> future = StatusRepository.getInstance().refreshData();
            jsonObject = future.get();
        } catch (InterruptedException e) {
            Timber.v(e, "Error getting the future of backgroundtask");
        } catch (ExecutionException e) {
            Timber.v(e, "Error getting the future of backgroundtask");
        } catch (CancellationException ignored) {}

        if (jsonObject == null) {
            tries++;
            Timber.d("Retrying backgroundwork: #%d", tries);
            return Result.retry();
        }

        Status status = StatusRepository.getInstance().getStatus();
        Status storedStatus = PrefHandler.Pref.STATUS.get(Status.UNDEFINED);
        if (storedStatus == status) {
            Timber.d("Status not changed, no notification");
            return Result.success();
        }
        PrefHandler.Pref.STATUS.set(status);
        String updateMessage = StatusRepository.getInstance().getUpdateMessage();
        Timber.d("Changed status, showing notification: %s %s", status, updateMessage);

        if (PrefHandler.Pref.NOTIFICATION_STATUS.get(false)) {
            NotificationHandler.sendNotification(
                    getApplicationContext().getString(status.getStatus()),
                    updateMessage,
                    getApplicationContext().getResources().getColor(status.getColor()),
                    "");
        }
        return Result.success();
    }
}
