package com.andruid.magic.newsdaily.util;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PrefUtil {
    public static String getDefaultCountry(Context context){
        TelephonyManager telephoneManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        return telephoneManager.getNetworkCountryIso();
    }
}