package se.gorymoon.hdopen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;

import com.github.paolorotolo.appintro.AppIntro;

import androidx.annotation.Nullable;
import se.gorymoon.hdopen.notification.VersionHandler;
import timber.log.Timber;

public class IntroActivity extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

    }


    public static void checkFirstStart(Context context) {
        new Thread(() -> {
            boolean isFirstRun = PrefHandler.Pref.FIRST_RUN.get(true);

            if (isFirstRun) {
                Timber.d("First run of this version: %s", VersionHandler.getLocalVersion());
                final Intent i = new Intent(context, IntroActivity.class);
                new Handler(Looper.getMainLooper()).post(() -> context.startActivity(i));

                //PrefHandler.Pref.FIRST_RUN.set(false);
            }
        }).start();
    }
}
