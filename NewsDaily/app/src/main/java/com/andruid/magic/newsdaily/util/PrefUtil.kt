package com.andruid.magic.newsdaily.util

import android.content.Context
import android.telephony.TelephonyManager
import androidx.preference.PreferenceManager
import com.andruid.magic.newsdaily.R

class PrefUtil {
    companion object {
        @JvmStatic
        fun isFirstTime(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.first_start),
                            context.resources.getBoolean(R.bool.def_first))
        }

        @JvmStatic
        fun updateFirstTimePref(context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(context.getString(R.string.first_start), false)
                    .apply()
        }

        @JvmStatic
        fun getDefaultCountry(context: Context?): String {
            val telephoneManager : TelephonyManager? = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephoneManager?.networkCountryIso ?: "in"
        }
    }
}