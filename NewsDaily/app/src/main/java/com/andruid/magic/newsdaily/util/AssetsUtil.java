package com.andruid.magic.newsdaily.util;

import android.content.res.AssetManager;

import com.blongho.country_data.Country;
import com.blongho.country_data.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AssetsUtil {
    private static final String ASSET_COUNTRIES = "file:///android_asset/countries.txt";

    public static List<Country> getCountries(AssetManager assetManager) throws IOException {
        List<String> countryCodes = new ArrayList<>();
        String actualFilename = ASSET_COUNTRIES.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null)
            countryCodes.add(line);
        br.close();
        List<Country> countries = new ArrayList<>();
        for(String code : countryCodes)
            countries.add(World.getCountryFrom(code));
        return countries;
    }
}