package com.e.insphoto.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.e.insphoto.R;

import java.io.File;

//
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginActivity.accountFile=getFilesDir().toString()+"/account";
        autoLogin(null);
    }

    public void autoLogin(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
}
