package se.gorymoon.hdopen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.VolleyError;
import com.google.common.base.Joiner;
import com.rodolfonavalon.shaperipplelibrary.ShapeRipple;
import com.rodolfonavalon.shaperipplelibrary.model.Circle;
import com.vdurmont.semver4j.Semver;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import it.sephiroth.android.library.xtooltip.ClosePolicy;
import it.sephiroth.android.library.xtooltip.Tooltip;
import kotlin.Unit;
import se.gorymoon.hdopen.network.StatusRepository;
import se.gorymoon.hdopen.notification.VersionChangeListener;
import se.gorymoon.hdopen.notification.VersionHandler;
import se.gorymoon.hdopen.status.Status;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements VersionChangeListener {

    private static final String DOWNLOAD_URL = "https://gorymoon.se/hdopen";

    @BindView(R.id.status)
    public TextView statusView;

    @BindView(R.id.updated)
    public TextView updatedView;

    @BindView(R.id.background)
    public FrameLayout background;

    @BindView(R.id.progressBar)
    public ProgressBar progressBar;

    @BindView(R.id.info_button)
    public ImageView infoButton;

    @BindView(R.id.ripple)
    public ShapeRipple ripple;

    private Unbinder unbinder;
    private Tooltip tooltip;
    private Semver remoteVersion;

    public static boolean shouldShowTooltip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        IntroActivity.checkFirstStart(this);

        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        background.postDelayed(() -> {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String version = extras.getString("version");
                String changelog = extras.getString("changelog");
                VersionHandler.handleVersionMessage(version, changelog);

                Timber.d("Got data: %s", Joiner.on(", ").join(extras.keySet()));
            }

            new Handler().post(() -> {
                VersionHandler.setListener(this);
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
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.background)
    public void refresh() {
        if ((progressBar.getVisibility() == View.INVISIBLE)) {
            progressBar.setVisibility(View.VISIBLE);
            StatusRepository.getInstance().refreshData();
        }
    }

    @OnClick(R.id.info_button)
    public void infoButton() {
        Timber.d("Clicked info button");
        if (tooltip != null) {
            tooltip.dismiss();
        }

        //Mess... Need to clean up somehow. Change to Kotlin?
        String changelog = " -" + Joiner.on("\n -").join(VersionHandler.getChangelog());
        String message = String.format(String.valueOf(getResources().getText(R.string.version_info)), VersionHandler.getLocalVersion().toString(), remoteVersion.toString(), changelog);
        MaterialDialog dialog = new MaterialDialog(this)
                .title(R.string.new_version, null)
                .message(null, message,
                        false, 1F);

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
        if (progressBar != null && statusView != null && updatedView != null && ripple != null) {
            progressBar.setVisibility(View.INVISIBLE);

            statusView.setText(status.getStatus());
            updatedView.setText(time);
            background.setBackgroundResource(status.getColor());
            ripple.setRippleColor(getResources().getColor(status.getColor()));
            Timber.d("Updated status");
        }
    }

    @Override
    public void onVersionChange(Semver newVersion) {
        this.remoteVersion = newVersion;
        new Handler(Looper.getMainLooper()).post(this::updateVersionInfo);
    }

    private void updateVersionInfo() {
        if (ripple != null && infoButton != null) {
            ripple.setRippleShape(new Circle());
            ripple.setRippleColor(getResources().getColor(R.color.undefined));
            ripple.setVisibility(View.VISIBLE);
            infoButton.setVisibility(View.VISIBLE);

            if (shouldShowTooltip) {
                tooltip = new Tooltip.Builder(this)
                        .anchor(infoButton, 0, 0, false)
                        .text(R.string.new_version)
                        .arrow(true)
                        .fadeDuration(200)
                        .overlay(false)
                        .closePolicy(ClosePolicy.Companion.getTOUCH_INSIDE_NO_CONSUME())
                        .floatingAnimation(Tooltip.Animation.Companion.getSLOW())
                        .create();
                tooltip.show(infoButton, Tooltip.Gravity.LEFT, true);
            }
        }
    }
}
