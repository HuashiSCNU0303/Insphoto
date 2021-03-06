package com.e.insphoto.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.e.insphoto.activities.AboutActivity;
import com.e.insphoto.activities.LoginActivity;
import com.e.insphoto.activities.UserInfoActivity;
import com.e.insphoto.adapters.MenuItemAdapter;
import com.e.insphoto.entities.RecentMessagePool;
import com.e.insphoto.entities.User;
import com.e.insphoto.entities.UserPool;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.R;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jiajie.load.LoadingDialog;
import com.jkt.tcompress.OnCompressListener;
import com.jkt.tcompress.TCompress;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class MinePageFragment extends Fragment {

    private static final int CHANGE_NAME_SUCCESS = 201;
    private static final int CHANGE_IMG_SUCCESS = 203;
    private static final int CHANGE_IMG = 205;

    private MenuItemAdapter mAdapter;
    private XRecyclerView mRecyclerView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.framelayout_minepage, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUserInfoAsync();
        initMenu();
        getActivity().findViewById(R.id.myProfileName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfileName();
            }
        });
        getActivity().findViewById(R.id.myProfileImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "???????????????????????????", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHANGE_IMG);
            }
        });
        getActivity().findViewById(R.id.myProfileDescription).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfileDescription();
            }
        });
    }

    private void initMenu() {
        mRecyclerView = getActivity().findViewById(R.id.menuRecyclerView);
        List<Pair<String, Integer>> menuItems = new ArrayList<>();
        menuItems.add(new Pair<>("??????????????????", R.drawable.mineinfo));
        menuItems.add(new Pair<>("????????????", R.drawable.logout));
        menuItems.add(new Pair<>("??????", R.drawable.about));
        // TODO: ???????????????????????????
        mAdapter = new MenuItemAdapter(getContext(), menuItems);
        mAdapter.setOnItemClickListener(new MenuItemAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch(position) {
                    case 0: {
                        // ?????????????????????????????????
                        Intent intent = new Intent();
                        intent.setClass(getContext(), UserInfoActivity.class);
                        intent.putExtra("user", Parcels.wrap(User.class, Constant.currentUser));
                        startActivity(intent);
                        break;
                    }
                    case 1: {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        RecentMessagePool.clear();
                        break;
                    }
                    case 2: {
                        Intent intent = new Intent();
                        intent.setClass(getContext(), AboutActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.setLoadingMoreEnabled(false);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void initUserInfoAsync() {
        TextView textView = getActivity().findViewById(R.id.myProfileName);
        textView.setText(Constant.currentUser.getProfileName());

        TextView description = getActivity().findViewById(R.id.myProfileDescription);
        description.setText("??????????????? "+Constant.currentUser.getDescription());

        ImageView imageView = getActivity().findViewById(R.id.myProfileImg);
        Glide.with(this.getContext()).load(Constant.currentUser.getProfileImgPath())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void changeProfileName() {
        View view = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText1 = view.findViewById(R.id.editUserInfo);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("??????????????????")//????????????????????????
                .setView(view)
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = editText1.getText().toString();
                        changeProfileNameAsync(Constant.currentUser.getAccount(), newName);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void changeProfileNameAsync(String account, final String newName) {
        final LoadingDialog loadingDialog = new LoadingDialog.Builder(getContext()).loadText("?????????...").build();
        loadingDialog.show();
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(Constant.currentUser.getID()))
                .add("newName", newName)
                .build();
        String url = HttpUtil.BASEURL+"ChangeNameServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("????????????", e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Looper.prepare();
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int resultNum = jsonObject.getInteger("Result");
                loadingDialog.dismiss();
                if (resultNum == CHANGE_NAME_SUCCESS) {
                    Toast.makeText(getContext(), "?????????????????????", Toast.LENGTH_LONG).show();
                    TextView textView = getActivity().findViewById(R.id.myProfileName);
                    UserPool.getUser(Constant.currentUser.getID()).setProfileName(newName);
                    textView.setText(newName);
                }
                else {
                    Toast.makeText(getContext(), "?????????????????????", Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        });
    }


    public void changeProfileDescription() {
        View view = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText1 = view.findViewById(R.id.editUserInfo);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("????????????????????????")//????????????????????????
                .setView(view)
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newDescription = editText1.getText().toString();
                        changeProfileDescriptionAsync(newDescription);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void changeProfileDescriptionAsync(final String newDescription) {
        final LoadingDialog loadingDialog = new LoadingDialog.Builder(getContext()).loadText("?????????...").build();
        loadingDialog.show();
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(Constant.currentUser.getID()))
                .add("newDescription", newDescription)
                .build();
        String url = HttpUtil.BASEURL+"ChangeDescriptionServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("????????????", e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Looper.prepare();
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int resultNum = jsonObject.getInteger("Result");
                loadingDialog.dismiss();
                if (resultNum == Constant.CHANGE_DESCRIPTION_SUCCESS) {
                    Toast.makeText(getContext(), "???????????????????????????", Toast.LENGTH_LONG).show();
                    TextView textView = getActivity().findViewById(R.id.myProfileDescription);
                    UserPool.getUser(Constant.currentUser.getID()).setDescription(newDescription);
                    textView.setText("???????????????" + newDescription);
                }
                else {
                    Toast.makeText(getContext(), "???????????????????????????", Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_IMG) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumns = {MediaStore.Images.Media.DATA};
                Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                // ?????????????????????????????????
                TCompress tCompress = new TCompress.Builder()
                        .setMaxWidth(810)
                        .setMaxHeight(540)
                        .setQuality(80)
                        .setFormat(Bitmap.CompressFormat.JPEG)
                        .setConfig(Bitmap.Config.RGB_565)
                        .build();
                tCompress.compressToFileAsync(new File(imagePath), new OnCompressListener<File>() {
                    @Override
                    public void onCompressFinish(boolean success, final File file) {
                        if (success) {
                            final LoadingDialog dialog = new LoadingDialog.Builder(getContext()).loadText("???????????????...").build();
                            dialog.show();
                            boolean isProfileDefault = false;
                            if (Constant.currentUser.getProfileImgUrl().equals("defaultprofile.jpg")) {
                                isProfileDefault = true;
                            }
                            HttpUtil.sendProfileRequest(Constant.currentUser.getID(), file.getAbsolutePath(), isProfileDefault, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e("??????????????????", e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    Looper.prepare();
                                    String responseData = response.body().string();
                                    JSONObject jsonObject = JSON.parseObject(responseData);
                                    int responseNum = jsonObject.getInteger("Result");
                                    Log.e("???????????????",responseData+"_");
                                    if (responseNum == CHANGE_IMG_SUCCESS) {
                                        Toast.makeText(getContext(), "?????????????????????", Toast.LENGTH_LONG).show();
                                        final ImageView imageView = getActivity().findViewById(R.id.myProfileImg);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Glide.with(getContext()).load(file.getAbsolutePath()).into(imageView);
                                            }
                                        });
                                        User currentUser = UserPool.getUser(Constant.currentUser.getID());
                                        currentUser.setProfileImgPath(file.getAbsolutePath());
                                        currentUser.setProfileImgUrl(currentUser.getId()+"_profile.jpg");
                                    }
                                    else {
                                    }
                                    dialog.dismiss();
                                    Looper.loop();
                                }
                            });
                        }
                        else {
                            Log.e("??????????????????", " ");
                        }
                    }
                });
                c.close();
            }
        }
    }
}
