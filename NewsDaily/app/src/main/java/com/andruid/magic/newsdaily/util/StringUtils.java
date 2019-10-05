package com.andruid.magic.newsdaily.util;

public class StringUtils {
    public static String capFirstLetter(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}