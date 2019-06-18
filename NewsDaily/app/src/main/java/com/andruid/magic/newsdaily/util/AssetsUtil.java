package com.andruid.magic.newsdaily.util;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.andruid.magic.newsdaily.data.Constants.LABEL_FILE;

public class AssetsUtil {
    public static List<String> readCategories(AssetManager assetManager) throws IOException {
        List<String> categories = new ArrayList<>();
        String actualFilename = LABEL_FILE.split("file:///android_asset/")[1];
        InputStream labelsInput = assetManager.open(actualFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
        String line;
        while ((line = br.readLine()) != null)
            categories.add(line);
        br.close();
        return categories;
    }
}