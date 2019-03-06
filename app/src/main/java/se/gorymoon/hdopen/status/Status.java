package se.gorymoon.hdopen.status;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import se.gorymoon.hdopen.R;

public enum Status {
    OPEN(0, R.string.open, R.color.open),
    CLOSED(1, R.string.closed, R.color.closed),
    UNDEFINED(2, R.string.undefined, R.color.undefined);

    private int id;
    @StringRes
    private final int status;
    @ColorRes
    private final int color;

    Status(int id, @StringRes int status, @ColorRes int color) {
        this.id = id;
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

    public int getId() {
        return id;
    }

    public static Status getFromId(int id) {
        for (Status val: Status.values()) {
            if (val.getId() == id) {
                return val;
            }
        }
        return UNDEFINED;
    }
}
