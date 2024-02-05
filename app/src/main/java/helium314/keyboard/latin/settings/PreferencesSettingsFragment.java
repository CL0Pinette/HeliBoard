/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.latin.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.inputmethod.InputMethodSubtype;

import androidx.preference.Preference;

import helium314.keyboard.keyboard.KeyboardLayoutSet;
import helium314.keyboard.keyboard.KeyboardSwitcher;
import helium314.keyboard.latin.AudioAndHapticFeedbackManager;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.RichInputMethodManager;
import helium314.keyboard.latin.utils.PopupKeysUtilsKt;
import helium314.keyboard.latin.utils.SubtypeSettingsKt;
import helium314.keyboard.latin.utils.SubtypeUtilsKt;
import helium314.keyboard.latin.utils.ToolbarUtilsKt;

import kotlin.collections.ArraysKt;

public final class PreferencesSettingsFragment extends SubScreenFragment {

    private boolean mReloadKeyboard = false;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_screen_preferences);

        final Resources res = getResources();
        final Context context = getActivity();

        // When we are called from the Settings application but we are not already running, some
        // singleton and utility classes may not have been initialized.  We have to call
        // initialization method of these classes here. See {@link LatinIME#onCreate()}.
        RichInputMethodManager.init(context);

        if (!AudioAndHapticFeedbackManager.getInstance().hasVibrator()) {
            removePreference(Settings.PREF_VIBRATE_ON);
            removePreference(Settings.PREF_VIBRATION_DURATION_SETTINGS);
        }
        if (!Settings.readFromBuildConfigIfToShowKeyPreviewPopupOption(res)) {
            removePreference(Settings.PREF_POPUP_ON);
        }

        setupKeypressVibrationDurationSettings();
        setupKeypressSoundVolumeSettings();
        setupHistoryRetentionTimeSettings();
        refreshEnablingsOfKeypressSoundAndVibrationAndHistRetentionSettings();
        setLocalizedNumberRowVisibility();
        findPreference(Settings.PREF_POPUP_KEYS_LABELS_ORDER).setVisible(getSharedPreferences().getBoolean(Settings.PREF_SHOW_HINTS, false));
        findPreference(Settings.PREF_POPUP_KEYS_ORDER).setOnPreferenceClickListener((pref) -> {
            PopupKeysUtilsKt.reorderPopupKeysDialog(requireContext(), Settings.PREF_POPUP_KEYS_ORDER, PopupKeysUtilsKt.POPUP_KEYS_ORDER_DEFAULT, R.string.popup_order);
            return true;
        });
        findPreference(Settings.PREF_POPUP_KEYS_LABELS_ORDER).setOnPreferenceClickListener((pref) -> {
            PopupKeysUtilsKt.reorderPopupKeysDialog(requireContext(), Settings.PREF_POPUP_KEYS_LABELS_ORDER, PopupKeysUtilsKt.POPUP_KEYS_LABEL_DEFAULT, R.string.hint_source);
            return true;
        });
        findPreference(Settings.PREF_TOOLBAR_KEYS).setOnPreferenceClickListener((pref) -> {
            PopupKeysUtilsKt.reorderPopupKeysDialog(requireContext(), Settings.PREF_TOOLBAR_KEYS, ToolbarUtilsKt.getDefaultToolbarPref(), R.string.toolbar_keys);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        refreshEnablingsOfKeypressSoundAndVibrationAndHistRetentionSettings();
        if (key == null) return;
        switch (key) {
            case Settings.PREF_POPUP_KEYS_ORDER, Settings.PREF_SHOW_POPUP_HINTS, Settings.PREF_SHOW_NUMBER_ROW,
                    Settings.PREF_POPUP_KEYS_LABELS_ORDER, Settings.PREF_TOOLBAR_KEYS
                    -> mReloadKeyboard = true;
            case Settings.PREF_LOCALIZED_NUMBER_ROW -> KeyboardLayoutSet.onSystemLocaleChanged();
            case Settings.PREF_SHOW_HINTS
                    -> findPreference(Settings.PREF_POPUP_KEYS_LABELS_ORDER).setVisible(prefs.getBoolean(Settings.PREF_SHOW_HINTS, false));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReloadKeyboard)
            KeyboardSwitcher.getInstance().forceUpdateKeyboardTheme(requireContext());
        mReloadKeyboard = false;
    }

    private void setLocalizedNumberRowVisibility() {
        final Preference pref = findPreference(Settings.PREF_LOCALIZED_NUMBER_ROW);
        if (pref == null) return;
        // locales that have a number row defined (not good to have it hardcoded, but reading a bunch of files may be noticeably slow)
        final String[] numberRowLocales = new String[] { "ar", "bn", "fa", "gu", "hi", "mr", "ne", "ur" };
        for (final InputMethodSubtype subtype : SubtypeSettingsKt.getEnabledSubtypes(getSharedPreferences(), true)) {
            if (ArraysKt.any(numberRowLocales, (l) -> l.equals(SubtypeUtilsKt.locale(subtype).getLanguage()))) {
                pref.setVisible(true);
                return;
            }
        }
        pref.setVisible(false);
    }

    private void refreshEnablingsOfKeypressSoundAndVibrationAndHistRetentionSettings() {
        final SharedPreferences prefs = getSharedPreferences();
        final Resources res = getResources();
        setPreferenceVisible(Settings.PREF_VIBRATION_DURATION_SETTINGS,
                Settings.readVibrationEnabled(prefs, res));
        setPreferenceVisible(Settings.PREF_KEYPRESS_SOUND_VOLUME,
                Settings.readKeypressSoundEnabled(prefs, res));
        setPreferenceVisible(Settings.PREF_CLIPBOARD_HISTORY_RETENTION_TIME,
                Settings.readClipboardHistoryEnabled(prefs));
    }

    private void setupKeypressVibrationDurationSettings() {
        final SeekBarDialogPreference pref = findPreference(
                Settings.PREF_VIBRATION_DURATION_SETTINGS);
        if (pref == null) {
            return;
        }
        final SharedPreferences prefs = getSharedPreferences();
        final Resources res = getResources();
        pref.setInterface(new SeekBarDialogPreference.ValueProxy() {
            @Override
            public void writeValue(final int value, final String key) {
                prefs.edit().putInt(key, value).apply();
            }

            @Override
            public void writeDefaultValue(final String key) {
                prefs.edit().remove(key).apply();
            }

            @Override
            public int readValue(final String key) {
                return Settings.readKeypressVibrationDuration(prefs, res);
            }

            @Override
            public int readDefaultValue(final String key) {
                return Settings.readDefaultKeypressVibrationDuration(res);
            }

            @Override
            public void feedbackValue(final int value) {
                AudioAndHapticFeedbackManager.getInstance().vibrate(value);
            }

            @Override
            public String getValueText(final int value) {
                if (value < 0) {
                    return res.getString(R.string.settings_system_default);
                }
                return res.getString(R.string.abbreviation_unit_milliseconds, Integer.toString(value));
            }
        });
    }

    private void setupKeypressSoundVolumeSettings() {
        final SeekBarDialogPreference pref = findPreference(
                Settings.PREF_KEYPRESS_SOUND_VOLUME);
        if (pref == null) {
            return;
        }
        final SharedPreferences prefs = getSharedPreferences();
        final Resources res = getResources();
        final AudioManager am = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        pref.setInterface(new SeekBarDialogPreference.ValueProxy() {
            private static final float PERCENTAGE_FLOAT = 100.0f;

            private float getValueFromPercentage(final int percentage) {
                return percentage / PERCENTAGE_FLOAT;
            }

            private int getPercentageFromValue(final float floatValue) {
                return (int)(floatValue * PERCENTAGE_FLOAT);
            }

            @Override
            public void writeValue(final int value, final String key) {
                prefs.edit().putFloat(key, getValueFromPercentage(value)).apply();
            }

            @Override
            public void writeDefaultValue(final String key) {
                prefs.edit().remove(key).apply();
            }

            @Override
            public int readValue(final String key) {
                return getPercentageFromValue(Settings.readKeypressSoundVolume(prefs, res));
            }

            @Override
            public int readDefaultValue(final String key) {
                return getPercentageFromValue(Settings.readDefaultKeypressSoundVolume(res));
            }

            @Override
            public String getValueText(final int value) {
                if (value < 0) {
                    return res.getString(R.string.settings_system_default);
                }
                return Integer.toString(value);
            }

            @Override
            public void feedbackValue(final int value) {
                am.playSoundEffect(
                        AudioManager.FX_KEYPRESS_STANDARD, getValueFromPercentage(value));
            }
        });
    }

    private void setupHistoryRetentionTimeSettings() {
        final SharedPreferences prefs = getSharedPreferences();
        final Resources res = getResources();
        final SeekBarDialogPreference pref = findPreference(
                Settings.PREF_CLIPBOARD_HISTORY_RETENTION_TIME);
        if (pref == null) {
            return;
        }
        pref.setInterface(new SeekBarDialogPreference.ValueProxy() {
            @Override
            public void writeValue(final int value, final String key) {
                prefs.edit().putInt(key, value).apply();
            }

            @Override
            public void writeDefaultValue(final String key) {
                prefs.edit().remove(key).apply();
            }

            @Override
            public int readValue(final String key) {
                return Settings.readClipboardHistoryRetentionTime(prefs, res);
            }

            @Override
            public int readDefaultValue(final String key) {
                return Settings.readDefaultClipboardHistoryRetentionTime(res);
            }

            @Override
            public String getValueText(final int value) {
                if (value <= 0) {
                    return res.getString(R.string.settings_no_limit);
                }
                return res.getString(R.string.abbreviation_unit_minutes, Integer.toString(value));
            }

            @Override
            public void feedbackValue(final int value) {}
        });
    }
}
