package se.gorymoon.hdopen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import se.gorymoon.hdopen.databinding.FirstRunSetupViewBinding;
import se.gorymoon.hdopen.utils.PrefHandler;

public class Version210Fragment extends Fragment {

    private boolean notificationsEnabled;
    private boolean notificationsStatus;
    private boolean notificationsVersion;

    private FirstRunSetupViewBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FirstRunSetupViewBinding.inflate(inflater, container, false);

        binding.notificationEnabled.setChecked(notificationsEnabled = PrefHandler.Pref.ENABLE_NOTIFICATIONS.get(true));
        binding.notificationEnabled.setOnCheckedChangeListener((button, isChecked) -> {
            notificationsEnabled = isChecked;
            binding.notificationStatus.setEnabled(isChecked);
            binding.notificationVersion.setEnabled(isChecked);
        });

        binding.notificationStatus.setChecked(notificationsStatus = PrefHandler.Pref.NOTIFICATION_STATUS.get(false));
        binding.notificationStatus.setOnCheckedChangeListener((button, isChecked) -> notificationsStatus = isChecked);

        binding.notificationVersion.setChecked(notificationsVersion = PrefHandler.Pref.NOTIFICATION_VERSION.get(true));
        binding.notificationVersion.setOnCheckedChangeListener((button, isChecked) -> notificationsVersion = isChecked);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
