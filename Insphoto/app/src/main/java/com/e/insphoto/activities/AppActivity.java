package com.e.insphoto.activities;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.e.insphoto.services.JWebSocketService;
import com.e.insphoto.utils.MyViewPager;
import com.e.insphoto.R;
import com.e.insphoto.fragments.IndexPageFragment;
import com.e.insphoto.fragments.MessagePageFragment;
import com.e.insphoto.fragments.MinePageFragment;
import com.google.android.material.tabs.TabLayout;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

public class AppActivity extends FragmentActivity {

    private MyViewPager mViewPager;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    private TabLayout mTabLayout;
    Intent webSocketService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        initView();
        checkPermission();
        webSocketService = new Intent(AppActivity.this, JWebSocketService.class);
        startService(webSocketService);
    }

    private void checkPermission() {
        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
        rxPermissions
                .requestEach(
                        android.Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {

                        }
                        else if (permission.shouldShowRequestPermissionRationale) {
                            Toast.makeText(AppActivity.this, "求求你给个权限吧", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AppActivity.this, "不给就算了", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initView() {
        mViewPager = findViewById(R.id.viewPager);
        mTabLayout = findViewById(R.id.tab_layout);
        mFragments = new ArrayList<>(3);
        mFragments.add(new IndexPageFragment());
        mFragments.add(new MessagePageFragment());
        mFragments.add(new MinePageFragment());
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //选中某个tab
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 1) {
                    Intent intent = new Intent();
                    intent.setAction("UPDATE_DIALOG_LIST");
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTabLayout.getSelectedTabPosition() == 1) {
            Intent intent = new Intent();
            intent.setAction("UPDATE_DIALOG_LIST");
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("AppActivity", "已回收");
        stopService(webSocketService);
    }

    public class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }
    }
}


