package com.andruid.magic.newsdaily.util;

public class StringUtil {
    public static String capFirstLetter(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}