package se.gorymoon.hdopen.activities;

import android.os.Bundle;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import se.gorymoon.hdopen.App;
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
                new LibsBuilder()
                        .withFields(R.string.class.getFields())
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withLicenseShown(true)
                        .withAboutIconShown(true)
                        .withVersionShown(true)
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutVersionShown(true)
                        .withAboutVersionShown(true)
                        .withActivityTitle(getString(R.string.pref_libraries))
                        .start(App.getInstance().getApplicationContext());
                return true;
            });
        }
    }
}
