package se.gorymoon.hdopen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.utils.PrefHandler;

public class Version210Fragment extends Fragment {

    private boolean notificationsEnabled;
    private boolean notificationsStatus;
    private boolean notificationsVersion;

    @BindView(R.id.notification_enabled)
    protected Switch enabledSwitch;

    @BindView(R.id.notification_status)
    protected Switch statusSwitch;

    @BindView(R.id.notification_version)
    protected Switch versionSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.first_run_setup_view, container, false);
        ButterKnife.bind(this, view);

        enabledSwitch.setChecked(notificationsEnabled = PrefHandler.Pref.ENABLE_NOTIFICATIONS.get(true));
        statusSwitch.setChecked(notificationsStatus = PrefHandler.Pref.NOTIFICATION_STATUS.get(false));
        versionSwitch.setChecked(notificationsVersion = PrefHandler.Pref.NOTIFICATION_VERSION.get(true));
        return view;
    }

    @OnCheckedChanged(R.id.notification_enabled)
    void notificationEnabled(boolean isChecked) {
        notificationsEnabled = isChecked;
        statusSwitch.setEnabled(isChecked);
        versionSwitch.setEnabled(isChecked);
    }

    @OnCheckedChanged(R.id.notification_status)
    void notificationStatus(boolean isChecked) {
        notificationsStatus = isChecked;
    }

    @OnCheckedChanged(R.id.notification_version)
    void notificationVersion(boolean isChecked) {
        notificationsVersion = isChecked;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isNotificationsStatus() {
        return notificationsStatus;
    }

    public boolean isNotificationsVersion() {
        return notificationsVersion;
    }
}
