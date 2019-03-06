package se.gorymoon.hdopen;


import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import se.gorymoon.hdopen.work.Boot;
import timber.log.Timber;

public class App extends Application {

    protected static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Boot.addCheckWork();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
        instance = this;
    }

    private static class ReleaseTree extends Timber.Tree {

        @Override
        protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.ASSERT) {
                return;
            }

            Crashlytics.log(message);
            if (t != null) {
                Crashlytics.logException(t);
            }
        }
    }

}
