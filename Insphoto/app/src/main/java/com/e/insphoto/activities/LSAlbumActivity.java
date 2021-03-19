package com.e.insphoto.activities;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.e.insphoto.R;
import com.e.insphoto.entities.Picture;
import com.e.insphoto.entities.PicturePool;
import com.e.insphoto.entities.User;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.adapters.PictureAdapter;
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

public class LSAlbumActivity extends Activity {

    private User currentUser;
    private String type;
    private int curSmallestImgID = -1;
    private List<Picture> pictures;
    private PictureAdapter mAdapter;
    private XRecyclerView mRecyclerView;

    static final int INIT_USER_LS_PIC_SUCCESS = 323;
    static final int INIT_USER_LS_PIC_FAILURE = 324;
    static final int LOADMORE_USER_LS_PIC_SUCCESS = 325;
    static final int LOADMORE_USER_LS_PIC_FAILURE = 326;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_l_s_album);
        currentUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        type = getIntent().getStringExtra("type");
        pictures = new ArrayList<>();
        TextView textView = findViewById(R.id.albumHint);
        if (type.equals("like")) {
            textView.setText(currentUser.getProfileName()+" 的点赞");
        }
        else {
            textView.setText(currentUser.getProfileName()+" 的收藏");
        }
        initLSPicAsync();
    }

    private void setRecyclerView() {
        mRecyclerView = findViewById(R.id.LSPicRecyclerView);
        mAdapter = new PictureAdapter(this, pictures);
        mAdapter.setOnItemClickListener(new PictureAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到图片详情页面
                Intent intent = new Intent();
                Picture currentPicture = mAdapter.getItem(position);
                intent.setClass(LSAlbumActivity.this, PicInfoActivity.class);
                intent.putExtra("pic", Parcels.wrap(Picture.class, currentPicture));
                startActivity(intent);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
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
                        .add("userId", String.valueOf(currentUser.getId()))
                        .add("smallestImgID", String.valueOf(curSmallestImgID))
                        .add("type",type)
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadUserLSPhotoServlet", requestBody, new Callback() {
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
                        if (responseNum == LOADMORE_USER_LS_PIC_SUCCESS) {
                            Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>() {});
                            Map<Integer, String> map = new TreeMap<>(originalMap);
                            int j = 0;
                            ListIterator<Map.Entry<Integer, String>> li = new ArrayList<Map.Entry<Integer, String>>(map.entrySet()).listIterator(map.size());
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
                                PicturePool.addPicture(entry.getKey(), entry.getValue(), LSAlbumActivity.this);
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

    private void initLSPicAsync() {
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(currentUser.getID()))
                .add("type", type)
                .build();
        Log.e("传上去的类型", type+"_");
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "InitUserLSPhotosServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求出现错误",e.getMessage()+" ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("收到的信息", responseData);
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == INIT_USER_LS_PIC_SUCCESS) {
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
                        PicturePool.addPicture(entry.getKey(), entry.getValue(), LSAlbumActivity.this);
                        pictures.add(PicturePool.getPicture(entry.getKey()));
                        j++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRecyclerView();
                        }
                    });
                }
            }
        });
    }
}
