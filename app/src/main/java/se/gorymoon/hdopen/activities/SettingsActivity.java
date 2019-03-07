package se.gorymoon.hdopen.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import se.gorymoon.hdopen.BuildConfig;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.utils.PrefHandler;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AppPreferenceFragment())
                .commit();
    }

    public static class AppPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(PrefHandler.PREF_NAME);

            setPreferencesFromResource(R.xml.pref_notification, rootKey);
            findPreference("version").setSummaryProvider(preference -> BuildConfig.VERSION_NAME);
            findPreference("libraries").setOnPreferenceClickListener(preference -> {
                /*final LibUiListenerSerializable listener = new LibUiListenerSerializable();
                LibTaskCallback callback = new LibTaskCallback() {
                    @Override
                    public void onLibTaskStarted() {
                        listener.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLibTaskFinished(ItemAdapter itemAdapter) {
                        listener.progressBar.setVisibility(View.GONE);
                    }
                };
                new LibsBuilder()
                    .withFields(R.string.class.getFields())
                    .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                    .withExcludedLibraries("jackson")
                    .withLicenseShown(true)
                    .withAboutVersionShown(true)
                    .withActivityTitle(getString(R.string.about))
                    .withLibTaskCallback(callback)
                    .withUiListener(listener)
                    .start(this);*/
                return true;
            });
        }
    }
}
