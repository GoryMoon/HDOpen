package se.gorymoon.hdopen.status;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import se.gorymoon.hdopen.R;

public enum Status {
    OPEN(R.string.open, R.color.open),
    CLOSED(R.string.closed, R.color.closed),
    UNDEFINED(R.string.undefined, R.color.undefined);

    private final int status;
    private final int color;

    Status(int status, int color) {
        this.status = status;
        this.color = color;
    }

    @StringRes
    public int getStatus() {
        return status;
    }

    @ColorRes
    public int getColor() {
        return color;
    }
}
