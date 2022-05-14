package se.gorymoon.hdopen.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.work.ListenableWorker;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.rodolfonavalon.shaperipplelibrary.model.Circle;
import com.vdurmont.semver4j.Semver;

import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;
import kotlin.Unit;
import se.gorymoon.hdopen.R;
import se.gorymoon.hdopen.ads.AdManager;
import se.gorymoon.hdopen.databinding.ActivityMainBinding;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.utils.NotificationHandler;
import se.gorymoon.hdopen.utils.PrefHandler;
import se.gorymoon.hdopen.utils.Status;
import se.gorymoon.hdopen.utils.Utils;
import se.gorymoon.hdopen.version.VersionHandler;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String DOWNLOAD_URL = "https://gorymoon.se/hdopen";

    private long lastRefreshClickTime = 0;
    private ActivityMainBinding binding;
    private Tooltip tooltip;
    private Semver remoteVersion;

    public boolean shouldShowTooltip;

    private AdManager adManager;
    private Handler adHandler;
    private Runnable adRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntroActivity.checkFirstStart(this);
        Utils.setActionBar(getApplicationContext(), getSupportActionBar());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.background.setOnClickListener(v -> refresh());
        binding.infoButton.setOnClickListener(this::infoButtonClick);

        Bundle extras = getIntent().getExtras();
        binding.background.postDelayed(() -> {
            if (extras != null) {
                String string = extras.getString(NotificationHandler.NOTIFICATION_EXTRA);
                if (string != null && string.equals(VersionHandler.NEW_VERSION_TAG)) {
                    shouldShowTooltip = true;
                }
                Timber.d("Got data: %s", TextUtils.join(", ", extras.keySet()));
            }
            new Handler().post(() -> {
                VersionHandler.setListener(this::onVersionChange);
                if (VersionHandler.isOutdated()) {
                    onVersionChange(VersionHandler.getRemoteVersion());
                }
            });
        }, 200);

        StatusRepository.getInstance().addListener("app", this::setAppStatus);
        StatusRepository.getInstance().addErrorListener("app", this::error);

        refresh();
    }

    @Override
    protected void onStart() {
        showAds();
        super.onStart();
    }

    @Override
    protected void onResume() {
        showAds();
        super.onResume();
    }

    @Override
    protected void onStop() {
        removeAds();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        removeAds();
        super.onDestroy();
    }

    private void showAds() {
        if (PrefHandler.Pref.ENABLE_ADS.get(true)) {
            if (adManager == null)
                adManager = new AdManager();
            if (adHandler == null)
                adHandler = new Handler();
            else if (adRunnable != null)
                adHandler.removeCallbacks(adRunnable);

            Activity activity = this;
            adRunnable = () -> adManager.fetchAd().handle((adResponse, throwable) -> {
                if (adResponse != null)
                    Glide.with(activity).load(adResponse.image).into(binding.adView);

                if (adHandler != null)
                    adHandler.postDelayed(adRunnable, 7500);

                return ListenableWorker.Result.success();
            });
            adHandler.postDelayed(adRunnable, 100);
        } else {
            removeAds();
        }
    }

    private void removeAds() {
        if (adHandler != null && adRunnable != null)
            adHandler.removeCallbacks(adRunnable);

        binding.adView.setImageBitmap(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        if ((binding.progressBar.getVisibility() == View.INVISIBLE)) {
            if (SystemClock.elapsedRealtime() - lastRefreshClickTime >= 1000) {
                lastRefreshClickTime = SystemClock.elapsedRealtime();
                binding.progressBar.setVisibility(View.VISIBLE);
                StatusRepository.getInstance().refreshData(getApplicationContext());
            }
        }
    }

    public void infoButtonClick(View v) {
        Timber.d("Clicked info button");
        if (tooltip != null) {
            tooltip.dismiss();
        }

        //Mess... Need to clean up somehow. Change to Kotlin?
        String changelog = " -" + TextUtils.join("\n -", VersionHandler.getChangelog());
        String message = String.format(String.valueOf(getResources().getText(R.string.version_info)), VersionHandler.getLocalVersion().toString(), remoteVersion.toString(), changelog);
        MaterialDialog dialog = new MaterialDialog(this, MaterialDialog.getDEFAULT_BEHAVIOR())
                .title(R.string.new_version, null)
                .message(null, message, null);

        dialog.positiveButton(R.string.download_button, null, materialDialog -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_URL));
            startActivity(browserIntent);
            return Unit.INSTANCE;
        });
        dialog.negativeButton(R.string.close, null, null);
        dialog.show();
    }


    private void error(VolleyError error) {
        setAppStatus(Status.UNDEFINED, "");
    }

    private void setAppStatus(Status status, String time) {
        if (binding.progressBar != null && binding.status != null && binding.updated != null && binding.ripple != null) {
            binding.progressBar.setVisibility(View.INVISIBLE);

            binding.status.setText(status.getStatus());
            binding.updated.setText(time);
            binding.background.setBackgroundResource(status.getColor());
            binding.ripple.setRippleColor(getResources().getColor(status.getColor()));
            Timber.d("Updated status");
        }
    }

    private void onVersionChange(Semver newVersion) {
        this.remoteVersion = newVersion;
        new Handler(Looper.getMainLooper()).post(this::updateVersionInfo);
    }

    private void updateVersionInfo() {
        if (binding.ripple != null && binding.infoButton != null) {
            binding.ripple.setRippleShape(new Circle());
            binding.ripple.setRippleColor(getResources().getColor(R.color.undefined));
            binding.ripple.setVisibility(View.VISIBLE);
            binding.infoButton.setVisibility(View.VISIBLE);

            if (shouldShowTooltip) {
                shouldShowTooltip = false;
                tooltip = new Tooltip.Builder(this)
                        .anchor(binding.infoButton, 0, 0, false)
                        .text(R.string.new_version)
                        .arrow(true)
                        .overlay(false)
                        .typeface(ResourcesCompat.getFont(getApplicationContext(), R.font.roboto_light))
                        .styleId(R.style.CustomTooltip)
                        .closePolicy(ClosePolicy.Companion.getTOUCH_INSIDE_NO_CONSUME())
                        .floatingAnimation(Tooltip.Animation.Companion.getSLOW())
                        .create();
                tooltip.show(binding.infoButton, Tooltip.Gravity.LEFT, true);
            }
        }
    }
}
