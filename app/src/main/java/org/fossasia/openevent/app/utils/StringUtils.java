package org.fossasia.openevent.app.utils;

public final class StringUtils {

    private StringUtils() {
    }

    public static String emptyToNull(String str) {
        if (str == null || str.isEmpty())
            return null;
        else
            return str;
    }
}
