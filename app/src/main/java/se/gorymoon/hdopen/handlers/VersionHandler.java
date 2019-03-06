package se.gorymoon.hdopen.handlers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.vdurmont.semver4j.Semver;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

import java9.util.function.Consumer;
import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.utils.PrefHandler;
import timber.log.Timber;

public final class VersionHandler {

    private VersionHandler() {}

    private static Consumer<Semver> listener;
    private static Semver localVersion;

    public static final String NEW_VERSION_TAG = "se.gorymoon.hdopen.new_version";

    public static void handleVersionMessage(String version, String changelogJson) {
        if (version == null || changelogJson == null) return;
        Timber.i("Got version info about: %s", version);

        Semver remoteVersion = new Semver(version);
        Semver localVersion = getLocalVersion();

        Set<String> changelog = new HashSet<>();
        try {
            JSONArray jsonArray = new JSONArray(changelogJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                changelog.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Timber.v(e, "Error parsing changelog");
        }

        PrefHandler.getInstance().startBulk();
        PrefHandler.Pref.REMOTE_VERSION.set(version);
        PrefHandler.Pref.CHANGELOG.set(changelog);
        PrefHandler.getInstance().commitBulk();

        //Outdated version
        if (localVersion != null && localVersion.isLowerThan(remoteVersion)) {
            if (PrefHandler.Pref.NOTIFICATION_VERSION.get(true)) {
                Context context = App.getInstance().getApplicationContext();
                NotificationHandler.sendNotification(
                        context.getString(R.string.new_version),
                        context.getString(R.string.new_version_description),
                        context.getResources().getColor(R.color.colorAccent),
                        NEW_VERSION_TAG);
            }
            if (listener != null) {
                listener.accept(remoteVersion);
            }
            Timber.i("Version Outdated (Local < Remote): %s < %s", localVersion.toString(), remoteVersion.toString());
        }
    }

    private static Semver createLocal() {
        Semver localVersion = null;
        try {
            PackageInfo packageInfo = App.getInstance().getPackageManager().getPackageInfo(App.getInstance().getPackageName(), 0);
            localVersion = new Semver(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.v(e, "Error getting local-version");
        }
        return localVersion;
    }

    public static Semver getLocalVersion() {
        if (localVersion == null) {
            localVersion = createLocal();
        }
        return localVersion;
    }

    public static Semver getRemoteVersion() {
        String remote = PrefHandler.Pref.REMOTE_VERSION.get(null);
        if (remote != null) {
            return new Semver(remote);
        }
        return null;
    }

    public static Set<String> getChangelog() {
        return PrefHandler.Pref.CHANGELOG.get(null);
    }


    public static boolean isOutdated() {
        final Semver remote = getRemoteVersion();
        final Semver local = getLocalVersion();
        if (remote != null && local != null) {
            Timber.d("Comparing two versions: %s and %s", local, remote);
            return local.isLowerThan(remote);
        }
        return false;
    }

    public static void setListener(Consumer<Semver> listener) {
        VersionHandler.listener = listener;
    }
}
