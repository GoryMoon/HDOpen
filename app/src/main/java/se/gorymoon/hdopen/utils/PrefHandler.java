package se.gorymoon.hdopen.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import se.gorymoon.hdopen.App;
import se.gorymoon.hdopen.status.Status;

public class PrefHandler {

    private static PrefHandler instance;
    private final SharedPreferences sharedPreferences;
    private boolean bulk;
    private SharedPreferences.Editor editor;

    private PrefHandler() {
        sharedPreferences = App.getInstance().getSharedPreferences("HDOpen", Context.MODE_PRIVATE);
    }

    public static PrefHandler getInstance() {
        if (instance == null) {
            instance = new PrefHandler();
        }
        return instance;
    }

    private SharedPreferences.Editor edit() {
        if (this.editor == null) {
            this.editor = sharedPreferences.edit();
        }
        return this.editor;
    }

    private void apply(SharedPreferences.Editor editor) {
        if (!this.bulk) {
            commitBulk();
        }
    }

    public void startBulk() {
        this.bulk = true;
    }

    public void commitBulk() {
        this.bulk = false;
        if (this.editor != null) {
            this.editor.apply();
        }
    }

    public static class Pref<E> {
        private static final PrefGetter<String> STRING_GETTER = (pref, defaultVal, preferences) -> preferences.getString(pref.key, defaultVal);
        private static final PrefSetter<String> STRING_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putString(pref.key, val));

        private static final PrefGetter<Set<String>> STRING_SET_GETTER = (pref, defaultVal, preferences) -> preferences.getStringSet(pref.key, defaultVal);
        private static final PrefSetter<Set<String>> STRING_SET_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putStringSet(pref.key, val));

        private static final PrefGetter<Boolean> BOOLEAN_GETTER = (pref, defaultVal, preferences) -> preferences.getBoolean(pref.key, defaultVal);
        private static final PrefSetter<Boolean> BOOLEAN_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putBoolean(pref.key, val));

        private static final PrefGetter<Float> FLOAT_GETTER = (pref, defaultVal, preferences) -> preferences.getFloat(pref.key, defaultVal);
        private static final PrefSetter<Float> FLOAT_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putFloat(pref.key, val));

        private static final PrefGetter<Integer> INT_GETTER = (pref, defaultVal, preferences) -> preferences.getInt(pref.key, defaultVal);
        private static final PrefSetter<Integer> INT_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putInt(pref.key, val));

        private static final PrefGetter<Long> LONG_GETTER = (pref, defaultVal, preferences) -> preferences.getLong(pref.key, defaultVal);
        private static final PrefSetter<Long> LONG_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putLong(pref.key, val));

        private static final PrefGetter<Status> STATUS_GETTER = (pref, defaultVal, preferences) -> Status.getFromId(preferences.getInt(pref.key, defaultVal.getId()));
        private static final PrefSetter<Status> STATUS_SETTER = (pref, val, preferences) -> preferences.apply(preferences.edit().putInt(pref.key, val.getId()));

        public static final Pref<String> REMOTE_VERSION = new Pref<>("remote_version", STRING_GETTER, STRING_SETTER);
        public static final Pref<Set<String>> CHANGELOG = new Pref<>("changelog", STRING_SET_GETTER, STRING_SET_SETTER);
        public static final Pref<Boolean> FIRST_RUN = new Pref<>("first_run", BOOLEAN_GETTER, BOOLEAN_SETTER);
        public static final Pref<Status> STATUS = new Pref<>("status", STATUS_GETTER, STATUS_SETTER);

        private String key;
        private PrefGetter<E> internalGetter;
        private PrefSetter<E> internalSetter;

        private Pref(String key, PrefGetter<E> internalGetter, PrefSetter<E> internalSetter) {
            this.key = key;
            this.internalGetter = internalGetter;
            this.internalSetter = internalSetter;
        }

        public E get(E defaultVal) {
            return internalGetter.get(this, defaultVal, getInstance().sharedPreferences);
        }

        public void set(E val) {
            internalSetter.set(this, val, getInstance());
        }

    }

    private interface PrefGetter<E> {
        E get(Pref pref, E defaultVal, SharedPreferences preferences);
    }

    private interface PrefSetter<E> {
        void set(Pref pref, E val, PrefHandler preferences);
    }
}
