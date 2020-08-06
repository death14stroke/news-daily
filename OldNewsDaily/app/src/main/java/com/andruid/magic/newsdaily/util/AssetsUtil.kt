package com.andruid.magic.newsdaily.util

import android.content.res.AssetManager
import com.blongho.country_data.Country
import com.blongho.country_data.World
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class AssetsUtil {
    companion object {
        private const val ASSET_COUNTRIES = "file:///android_asset/countries.txt"

        @Throws(IOException::class)
        @JvmStatic
        fun getCountries(assetManager: AssetManager?): List<Country> {
            assetManager?.let { manager ->
                val countryCodes = mutableListOf<String>()
                val actualFilename = ASSET_COUNTRIES.split("file:///android_asset/").toTypedArray()[1]
                val labelsInput = manager.open(actualFilename)
                val br = BufferedReader(InputStreamReader(labelsInput))
                var line: String?
                while (br.readLine().also { line = it } != null)
                    line?.apply { countryCodes.add(this) }
                br.close()
                return countryCodes.map { World.getCountryFrom(it) }
            } ?: return emptyList()
        }
    }
}