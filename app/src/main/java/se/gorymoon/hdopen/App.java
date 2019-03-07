package se.gorymoon.hdopen;


import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.vdurmont.semver4j.Semver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }

        Boot.addCheckWork();
        Semver localVersion = VersionHandler.getLocalVersion();
        if (localVersion != null) {
            String s = PrefHandler.Pref.LAST_VERSION.get(null);
            if (s != null && !localVersion.isEqualTo(s)) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(s);
            }
            if (s == null || !localVersion.isEqualTo(s)) {
                String versionValue = localVersion.getValue();
                FirebaseMessaging.getInstance().subscribeToTopic(versionValue);
                PrefHandler.Pref.LAST_VERSION.set(versionValue);
                VersionHandler.handleNewVersion(s);
            }
        }
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
