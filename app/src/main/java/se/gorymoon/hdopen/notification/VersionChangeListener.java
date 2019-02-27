package se.gorymoon.hdopen.notification;

import com.vdurmont.semver4j.Semver;

public interface VersionChangeListener {

    void onVersionChange(Semver newVersion);

}
