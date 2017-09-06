
package com.teliver.sdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

@SuppressLint("CommitPrefEdits")
final class TPreference {

    private SharedPreferences mPreferences;

    private Editor mEditor;

    TPreference(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPreferences.edit();
    }

    void storeString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.apply();
    }

    String getString(String key) {
        return mPreferences.getString(key, "");
    }

}
