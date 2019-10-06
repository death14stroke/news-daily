package com.andruid.magic.newsdaily.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    static String capFirstLetter(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("general");
        categories.add("business");
        categories.add("entertainment");
        categories.add("health");
        categories.add("science");
        categories.add("sports");
        categories.add("technology");
        return categories;
    }
}