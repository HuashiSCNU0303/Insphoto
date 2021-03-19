package com.e.insphoto.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.e.insphoto.R;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.HttpUtil;
import com.jiajie.load.LoadingDialog;
import com.jkt.tcompress.OnCompressListener;
import com.jkt.tcompress.TCompress;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ModifyActivity extends Activity {

    private static final int POST_TO_OTHERAPP = 200;
    private static final int POST_IMAGE_SUCCESS = 210;
    private static final int POST_IMAGE_FAILURE = 211;
    ImageView selectedImage;
    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        // 初始图片
        selectedImage = findViewById(R.id.selectedImage);
        imagePath = getIntent().getStringExtra("url");
        Glide.with(this).load(imagePath).into(selectedImage);

    }

    // 发布照片
    public void OnClick_postButton(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(new String[]{"分享到Insphoto", "分享到其他应用"}, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                if (which == 0) {
                    // 表示分享到本应用
                    TCompress tCompress = new TCompress.Builder()
                            .setMaxWidth(1920)
                            .setMaxHeight(1080)
                            .setQuality(70)
                            .setFormat(Bitmap.CompressFormat.JPEG)
                            .setConfig(Bitmap.Config.RGB_565)
                            .build();
                    EditText editDescription = findViewById(R.id.editDescription);
                    final String description = editDescription.getText().toString();
                    tCompress.compressToFileAsync(new File(imagePath), new OnCompressListener<File>() {
                        @Override
                        public void onCompressFinish(boolean success, final File file) {
                            if (success) {
                                final LoadingDialog loadingDialog = new LoadingDialog.Builder(ModifyActivity.this).loadText("照片上传中...").build();
                                loadingDialog.show();
                                HttpUtil.sendPhotoRequest(Constant.currentUser.getID(), file.getAbsolutePath(), description, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.e("图片上传错误", e.getMessage());
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                        String responseData = response.body().string();
                                        JSONObject jsonObject = JSON.parseObject(responseData);
                                        int responseNum = jsonObject.getInteger("Result");
                                        if (responseNum == POST_IMAGE_SUCCESS) {
                                            ModifyActivity.this.setResult(RESULT_OK);
                                            loadingDialog.dismiss();
                                            dialog.dismiss();
                                            ModifyActivity.this.finish();
                                        }
                                        else {
                                            Looper.prepare();
                                            Toast.makeText(ModifyActivity.this,"照片上传失败！",Toast.LENGTH_LONG).show();
                                            String ex = jsonObject.getString("Exception");
                                            Log.e("图片上传失败", ex+" ");
                                            loadingDialog.dismiss();
                                            dialog.dismiss();
                                            Looper.loop();
                                        }
                                    }
                                });
                            }
                            else {
                                Log.e("图片压缩失败", " ");
                            }
                        }
                    });
                }
                else {
                    // 分享到其他应用，不需要上传到数据库
                    // 调用系统自带分享功能即可
                    Intent imageIntent = new Intent(Intent.ACTION_SEND);
                    imageIntent.setType("image/jpeg");
                    imageIntent.putExtra(Intent.EXTRA_STREAM, imagePath);
                    startActivity(Intent.createChooser(imageIntent, "分享"));
                    dialog.dismiss();
                    // 回到原来的界面
                    ModifyActivity.this.finish();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }
}
