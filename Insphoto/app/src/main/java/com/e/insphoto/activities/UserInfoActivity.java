package com.e.insphoto.activities;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;
import com.e.insphoto.R;
import com.e.insphoto.entities.Picture;
import com.e.insphoto.entities.PicturePool;
import com.e.insphoto.entities.User;
import com.e.insphoto.utils.Constant;
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

public class UserInfoActivity extends Activity {
    private User currentUser;
    private XRecyclerView mRecyclerView;
    private PictureAdapter mAdapter;
    private int curSmallestImgID = -1;
    private List<Picture> pictures;
    private boolean hasSubscribe = false;

    static final int INIT_USER_PIC_SUCCESS = 319;
    static final int INIT_USER_PIC_FAILURE = 320;
    static final int LOADMORE_USER_PIC_SUCCESS = 321;
    static final int LOADMORE_USER_PIC_FAILURE = 322;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        currentUser = Parcels.unwrap(getIntent().getParcelableExtra("user"));

        mRecyclerView = findViewById(R.id.userPicRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        final View header = LayoutInflater.from(this).inflate(R.layout.userinfoheader, mRecyclerView, false);

        ImageView imageView = header.findViewById(R.id.userprofile);
        Glide.with(this).load(currentUser.getProfileImgPath()).into(imageView);
        TextView textView = header.findViewById(R.id.username);
        textView.setText(currentUser.getProfileName());
        TextView textView_2 = header.findViewById(R.id.userDescription);
        textView_2.setText("??????????????? "+currentUser.getDescription());
        pictures = new ArrayList<>();
        initUserPicAsync(header);

        TextView sendMessage = header.findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(UserInfoActivity.this, ChatActivity.class);
                intent.putExtra("receiver", Parcels.wrap(User.class, currentUser));
                startActivity(intent);
            }
        });

        TextView subscribe = header.findViewById(R.id.subscribe);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO

            }
        });
    }

    private void initUserPicAsync(final View header) {
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(currentUser.getId()))
                .add("mine", String.valueOf(Constant.currentUser.getID()))
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "InitUserPhotosServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("??????????????????",e.getMessage()+" ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == INIT_USER_PIC_SUCCESS) {
                    Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>(){});
                    Map<Integer, String> map = new TreeMap<>(originalMap);
                    final Map<String, Integer> LSNumMap = JSONObject.parseObject(jsonObject.getJSONObject("LSNums").toJSONString(), new TypeReference<Map<String, Integer>>(){});
                    int j = 0;
                    ListIterator<Map.Entry<Integer, String>> li = new ArrayList<>(map.entrySet()).listIterator(map.size());
                    while (li.hasPrevious()) {
                        Map.Entry<Integer, String> entry = li.previous();
                        if (j == map.size() - 1) {
                            curSmallestImgID = entry.getKey();
                            Log.e("??????ID", curSmallestImgID+"_");
                        }
                        PicturePool.addPicture(entry.getKey(), entry.getValue(), UserInfoActivity.this);
                        pictures.add(PicturePool.getPicture(entry.getKey()));
                        j++;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView likeNum = header.findViewById(R.id.userLikeNum);
                            try {
                                int userLikeNum = LSNumMap.get("likenum");
                                likeNum.setText("??????\n" +userLikeNum);
                            }
                            catch (NullPointerException e) {
                                likeNum.setText("??????\n0");
                            }
                            likeNum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onClick_turnToLikeButton();
                                }
                            });

                            TextView starNum = header.findViewById(R.id.userStarNum);
                            try {
                                int userStarNum = LSNumMap.get("starnum");
                                starNum.setText("??????\n" + userStarNum);
                            }
                            catch (NullPointerException e) {
                                starNum.setText("??????\n0");
                            }
                            starNum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onClick_turnToStarButton();
                                }
                            });

                            TextView subscribeNum = header.findViewById(R.id.userSubscribeNum);
                            try {
                                int userSubscribeNum = LSNumMap.get("subscribenum");
                                subscribeNum.setText("??????\n" +userSubscribeNum);
                            }
                            catch (NullPointerException e) {
                                subscribeNum.setText("??????\n0");
                            }
                            subscribeNum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onClick_turnToSubscribeButton();
                                }
                            });

                            TextView followerNum = header.findViewById(R.id.userFollowerNum);
                            try {
                                int userFollowerNum = LSNumMap.get("followernum");
                                followerNum.setText("??????\n" + userFollowerNum);
                            }
                            catch (NullPointerException e) {
                                followerNum.setText("??????\n0");
                            }
                            followerNum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onClick_turnToFollowerButton();
                                }
                            });

                            TextView subscribe = header.findViewById(R.id.subscribe);
                            ImageView subscribeIcon = header.findViewById(R.id.subscribeicon);
                            int subscribeStatus = LSNumMap.get("hassubscribe");
                            if (subscribeStatus == 1) {
                                subscribe.setText("????????????");
                                Glide.with(UserInfoActivity.this).load(R.drawable.cancelsubscribe).into(subscribeIcon);
                                hasSubscribe = true;
                            }
                            else {
                                subscribe.setText("?????????");
                            }
                            subscribe.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    setSubscribe(hasSubscribe, header);
                                }
                            });

                            setRecyclerView(header);
                        }
                    });
                }
            }
        });
    }

    private void setSubscribe(boolean hasSubscribed, final View header) {
        // ?????????
        RequestBody requestBody = new FormBody.Builder()
                .add("fromId", Constant.currentUser.getId())
                .add("toId", currentUser.getId())
                .add("subscribe", String.valueOf(!hasSubscribed))
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "SubscribeServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == Constant.SUBSCRIBE_OPERATION_SUCCESS) {

                }
            }
        });
        TextView subscribe = header.findViewById(R.id.subscribe);
        TextView followerNum = header.findViewById(R.id.userFollowerNum);
        ImageView subscribeIcon = header.findViewById(R.id.subscribeicon);
        int originFollowerNum = Integer.parseInt(followerNum.getText().toString().substring(3));
        Log.e("??????", followerNum.getText().toString().substring(3));
        // ?????????
        if (hasSubscribed) {
            subscribe.setText("?????????");
            Glide.with(this).load(R.drawable.subscribe).into(subscribeIcon);
            hasSubscribe = false;
            followerNum.setText("??????\n" + (originFollowerNum - 1));
        }
        else {
            subscribe.setText("????????????");
            Glide.with(this).load(R.drawable.cancelsubscribe).into(subscribeIcon);
            hasSubscribe = true;
            followerNum.setText("??????\n" + (originFollowerNum + 1));
        }
    }

    private void setRecyclerView(final View header) {
        mAdapter = new PictureAdapter(this, pictures);
        mAdapter.setOnItemClickListener(new PictureAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // ???????????????????????????
                Intent intent = new Intent();
                Picture currentPicture = mAdapter.getItem(position - 1);
                intent.setClass(UserInfoActivity.this, PicInfoActivity.class);
                intent.putExtra("pic", Parcels.wrap(Picture.class, currentPicture));
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addHeaderView(header);
        mRecyclerView.setPullRefreshEnabled(false);
        ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                Log.e("??????????????????"," ");
                RequestBody requestBody = new FormBody.Builder()
                        .add("userId", String.valueOf(currentUser.getId()))
                        .add("smallestImgID", String.valueOf(curSmallestImgID))
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadUserPhotoServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("????????????", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.e("???????????????", responseData+"_");
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == LOADMORE_USER_PIC_SUCCESS) {
                            Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>() {});
                            Map<Integer, String> map = new TreeMap<>(originalMap);
                            int j = 0;
                            ListIterator<Map.Entry<Integer, String>> li = new ArrayList<Map.Entry<Integer, String>>(map.entrySet()).listIterator(map.size());
                            final List<Picture> tempPics = new ArrayList<>();
                            // ???????????????ID??????????????????
                            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                                if (j == 0) {
                                    curSmallestImgID = entry.getKey();
                                    Log.e("????????????ID", curSmallestImgID+"_");
                                }
                                j++;
                            }
                            while (li.hasPrevious()) {
                                Map.Entry<Integer, String> entry = li.previous();
                                PicturePool.addPicture(entry.getKey(), entry.getValue(), UserInfoActivity.this);
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

    public void onClick_turnToStarButton() {
        Intent intent = new Intent();
        intent.setClass(UserInfoActivity.this, LSAlbumActivity.class);
        intent.putExtra("user", Parcels.wrap(User.class, currentUser));
        intent.putExtra("type", "star");
        startActivity(intent);
    }

    public void onClick_turnToLikeButton() {
        Intent intent = new Intent();
        intent.setClass(UserInfoActivity.this, LSAlbumActivity.class);
        intent.putExtra("user", Parcels.wrap(User.class, currentUser));
        intent.putExtra("type", "like");
        startActivity(intent);
    }

    public void onClick_turnToSubscribeButton() {
        Intent intent = new Intent();
        intent.setClass(UserInfoActivity.this, SFUserListActivity.class);
        intent.putExtra("user", Parcels.wrap(User.class, currentUser));
        intent.putExtra("type", "subscribe");
        startActivity(intent);
    }

    public void onClick_turnToFollowerButton() {
        Intent intent = new Intent();
        intent.setClass(UserInfoActivity.this, SFUserListActivity.class);
        intent.putExtra("user", Parcels.wrap(User.class, currentUser));
        intent.putExtra("type", "follower");
        startActivity(intent);
    }
}
