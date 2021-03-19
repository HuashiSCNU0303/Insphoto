package com.e.insphoto.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.e.insphoto.entities.UserPool;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.R;
import com.e.insphoto.entities.User;
import com.jiajie.load.LoadingDialog;
import com.mengpeng.encrypts.sha1.Sha1EncryptUtils;

public class LoginActivity extends Activity
{
    public static String accountFile;
    private final static int LOGIN_WRONG = 0;
    private final static int LOGIN_SUCCESS = 1;
    private final static int PASSWORD_WRONG = 2;
    private final static int ACCOUNT_NOT_EXIST = 3;
    private final static int REGISTER_ID = 10;

    EditText accountEditText, passwordEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountEditText = this.findViewById(R.id.accountText);
        passwordEditText = this.findViewById(R.id.passwordText);
    }

    //登录按钮点击事件
    public void onClick_logButton(View v) throws InterruptedException {

        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        password = Sha1EncryptUtils.sha1Encode(password);
        Log.e("加密后", password);
        if (account.length() * password.length() == 0)
        {
            Toast.makeText(LoginActivity.this, "账号或密码为空", Toast.LENGTH_SHORT).show();
            return;
        }

        loginAsync(account, password);
    }

    // 登录
    public void loginAsync(final String account, String password) {
        final LoadingDialog dialog = new LoadingDialog.Builder(this).loadText("加载中...").build();
        dialog.show();
        RequestBody requestBody = new FormBody.Builder()
                .add("account", account)
                .add("password", password)
                .build();
        String url = HttpUtil.BASEURL+"LoginServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求错误", e.getMessage());
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "登录出错", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                switch (responseNum) {
                    case LOGIN_SUCCESS: {
                        Looper.prepare();
                        int userId = jsonObject.getInteger("UserId");
                        Constant.currentUser = UserPool.addUser(userId, LoginActivity.this);
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, AppActivity.class);
                        dialog.dismiss();
                        startActivity(intent);
                        LoginActivity.this.finish();
                        Looper.loop();
                        return;
                    }
                    case PASSWORD_WRONG: {
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Looper.loop();
                        break;
                    }
                    case ACCOUNT_NOT_EXIST: {
                        Looper.prepare();
                        Toast.makeText(LoginActivity.this, "账号不存在", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Looper.loop();
                        break;
                    }
                }
            }
        });
    }

    //登录后将账号及密码记录至本地
    static void recordLogin(String account,String password)
    {
        try
        {
            FileOutputStream out=new FileOutputStream(new File(accountFile));
            for(int i=0;i<account.length();i++)
            {
                out.write(account.charAt(i));
            }
            out.write('#');
            for(int i=0;i<password.length();i++)
            {
                out.write(password.charAt(i));
            }
            out.close();
        }
        catch (Exception ex)
        {

        }
    }

    //查找本地存储的账号与密码
    /*static boolean verifyLocalAccount()
    {
        try
        {
            File file=new File(accountFile);
            FileInputStream inputStream=new FileInputStream(file);
            String str="";
            int t=inputStream.read();
            while(t!=-1)
            {
                str += (char)t;
                t=inputStream.read();
            }
            inputStream.close();

            String account="";
            String password="";
            for(int i=0;i<str.length();i++)
            {
                if(str.charAt(i)=='#')
                {
                    account=str.substring(0, i-1);
                    password=str.substring(i+1,str.length()-1);
                    break;
                }
            }
            return verifyAccount(account, password);
        }
        catch (Exception ex)
        {
            return false;
        }
    }*/

    // 请求注册账号
    public void OnClick_registerButton(View v)
    {
        Intent intent=new Intent();
        intent.setClass(LoginActivity.this, RegisterActivity.class);
        startActivityForResult(intent, REGISTER_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // 注册成功后，把注册的用户名和密码自动填到账号密码里面去
        if (requestCode == REGISTER_ID) {
            if (resultCode == RESULT_OK) {
                accountEditText.setText(intent.getStringExtra("account"));
                passwordEditText.setText(intent.getStringExtra("password"));
            }
        }
    }
}