package com.e.insphoto.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    // 获得drawable内图片的路径
    public static Uri getUriFromDrawableRes(Context context, int id) {
        Resources resources = context.getResources();
        String path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + resources.getResourcePackageName(id) + "/"
                + resources.getResourceTypeName(id) + "/"
                + resources.getResourceEntryName(id);
        return Uri.parse(path);
    }

    public static String convertTimeToDateString(long MilliSeconds) {
        DateFormat format = SimpleDateFormat.getDateTimeInstance();
        return format.format(new Date(MilliSeconds));
    }

    public static Date convertTimeToDate(long MilliSeconds) {
        DateFormat format = SimpleDateFormat.getDateTimeInstance();
        return new Date(MilliSeconds);
    }

    public static long convertTimeToLong(String time) {
        try {
            DateFormat format = SimpleDateFormat.getDateTimeInstance();
            return format.parse(time).getTime();
        }
        catch (ParseException e) {
            return -1;
        }
    }

    public static String getPathFromFilepath(final String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }
}
