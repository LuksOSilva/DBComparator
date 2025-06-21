package com.luksosilva.dbcomparator.util;

import java.io.File;
import java.security.KeyStore;
import java.util.Objects;

public class util {

    public static String getCanonicalPath(File file) {
        try{

            return file.getCanonicalPath();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertBooleanToString(boolean bool) {
        return bool ? "Y" : "N";
    }

    public static boolean convertStringToBoolean(String str) {
        return str.equals("Y");
    }
}
