package com.e.insphoto.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {
    // 全局OKHttpClient对象
    static OkHttpClient okHttpClient = new OkHttpClient();

    // 云服务器url，用的时候后面再接具体的Servlet
    public static final String BASEURL = "http://47.105.203.231:8080/Insphoto/";

    // 访问图片时的云服务器url
    public static final String BASEURL_PHOTO = "http://47.105.203.231:8080/resource/Insphoto/";

    public static void sendPostRequest(String url, RequestBody requestBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public static void sendGetRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    // 发照片
    public static void sendPhotoRequest(int userId, String imagePath, String description, Callback callback) {
        String url = BASEURL+"ReceivePhotoServlet";
        File file = new File(imagePath);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("description", getEncodedString(description))
                .addFormDataPart(
                        "img",
                        file.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"),file)
                );
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call =  okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    // 发头像
    public static void sendProfileRequest(int userId, String imagePath, boolean status, Callback callback) {
        String url = BASEURL+"ReceiveProfileServlet";
        File file = new File(imagePath);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("status", String.valueOf(status))
                .addFormDataPart(
                        "img",
                        file.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"),file)
                );
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call =  okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    private static String getEncodedString(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}