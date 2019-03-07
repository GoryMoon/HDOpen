package se.gorymoon.hdopen.utils;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.res.ResourcesCompat;
import se.gorymoon.hdopen.R;

public class Utils {

    public static void setActionBar(Context context, ActionBar supportActionBar) {
        TextView v = new TextView(context);
        v.setLayoutParams(new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        v.setText(R.string.app_name);
        v.setTextSize(20);
        v.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.white, null));
        v.setTypeface(ResourcesCompat.getFont(context, R.font.roboto_light));
        supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        supportActionBar.setCustomView(v);
    }

}
