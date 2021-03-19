package com.e.insphoto.entities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.e.insphoto.R;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.utils.Util;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class PicturePool {
    private static HashMap<Integer, Picture> pictures = new HashMap<>();

    public static boolean addPicture(int id, String url, Context context) {
        if (pictures.containsKey(id)) {
            return false;
        }
        Picture picture = new Picture();
        // 下载图片
        String imagePath = "";
        try {
            File file = Glide.with(context)
                    .load(HttpUtil.BASEURL_PHOTO + url)
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            imagePath = file.getAbsolutePath();
        }
        catch (ExecutionException | InterruptedException e) {
            Uri uri = Util.getUriFromDrawableRes(context, R.drawable.defaultprofile); // 默认图片
            imagePath = uri.getPath();
        }
        picture.setImageID(id);
        picture.setImagePath(imagePath);
        picture.setImageUrl(url);
        picture.setPostTime(Util.convertTimeToDateString(Long.parseLong(url.substring(0, url.length() - 4)))); // 去掉url后面的.jpg
        pictures.put(id, picture);
        return true;
    }

    public static Picture getPicture(int id) {
        return pictures.get(id);
    }

    public static String getPicturePath(int id) {
        return pictures.get(id).getImagePath();
    }
}
