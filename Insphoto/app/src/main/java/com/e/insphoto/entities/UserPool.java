package com.e.insphoto.entities;

import android.content.Context;
import android.net.Uri;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.e.insphoto.R;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserPool {
    private static HashMap<Integer, User> users = new HashMap<>();

    static final int GET_USER_INFO_SUCCESS = 51;
    static final int GET_USER_INFO_FAILURE = 52;
    public static User addUser(final int id, Context context) {
        if (users.containsKey(id)) {
            return getUser(id);
        }
        // 获取用户所有信息
        final User user = new User();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(id))
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "GetUserInfoServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == GET_USER_INFO_SUCCESS) {
                    Map<String, String> map = JSONObject.parseObject(jsonObject.getJSONObject("info").toJSONString(), new TypeReference<Map<String, String>>() {});
                    user.setID(id);
                    user.setProfileName(map.get("name"));
                    user.setProfileImgUrl(map.get("profileimgurl"));
                    user.setAccount(map.get("account"));
                    user.setDescription(map.get("description"));
                }
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        }
        catch (InterruptedException e) {
        }

        // 下载头像图片
        try {
            File file = Glide.with(context)
                    .load(HttpUtil.BASEURL_PHOTO + user.getProfileImgUrl())
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
            user.setProfileImgPath(file.getAbsolutePath());
        }
        catch (ExecutionException | InterruptedException e) {
            Uri uri = Util.getUriFromDrawableRes(context, R.drawable.defaultprofile);
            user.setProfileImgPath(uri.getPath()); // 设置为默认头像
        }
        users.put(id, user);
        return user;
    }

    public static User getUser(int id) {
        return users.get(id);
    }
}
