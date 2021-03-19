package com.e.insphoto.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.R;
import com.jiajie.load.LoadingDialog;
import com.mengpeng.encrypts.sha1.Sha1EncryptUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends Activity {

    private final static int REGISTER_WRONG = 0;
    private final static int REGISTER_SUCCESS = 4;
    private final static int REGISTER_FAILURE = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void OnClick_returnButton(View v) {
        this.finish();
    }

    public void OnClick_registerButton(View v) {

        EditText accountEditText = this.findViewById(R.id.accountText);
        EditText passwordEditText = this.findViewById(R.id.passwordText);
        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        registerAsync(account, password);
    }

    private void registerAsync(final String account, final String password) {
        final LoadingDialog dialog = new LoadingDialog.Builder(this).loadText("加载中...").build();
        dialog.show();
        String encryptedPassword = Sha1EncryptUtils.sha1Encode(password);
        RequestBody requestBody = new FormBody.Builder()
                .add("account", account)
                .add("password", encryptedPassword)
                .build();
        String url = HttpUtil.BASEURL+"RegisterServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求错误", e.getMessage());
                Looper.prepare();
                Toast.makeText(RegisterActivity.this, "注册出错", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                switch (responseNum) {
                    case REGISTER_SUCCESS: {
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.putExtra("account", account);
                        intent.putExtra("password", password);
                        RegisterActivity.this.setResult(RESULT_OK, intent);
                        dialog.dismiss();
                        RegisterActivity.this.finish();
                        Looper.loop();
                        break;
                    }
                    case REGISTER_FAILURE: {
                        Looper.prepare();
                        Toast.makeText(RegisterActivity.this, "用户名已被注册！", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Looper.loop();
                        break;
                    }
                }
            }
        });
    }
}
