package com.e.insphoto.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
import com.e.insphoto.utils.HttpUtil;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;

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

public class SearchActivity extends Activity {

    private XRecyclerView mRecyclerView;
    private PictureAdapter mAdapter_Pic;
    private UserAdapter mAdapter_User;
    private int curSmallestImgID = -1;
    private int curBiggestUserID = -1;

    static final int SEARCH_SUCCESS = 331;
    static final int SEARCH_FAILURE = 332;
    static final int LOADMORE_SEARCH_SUCCESS = 333;
    static final int LOADMORE_SEARCH_FAILIRE = 334;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ToggleSwitch toggleSwitch = findViewById(R.id.searchType);
        final SearchView searchView = findViewById(R.id.searchView);

        toggleSwitch.setCheckedPosition(0);
        searchView.setQueryHint("请输入照片关键词");
        toggleSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
                if (i == 0) {
                    searchView.setQueryHint("请输入照片关键词");
                }
                else {
                    searchView.setQueryHint("请输入用户昵称");
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                int type = toggleSwitch.getCheckedPosition();
                if (type == 0) {
                    search(s, true);
                }
                else {
                    search(s, false);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        final TextView searchText = findViewById(R.id.searchText);
        searchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = searchView.getQuery().toString();
                search(text, true);
            }
        });
    }

    public void search(String text, boolean isSearchPic) {
        if (isSearchPic) {
            initResultPicAsync(text);
        }
        else {
            initResultUserAsync(text);
        }
    }

    private void initResultPicAsync(final String text) {
        RequestBody requestBody = new FormBody.Builder()
                .add("text", text)
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "SearchPhotoServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("搜索返回的数据", responseData+"_");
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                final List<Picture> pictures = new ArrayList<>();
                if (responseNum == SEARCH_SUCCESS) {
                    Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>(){});
                    Map<Integer, String> map = new TreeMap<>(originalMap);
                    int j = 0;
                    ListIterator<Map.Entry<Integer, String>> li = new ArrayList<>(map.entrySet()).listIterator(map.size());
                    while (li.hasPrevious()) {
                        Map.Entry<Integer, String> entry = li.previous();
                        if (j == map.size() - 1) {
                            curSmallestImgID = entry.getKey();
                            Log.e("最小ID", curSmallestImgID+"_");
                        }
                        PicturePool.addPicture(entry.getKey(), entry.getValue(), SearchActivity.this);
                        pictures.add(PicturePool.getPicture(entry.getKey()));
                        j++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setPicRecyclerView(text, pictures);
                        }
                    });
                }
            }
        });
    }

    private void initResultUserAsync(final String text) {
        RequestBody requestBody = new FormBody.Builder()
                .add("text", text)
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "SearchUserServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("搜索返回的数据", responseData+"_");
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                final List<User> users = new ArrayList<>();
                if (responseNum == SEARCH_SUCCESS) {
                    JSONArray userIdArray = jsonObject.getJSONArray("UserIds");
                    for (int j = 0; j < userIdArray.size(); j++) {
                        int userId = userIdArray.getInteger(j);
                        users.add(UserPool.addUser(userId, SearchActivity.this));
                    }
                    curBiggestUserID = userIdArray.getInteger(userIdArray.size()-1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setUserRecyclerView(text, users);
                        }
                    });
                }
            }
        });
    }

    private void setPicRecyclerView(final String text, final List<Picture> pictures) {
        mRecyclerView = findViewById(R.id.searchResultRecyclerView);
        mAdapter_Pic = new PictureAdapter(this, pictures);
        mAdapter_Pic.setOnItemClickListener(new PictureAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到图片详情页面
                Intent intent = new Intent();
                Picture currentPicture = mAdapter_Pic.getItem(position);
                intent.setClass(SearchActivity.this, PicInfoActivity.class);
                intent.putExtra("pic", Parcels.wrap(Picture.class, currentPicture));
                startActivity(intent);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter_Pic);
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
                        .add("smallestImgID", String.valueOf(curSmallestImgID))
                        .add("text", text)
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadPhotoResultServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求失败", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.e("返回的数据", responseData+"_");
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == LOADMORE_SEARCH_SUCCESS) {
                            Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>() {});
                            Map<Integer, String> map = new TreeMap<>(originalMap);
                            int j = 0;
                            ListIterator<Map.Entry<Integer, String>> li = new ArrayList<>(map.entrySet()).listIterator(map.size());
                            final List<Picture> tempPics = new ArrayList<>();
                            // 先更新最小ID，免得被阻塞
                            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                                if (j == 0) {
                                    curSmallestImgID = entry.getKey();
                                    Log.e("当前最小ID", curSmallestImgID+"_");
                                }
                                j++;
                            }
                            while (li.hasPrevious()) {
                                Map.Entry<Integer, String> entry = li.previous();
                                PicturePool.addPicture(entry.getKey(), entry.getValue(), SearchActivity.this);
                                tempPics.add(PicturePool.getPicture(entry.getKey()));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tempPics.size() != 0) {
                                        pictures.addAll(tempPics);
                                    }
                                    mRecyclerView.loadMoreComplete();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void setUserRecyclerView(final String text, final List<User> users) {
        mRecyclerView = findViewById(R.id.searchResultRecyclerView);
        mAdapter_User = new UserAdapter(this, users);
        mAdapter_User.setOnItemClickListener(new UserAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到个人中心页面
                Intent intent = new Intent();
                User currentUser = mAdapter_User.getItem(position);
                intent.setClass(SearchActivity.this, UserInfoActivity.class);
                intent.putExtra("user", Parcels.wrap(User.class, currentUser));
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter_User);
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
                        .add("text", text)
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadUserResultServlet", requestBody, new Callback() {
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
                        if (responseNum == LOADMORE_SEARCH_SUCCESS) {
                            JSONArray userIdArray = jsonObject.getJSONArray("UserIds");
                            curBiggestUserID = userIdArray.getInteger(userIdArray.size()-1);
                            for (int j = 0; j < userIdArray.size(); j++) {
                                int userId = userIdArray.getInteger(j);
                                tempUsers.add(UserPool.addUser(userId, SearchActivity.this));
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tempUsers.size()!=0) {
                                        users.addAll(tempUsers);
                                    }
                                    mRecyclerView.loadMoreComplete();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
