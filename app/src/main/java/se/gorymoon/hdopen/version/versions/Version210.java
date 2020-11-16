package se.gorymoon.hdopen.version.versions;

import androidx.fragment.app.Fragment;

import se.gorymoon.hdopen.fragments.Version210Fragment;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.version.IVersionSetup;
import se.gorymoon.hdopen.work.Boot;

public class Version210 implements IVersionSetup {

    @Override
    public Fragment getSlideFragment() {
        return new Version210Fragment();
    }

    @Override
    public void handleVersion(Fragment frag) {
        Version210Fragment fragment = (Version210Fragment) frag;
        PrefHandler.getInstance().startBulk();
        PrefHandler.Pref.ENABLE_NOTIFICATIONS.set(fragment.isNotificationsEnabled());
        PrefHandler.Pref.NOTIFICATION_STATUS.set(fragment.isNotificationsStatus());
        PrefHandler.Pref.NOTIFICATION_VERSION.set(fragment.isNotificationsVersion());
        PrefHandler.getInstance().commitBulk();
        Boot.addCheckWork();
    }
}
