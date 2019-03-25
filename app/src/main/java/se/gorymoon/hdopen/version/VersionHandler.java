package se.gorymoon.hdopen.version;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.vdurmont.semver4j.Semver;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import androidx.fragment.app.Fragment;
import java9.util.function.Consumer;
import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.utils.NotificationHandler;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.version.versions.Version210;
import timber.log.Timber;

public final class VersionHandler {

    private VersionHandler() {}

    private static Consumer<Semver> listener;
    private static Semver localVersion;
    private static LinkedHashMap<String, IVersionSetup> versionHandler = new LinkedHashMap<>();

    static {
        versionHandler.put("2.1.0", new Version210());
    }

    public static final String NEW_VERSION_TAG = "se.gorymoon.hdopen.new_version";

    public static void handleVersionMessage(String version, JSONArray changelogJson) {
        if (version == null || changelogJson == null) return;
        Timber.i("Got version info about: %s", version);

        Semver remoteVersion = new Semver(version);
        Semver localVersion = getLocalVersion();

        Set<String> changelog = new HashSet<>();
        try {
            for (int i = 0; i < changelogJson.length(); i++) {
                changelog.add(changelogJson.getString(i));
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
        try {
            PackageInfo packageInfo = App.getInstance().getPackageManager().getPackageInfo(App.getInstance().getPackageName(), 0);
            return new Semver(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.v(e, "Error getting local-version");
        }
        return new Semver("2.0.0");
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
        return new Semver("2.0.0");
    }

    public static Set<String> getChangelog() {
        return PrefHandler.Pref.CHANGELOG.get(null);
    }


    public static boolean isOutdated() {
        final Semver remote = getRemoteVersion();
        final Semver local = getLocalVersion();
        if (local != null) {
            Timber.d("Comparing two versions: %s and %s", local, remote);
            return local.isLowerThan(remote);
        }
        return false;
    }

    public static void setListener(Consumer<Semver> listener) {
        VersionHandler.listener = listener;
    }

    public static void handleNewVersion(String s) {
        if (s == null) {
            PrefHandler.Pref.OLD_RUN_VERSION.set("2.0.0");
            return;
        }
        Semver oldVersion = new Semver(s);
        for (String v: versionHandler.keySet()) {
            if (oldVersion.isLowerThan(v)) {
                Timber.d("Old version found, showing intro. Version: %s < %s", s, v);
                PrefHandler.Pref.OLD_RUN_VERSION.set(s);
                break;
            }
        }
    }

    public static LinkedHashMap<String, Fragment> getUpdateFragments() {
        LinkedHashMap<String, Fragment> fragments = new LinkedHashMap<>();
        String s = PrefHandler.Pref.OLD_RUN_VERSION.get(null);
        Semver oldVersion = s == null ? null: new Semver(s);
        for (Map.Entry<String, IVersionSetup> entry: versionHandler.entrySet()) {
            if (oldVersion == null || oldVersion.isLowerThan(entry.getKey())) {
                fragments.put(entry.getKey(), entry.getValue().getSlideFragment());
            }
        }
        Timber.d("%d intro pages returned", fragments.size());

        return fragments;
    }

    public static IVersionSetup getVersionSetup(String s) {
        return versionHandler.get(s);
    }
}
