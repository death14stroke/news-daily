package com.andruid.magic.newsdaily.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

import com.andruid.magic.newsdaily.R;

import java.util.Objects;

public class PrefUtil {
    public static boolean isFirstTime(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.
                        getString(R.string.first_start), context.getResources()
                .getBoolean(R.bool.def_first));
    }

    public static void updateFirstTimePref(Context context){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.first_start), false)
                .apply();
    }

    public static String getDefaultCountry(Context context){
        TelephonyManager telephoneManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return Objects.requireNonNull(telephoneManager).getNetworkCountryIso();
    }
}