package se.gorymoon.hdopen;

import android.app.Application;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vdurmont.semver4j.Semver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java9.util.stream.StreamSupport;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.version.VersionHandler;
import se.gorymoon.hdopen.work.Boot;
import timber.log.Timber;

public class App extends Application {

    protected static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        verifyLogging();

        if (PrefHandler.Pref.ENABLE_NOTIFICATIONS.get(false)) {
            Boot.addCheckWork(getApplicationContext());
        }

        FirebaseMessaging.getInstance().subscribeToTopic("version");
        Semver localVersion = VersionHandler.getLocalVersion();
        if (localVersion != null) {
            String s = PrefHandler.Pref.LAST_VERSION.get(null);
            if (s == null || !localVersion.isEqualTo(s)) {
                String versionValue = localVersion.getValue();
                PrefHandler.Pref.LAST_VERSION.set(versionValue);
                VersionHandler.handleNewVersion(s);
            }
        }
    }

    public static void verifyLogging() {
        if (BuildConfig.DEBUG) {
            if (StreamSupport.stream(Timber.forest()).noneMatch(tree -> tree instanceof Timber.DebugTree))
                Timber.plant(new Timber.DebugTree());
        } else {
            if (StreamSupport.stream(Timber.forest()).noneMatch(tree -> tree instanceof ReleaseTree)) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
                Timber.plant(new ReleaseTree());
            }
        }
    }

    private static class ReleaseTree extends Timber.Tree {

        @Override
        protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.ASSERT) {
                return;
            }

            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.log(message);
            if (t != null) {
                crashlytics.recordException(t);
            }
        }
    }

}
