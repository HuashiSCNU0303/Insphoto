package com.e.insphoto.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.e.insphoto.activities.*;
import com.e.insphoto.R;
import com.e.insphoto.entities.Picture;
import com.e.insphoto.entities.PicturePool;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.adapters.PictureAdapter;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import org.parceler.Parcels;

import java.io.File;
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

import static android.app.Activity.RESULT_OK;

public class IndexPageFragment extends Fragment {
    private static final int POST_CAMERA_IMAGE = 11;
    private static final int POST_ALBUM_IMAGE = 12;
    private static final int POST_IMAGE = 10;
    private static final int INIT_PHOTO_SUCCESS = 301;
    static final int INIT_PHOTO_FAILURE = 302;
    static final int REFRESH_PHOTO_SUCCESS = 303;
    static final int REFRESH_PHOTO_FAILURE = 304;
    static final int LOADMORE_PHOTO_SUCCESS = 305;
    static final int LOADMORE_PHOTO_FAILURE = 306;

    private int curBiggestImgID, curSmallestImgID;

    private String[] photoByCamera;
    private XRecyclerView mRecyclerView;
    private PictureAdapter mAdapter;
    private List<Picture> pictures;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.framelayout_indexpage, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoByCamera = new String[] {""};
        pictures = new ArrayList<>();
        getActivity().findViewById(R.id.selectPhotoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_selectPhotoButton();
            }
        });
        getActivity().findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_searchButton();
            }
        });
        initPhotoAsync();
    }

    private void setRecyclerView() {
        mRecyclerView = getActivity().findViewById(R.id.picRecyclerView);
        mAdapter = new PictureAdapter(getActivity(), pictures);
        mAdapter.setOnItemClickListener(new PictureAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 跳转到图片详情页面
                Intent intent = new Intent();
                Picture currentPicture = mAdapter.getItem(position);
                intent.setClass(getContext(), PicInfoActivity.class);
                intent.putExtra("pic", Parcels.wrap(Picture.class, currentPicture));
                startActivity(intent);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                Log.e("正在刷新"," ");
                RequestBody requestBody = new FormBody.Builder()
                        .add("biggestImgID", String.valueOf(curBiggestImgID))
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "RefreshPhotoServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.e("返回的数据", responseData+"_");
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == REFRESH_PHOTO_SUCCESS) {
                            Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>() {});
                            Map<Integer, String> map = new TreeMap<>(originalMap);
                            int j = 0;
                            ListIterator<Map.Entry<Integer, String>> li = new ArrayList<>(map.entrySet()).listIterator(map.size());
                            final List<Picture> tempPics = new ArrayList<>();
                            // 先更新最大ID，免得被阻塞
                            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                                if (j == map.size()-1) {
                                    curBiggestImgID = entry.getKey();
                                    Log.e("当前最大ID", curBiggestImgID+"_");
                                }
                                j++;
                            }
                            while (li.hasPrevious()) {
                                Map.Entry<Integer, String> entry = li.previous();
                                PicturePool.addPicture(entry.getKey(), entry.getValue(), getContext());
                                tempPics.add(PicturePool.getPicture(entry.getKey()));
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (tempPics.size() != 0) {
                                        pictures.addAll(0, tempPics);
                                        mAdapter.notifyItemRangeInserted(0, tempPics.size());
                                    }
                                    mRecyclerView.refreshComplete();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onLoadMore() {
                Log.e("正在加载更多"," ");
                RequestBody requestBody = new FormBody.Builder()
                        .add("smallestImgID", String.valueOf(curSmallestImgID))
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadPhotoServlet", requestBody, new Callback() {
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
                        if (responseNum == LOADMORE_PHOTO_SUCCESS) {
                            Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>() {});
                            Map<Integer, String> map = new TreeMap<>(originalMap);
                            int j = 0;
                            Log.e("开始下载","_");
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
                                PicturePool.addPicture(entry.getKey(), entry.getValue(), getContext());
                                tempPics.add(PicturePool.getPicture(entry.getKey()));
                            }
                            Log.e("下载完成","_");
                            getActivity().runOnUiThread(new Runnable() {
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

    private void initPhotoAsync() {
        HttpUtil.sendGetRequest(HttpUtil.BASEURL + "InitPhotosServlet", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求出现错误",e.getMessage()+" ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == INIT_PHOTO_SUCCESS) {
                    Map<Integer, String> originalMap = JSONObject.parseObject(jsonObject.getJSONObject("Photos").toJSONString(), new TypeReference<Map<Integer, String>>(){});
                    Map<Integer, String> map = new TreeMap<>(originalMap);
                    int j = 0;
                    ListIterator<Map.Entry<Integer, String>> li = new ArrayList<>(map.entrySet()).listIterator(map.size());
                    while (li.hasPrevious()) {
                        Map.Entry<Integer, String> entry = li.previous();
                        if (j == 0) {
                            curBiggestImgID = entry.getKey();
                            Log.e("最大ID", curBiggestImgID+"_");
                        }
                        else if (j == map.size() - 1) {
                            curSmallestImgID = entry.getKey();
                            Log.e("最小ID", curSmallestImgID+"_");
                        }
                        PicturePool.addPicture(entry.getKey(), entry.getValue(), getContext());
                        pictures.add(PicturePool.getPicture(entry.getKey()));
                        j++;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRecyclerView();
                        }
                    });
                }
            }
        });
    }

    private void onClick_selectPhotoButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(new String[]{"相机拍照", "本地上传"}, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // 用相机拍照
                    photoByCamera[0] = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + System.currentTimeMillis() + ".jpg";
                    File cameraSavePath = new File(photoByCamera[0]);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = FileProvider.getUriForFile(getContext(), "com.e.insphoto.fileprovider", cameraSavePath);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        uri = Uri.fromFile(cameraSavePath);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, POST_CAMERA_IMAGE);
                }
                else {
                    Toast.makeText(getContext(), "请选择要上传的照片", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, POST_ALBUM_IMAGE);
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(true);
    }

    private void onClick_searchButton() {
        Intent intent=new Intent();
        intent.setClass(getContext(), SearchActivity.class);
        startActivity(intent);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == POST_ALBUM_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                Intent intent=new Intent();
                intent.setClass(getContext(), ModifyActivity.class);
                intent.putExtra("url", imagePath);
                startActivityForResult(intent, POST_IMAGE);
                c.close();
            }
        }
        else if (requestCode == POST_CAMERA_IMAGE) {
            if (resultCode == RESULT_OK) {
                Intent intent=new Intent();
                intent.setClass(getContext(), ModifyActivity.class);
                intent.putExtra("url", photoByCamera[0]);
                startActivityForResult(intent, POST_IMAGE);
            }
        }
        else if (requestCode == POST_IMAGE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getContext(), "图片上传成功！", Toast.LENGTH_LONG).show();
            }
        }
    }
}
