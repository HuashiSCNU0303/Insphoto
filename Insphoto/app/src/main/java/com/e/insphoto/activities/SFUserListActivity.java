package com.e.insphoto.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.e.insphoto.R;
import com.e.insphoto.adapters.PictureAdapter;
import com.e.insphoto.adapters.UserAdapter;
import com.e.insphoto.entities.Picture;
import com.e.insphoto.entities.PicturePool;
import com.e.insphoto.entities.User;
import com.e.insphoto.entities.UserPool;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.HttpUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SFUserListActivity extends Activity {

    private User currentUser;
    private String type;
    private int curBiggestUserID = -1;
    private List<User> users;
    private UserAdapter mAdapter;
    private XRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_f_user_list);
        currentUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        type = getIntent().getStringExtra("type");
        users = new ArrayList<>();
        TextView textView = findViewById(R.id.albumHint);
        if (type.equals("subscribe")) {
            textView.setText(currentUser.getProfileName()+" 的关注");
        }
        else {
            textView.setText(currentUser.getProfileName()+" 的粉丝");
        }
        initUserSFAsync();
    }

    private void setRecyclerView(final List<User> users) {
        mRecyclerView = findViewById(R.id.SFUserRecyclerView);
        mAdapter = new UserAdapter(this, users);
        mAdapter.setOnItemClickListener(new UserAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到个人中心页面
                Intent intent = new Intent();
                User currentUser = mAdapter.getItem(position);
                intent.setClass(SFUserListActivity.this, UserInfoActivity.class);
                intent.putExtra("user", Parcels.wrap(User.class, currentUser));
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setPullRefreshEnabled(false);
        ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                Log.e("正在加载更多"," ");
                RequestBody requestBody = new FormBody.Builder()
                        .add("biggestUserID", String.valueOf(curBiggestUserID))
                        .add("userID", currentUser.getId())
                        .add("type", type)
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadUserSFServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求失败", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.e("搜索返回的数据", responseData+"_");
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        final List<User> tempUsers = new ArrayList<>();
                        if (responseNum == Constant.LOADMORE_USER_SF_SUCCESS) {
                            JSONArray userIdArray = jsonObject.getJSONArray("UserIds");
                            if (userIdArray.size() > 0) {
                                curBiggestUserID = userIdArray.getInteger(userIdArray.size() - 1);
                                for (int j = 0; j < userIdArray.size() - 1; j++) {
                                    int userId = userIdArray.getInteger(j);
                                    tempUsers.add(UserPool.addUser(userId, SFUserListActivity.this));
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (tempUsers.size() != 0) {
                                            users.addAll(tempUsers);
                                        }
                                        mRecyclerView.loadMoreComplete();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void initUserSFAsync() {
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(currentUser.getID()))
                .add("type", type)
                .build();
        Log.e("传上去的类型", type + "_");
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "InitUserSFServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求出现错误", e.getMessage() + " ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("搜索返回的数据", responseData+"_");
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                final List<User> users = new ArrayList<>();
                if (responseNum == Constant.INIT_USER_SF_SUCCESS) {
                    JSONArray userIdArray = jsonObject.getJSONArray("UserIds");
                    if (userIdArray.size() > 0) {
                        for (int j = 0; j < userIdArray.size() - 1; j++) {
                            int userId = userIdArray.getInteger(j);
                            users.add(UserPool.addUser(userId, SFUserListActivity.this));
                        }
                        curBiggestUserID = userIdArray.getInteger(userIdArray.size() - 1);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRecyclerView(users);
                        }
                    });
                }
            }
        });
    }
}
