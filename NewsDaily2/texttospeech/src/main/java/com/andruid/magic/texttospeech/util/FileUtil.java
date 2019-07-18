package com.andruid.magic.texttospeech.util;

public class FileUtil {
    public static String getFileName(String utteranceId){
        return "news_"+utteranceId+".mp3";
    }

    public static String getUtteranceId(String fileName){
        return fileName.substring(5, fileName.length()-4);
    }
}