package se.gorymoon.hdopen.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.model.SliderPage;
import com.github.appintro.model.SliderPagerBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.utils.Utils;
import se.gorymoon.hdopen.version.VersionHandler;
import timber.log.Timber;

public class IntroActivity extends AppIntro {

    private LinkedHashMap<String, Fragment> updateFragments;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActionBar(getApplicationContext(), getSupportActionBar());

        SliderPage page = new SliderPagerBuilder()
                .title(getString(R.string.welcome))
                .titleTypefaceFontRes(R.font.roboto_light)
                .description(getString(R.string.welcome_desc))
                .descriptionTypefaceFontRes(R.font.roboto_light)
                .backgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null))
                .imageDrawable(R.drawable.splash_logo)
                .build();
        addSlide(AppIntroFragment.newInstance(page));

        updateFragments = VersionHandler.getUpdateFragments();
        for (Fragment fragment: updateFragments.values()) {
            addSlide(fragment);
        }

        setBarColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null));
        setSkipButtonEnabled(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        for (Map.Entry<String, Fragment> entry: updateFragments.entrySet()) {
            VersionHandler.getVersionSetup(entry.getKey()).handleVersion(entry.getValue());
        }
        PrefHandler.Pref.OLD_RUN_VERSION.set(null);

        setResult(RESULT_OK, new Intent());
        finish();
    }


    public static void checkFirstStart(Activity activity) {
        new Thread(() -> {
            String oldVersion = PrefHandler.Pref.OLD_RUN_VERSION.get(null);

            if (oldVersion != null) {
                Timber.d("First run of this version: %s", VersionHandler.getLocalVersion());
                final Intent i = new Intent(activity, IntroActivity.class);
                new Handler(Looper.getMainLooper()).post(() -> activity.startActivityForResult(i, 1));
            }
        }).start();
    }
}
