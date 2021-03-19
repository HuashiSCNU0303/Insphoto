package com.e.insphoto.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coorchice.library.SuperTextView;
import com.e.insphoto.R;
import com.e.insphoto.entities.*;
import com.e.insphoto.adapters.CommentAdapter;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.HttpUtil;
import com.e.insphoto.utils.Util;
import com.github.chrisbanes.photoview.PhotoView;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.jiajie.load.LoadingDialog;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class PicInfoActivity extends Activity {

    private Picture currentPhoto;
    private XRecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private boolean[] hasRefreshed;
    private

    static final int GET_PIC_INFO_SUCCESS = 307;
    static final int GET_PIC_INFO_FAILURE = 308;
    static final int LSC_OPERATION_SUCCESS = 309;
    static final int LSC_OPERATION_FAILURE = 310;
    static final int REFRESH_LSC_SUCCESS = 313;
    static final int REFRESH_LSC_FAILURE = 314;
    static final int LOAD_COMMENT_SUCCESS = 315;
    static final int LOAD_COMMENT_FAILURE = 316;
    static final int DEL_PIC_SUCCESS = 317;
    static final int DEL_PIC_FAILURE = 318;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_info);

        mRecyclerView = findViewById(R.id.commentRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        final View header = LayoutInflater.from(this).inflate(R.layout.picinfoheader, mRecyclerView, false);
        ImageButton refreshCommentButton = header.findViewById(R.id.refreshCommentButton);
        refreshCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_refreshCommentButton(header);
            }
        });
        ImageButton delPhotoButton = findViewById(R.id.delPhotoButton);
        delPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_delPhotoButton();
            }
        });
        ImageButton editDescriptionButton = findViewById(R.id.editDescriptionButton);
        editDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick_editDescriptionButton(header);
            }
        });
        hasRefreshed = new boolean[]{false};
        currentPhoto = Parcels.unwrap(getIntent().getParcelableExtra("pic"));
        PhotoView photoView = (PhotoView) header.findViewById(R.id.photo_view);
        photoView.setImageURI(Uri.fromFile(new File(currentPhoto.getImagePath())));
        initPicInfoAsync(header);
    }

    private void initPicInfoAsync(final View header) {
        RequestBody requestBody = new FormBody.Builder()
                .add("userId", String.valueOf(Constant.currentUser.getId()))
                .add("id", String.valueOf(currentPhoto.getImageID()))
                .build();
        String url = HttpUtil.BASEURL+"GetPicInfoServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("返回失败", "_");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == GET_PIC_INFO_SUCCESS) {
                    JSONObject picInfo = jsonObject.getJSONObject("info");
                    int posterID = picInfo.getInteger("posterID");
                    User Poster = UserPool.addUser(posterID, PicInfoActivity.this);
                    currentPhoto.setPoster(Poster);
                    currentPhoto.setDescription(picInfo.getString("description"));
                    currentPhoto.setLikeNum(picInfo.getInteger("likeNum"));
                    currentPhoto.setStarNum(picInfo.getInteger("starNum"));
                    currentPhoto.setCommentNum(picInfo.getInteger("commentNum"));
                    JSONArray commentArray = picInfo.getJSONArray("comments");
                    List<Comment> comments = new ArrayList<>();
                    for (int j = 0; j < commentArray.size(); j++) {
                        JSONObject commentInfo = commentArray.getJSONObject(j);
                        Comment comment = new Comment();
                        int userID = commentInfo.getInteger("userID");
                        comment.setTime(Util.convertTimeToDateString(Long.parseLong(commentInfo.getString("time"))));
                        comment.setComment(commentInfo.getString("comment"));
                        User user = UserPool.addUser(userID, PicInfoActivity.this);
                        comment.setUser(user);
                        comment.setId(commentInfo.getInteger("id"));
                        comments.add(comment);
                    }
                    currentPhoto.setComments(comments);
                    // 获取全部信息以后，再一次性更新UI
                    final Picture pic = currentPhoto;
                    final boolean isLike = picInfo.getBoolean("like");
                    final boolean isStar = picInfo.getBoolean("star");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isLike) {
                                ShineButton likeButton = header.findViewById(R.id.likebutton);
                                likeButton.setChecked(true);
                            }
                            if (isStar) {
                                ShineButton starButton = header.findViewById(R.id.starbutton);
                                starButton.setChecked(true);
                            }
                            TextView likeNum = header.findViewById(R.id.likenum);
                            likeNum.setText(String.valueOf(pic.getLikeNum()));

                            TextView starNum = header.findViewById(R.id.starnum);
                            starNum.setText(String.valueOf(pic.getStarNum()));

                            TextView commentNum = header.findViewById(R.id.commentnum);
                            commentNum.setText(String.valueOf(pic.getCommentNum()));

                            TextView posterName = header.findViewById(R.id.postername);
                            posterName.setText(pic.getPoster().getProfileName());

                            TextView postTime = header.findViewById(R.id.posttime);
                            postTime.setText("发表于 "+pic.getPostTime());

                            TextView commentNumText = header.findViewById(R.id.commentnumtext);
                            commentNumText.setText("全部 "+pic.getCommentNum()+" 条评论");

                            ImageView posterProfileImg = header.findViewById(R.id.posterprofile);
                            Glide.with(PicInfoActivity.this).load(pic.getPoster().getProfileImgPath()).into(posterProfileImg);
                            posterProfileImg.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setClass(PicInfoActivity.this, UserInfoActivity.class);
                                    intent.putExtra("user", Parcels.wrap(User.class, currentPhoto.getPoster()));
                                    startActivity(intent);
                                }
                            });

                            SuperTextView description = header.findViewById(R.id.description);
                            description.setText(pic.getDescription());

                            ImageButton delPhotoButton = findViewById(R.id.delPhotoButton);
                            if (currentPhoto.getPoster().getAccount().equals(Constant.currentUser.getAccount())) {
                                delPhotoButton.setEnabled(true);
                            }
                            else {
                                delPhotoButton.setEnabled(false);
                                delPhotoButton.setVisibility(View.INVISIBLE);
                            }

                            ImageButton editDescriptionButton = findViewById(R.id.editDescriptionButton);
                            if (currentPhoto.getPoster().getAccount().equals(Constant.currentUser.getAccount())) {
                                editDescriptionButton.setEnabled(true);
                            }
                            else {
                                editDescriptionButton.setEnabled(false);
                                editDescriptionButton.setVisibility(View.INVISIBLE);
                            }

                            setRecyclerView(header);
                            setPostCommentListener(header);
                            setLikeStarListener(header);
                        }
                    });
                }
                else {
                    Looper.prepare();
                    AlertDialog.Builder builder = new AlertDialog.Builder(PicInfoActivity.this)
                            .setMessage("该照片可能已被删除！")
                            .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    PicInfoActivity.this.finish();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    Looper.loop();
                }
            }
        });
    }

    private void setRecyclerView(final View header) {
        mAdapter = new CommentAdapter(this, currentPhoto.getComments());
        mAdapter.setOnItemClickListener(new CommentAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 进入该用户的个人主页
                final Comment currentComment = mAdapter.getItem(position);
                Intent intent = new Intent();
                intent.setClass(PicInfoActivity.this, UserInfoActivity.class);
                intent.putExtra("user", Parcels.wrap(User.class, currentComment.getUser()));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.e("长按了", position+"_");
                final Comment currentComment = mAdapter.getItem(position);
                if (currentComment.getUser().getAccount().equals(Constant.currentUser.getAccount())) {
                    new XPopup.Builder(PicInfoActivity.this)
                            .atView(view)
                            .asAttachList(new String[]{"删除"}, null, new OnSelectListener() {
                                @Override
                                public void onSelect(int position, String text) {
                                    Log.e("点击了", text);
                                    RequestBody requestBody = new FormBody.Builder()
                                            .add("id",String.valueOf(currentComment.getId()))
                                            .build();
                                    HttpUtil.sendPostRequest(HttpUtil.BASEURL + "DelCommentServlet", requestBody, new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            Log.e("请求错误",e.getMessage()+"_");
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            String responseData = response.body().string();
                                            Log.e("删除返回的数据", responseData+"_");
                                            JSONObject jsonObject = JSON.parseObject(responseData);
                                            int responseNum = jsonObject.getInteger("Result");
                                            if (responseNum == LSC_OPERATION_SUCCESS) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ImageButton refreshCommentButton = header.findViewById(R.id.refreshCommentButton);
                                                        refreshCommentButton.performClick();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            })
                            .show();
                }
            }
        });
        /*LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);*/
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setPullRefreshEnabled(false);
        mRecyclerView.addHeaderView(header);
        ((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                // 没有刷新过，或者当前评论数为0，就不用加载更多了
                if (!hasRefreshed[0] || currentPhoto.getCommentNum() <= 10) {
                    Log.e("没刷新时正在加载更多","_");
                    mRecyclerView.loadMoreComplete();
                    return;
                }
                Log.e("正在加载更多评论","_");
                RequestBody requestBody = new FormBody.Builder()
                        .add("id",String.valueOf(currentPhoto.getImageID()))
                        .add("commentid", String.valueOf(currentPhoto.getLastComment().getId()))
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "LoadCommentServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求错误",e.getMessage()+"_");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        Log.e("加载返回的数据",responseData+"_");
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == LOAD_COMMENT_SUCCESS) {
                            JSONArray commentArray = jsonObject.getJSONArray("Comments");
                            final List<Comment> comments = new ArrayList<>();
                            for (int j = 0; j < commentArray.size(); j++) {
                                JSONObject commentInfo = commentArray.getJSONObject(j);
                                final Comment comment = new Comment();
                                int userID = commentInfo.getInteger("userID");
                                comment.setTime(Util.convertTimeToDateString(Long.parseLong(commentInfo.getString("time"))));
                                comment.setComment(commentInfo.getString("comment"));
                                comment.setUser(UserPool.addUser(userID, PicInfoActivity.this));
                                comment.setId(commentInfo.getInteger("id"));
                                comments.add(comment);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (comments.size() != 0) {
                                        currentPhoto.getComments().addAll(comments);
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

    private void setLikeStarListener(final View header) {
        ShineButton likeButton = header.findViewById(R.id.likebutton);
        likeButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                RequestBody requestBody = null;
                final int currentLikeNum = currentPhoto.getLikeNum();
                if (checked) {
                    requestBody = new FormBody.Builder()
                            .add("id", String.valueOf(currentPhoto.getImageID()))
                            .add("userId", String.valueOf(Constant.currentUser.getId()))
                            .add("like", String.valueOf(true))
                            .add("add", String.valueOf(true))
                            .build();
                    currentPhoto.setLikeNum(currentLikeNum+1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView likeNum = header.findViewById(R.id.likenum);
                            likeNum.setText(String.valueOf(currentLikeNum+1));
                        }
                    });
                }
                else {
                    requestBody = new FormBody.Builder()
                            .add("id", String.valueOf(currentPhoto.getImageID()))
                            .add("userId", String.valueOf(Constant.currentUser.getId()))
                            .add("like", String.valueOf(true))
                            .add("add", String.valueOf(false))
                            .build();
                    currentPhoto.setLikeNum(currentLikeNum-1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView likeNum = header.findViewById(R.id.likenum);
                            likeNum.setText(String.valueOf(currentLikeNum-1));
                        }
                    });
                }
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "SetLikeAndStarServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求错误", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
            }
        });

        ShineButton starButton = header.findViewById(R.id.starbutton);
        starButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                RequestBody requestBody = null;
                final int currentStarNum = currentPhoto.getStarNum();
                if (checked) {
                    requestBody = new FormBody.Builder()
                            .add("id", String.valueOf(currentPhoto.getImageID()))
                            .add("userId", String.valueOf(Constant.currentUser.getId()))
                            .add("like", String.valueOf(false))
                            .add("add", String.valueOf(true))
                            .build();
                    currentPhoto.setStarNum(currentStarNum+1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView likeNum = header.findViewById(R.id.starnum);
                            likeNum.setText(String.valueOf(currentStarNum+1));
                        }
                    });
                }
                else {
                    requestBody = new FormBody.Builder()
                            .add("id", String.valueOf(currentPhoto.getImageID()))
                            .add("userId", String.valueOf(Constant.currentUser.getId()))
                            .add("like", String.valueOf(false))
                            .add("add", String.valueOf(false))
                            .build();
                    currentPhoto.setStarNum(currentStarNum-1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView starNum = header.findViewById(R.id.starnum);
                            starNum.setText(String.valueOf(currentStarNum-1));
                        }
                    });
                }
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "SetLikeAndStarServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求错误", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                    }
                });
            }
        });
    }

    private void setPostCommentListener(final View header) {
        final TextFieldBoxes textFieldBoxes = findViewById(R.id.commentbox);
        textFieldBoxes.getEndIconImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ExtendedEditText commentInput = findViewById(R.id.commentInput);
                String commentText = commentInput.getText().toString();
                RequestBody commentBody = new FormBody.Builder()
                        .add("id", String.valueOf(currentPhoto.getImageID()))
                        .add("userId", String.valueOf(Constant.currentUser.getId()))
                        .add("text", commentText)
                        .build();
                HttpUtil.sendPostRequest(HttpUtil.BASEURL + "AddCommentServlet", commentBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("请求错误", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == LSC_OPERATION_SUCCESS) {
                            // 模拟点击back键，收起键盘（确实这个方法挺蠢的。。）
                            Runtime.getRuntime().exec("input keyevent " + KeyEvent.KEYCODE_BACK);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ImageButton refreshCommentButton = header.findViewById(R.id.refreshCommentButton);
                                    refreshCommentButton.performClick();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void onClick_refreshCommentButton(final View header) {
        Log.e("正在刷新","_");
        hasRefreshed[0] = true;
        RequestBody requestBody = new FormBody.Builder()
                .add("id",String.valueOf(currentPhoto.getImageID()))
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "RefreshLSCServlet", requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求错误",e.getMessage()+"_");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.e("刷新返回的数据",responseData+"_");
                JSONObject jsonObject = JSON.parseObject(responseData);
                int responseNum = jsonObject.getInteger("Result");
                if (responseNum == REFRESH_LSC_SUCCESS) {
                    Map<String, Integer> map = JSONObject.parseObject(jsonObject.getJSONObject("LSCNums").toJSONString(), new TypeReference<Map<String, Integer>>() {});
                    currentPhoto.setLikeNum(map.get("likenum"));
                    currentPhoto.setStarNum(map.get("starnum"));
                    currentPhoto.setCommentNum(map.get("commentnum"));
                    JSONArray commentArray = jsonObject.getJSONArray("Comments");
                    final List<Comment> comments = new ArrayList<>();
                    for (int j = 0; j < commentArray.size(); j++) {
                        JSONObject commentInfo = commentArray.getJSONObject(j);
                        Comment comment = new Comment();
                        int userID = commentInfo.getInteger("userID");
                        comment.setTime(Util.convertTimeToDateString(Long.parseLong(commentInfo.getString("time"))));
                        comment.setComment(commentInfo.getString("comment"));
                        User user = UserPool.addUser(userID, PicInfoActivity.this);
                        comment.setUser(user);
                        comment.setId(commentInfo.getInteger("id"));
                        comments.add(comment);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentPhoto.getComments().clear();
                            currentPhoto.getComments().addAll(comments);
                            mAdapter.notifyDataSetChanged();

                            TextView likeNum = header.findViewById(R.id.likenum);
                            likeNum.setText(String.valueOf(currentPhoto.getLikeNum()));
                            TextView starNum = header.findViewById(R.id.starnum);
                            starNum.setText(String.valueOf(currentPhoto.getStarNum()));
                            TextView commentNum = header.findViewById(R.id.commentnum);
                            commentNum.setText(String.valueOf(currentPhoto.getCommentNum()));
                            TextView commentNumText = header.findViewById(R.id.commentnumtext);
                            commentNumText.setText("全部 "+currentPhoto.getCommentNum()+" 条评论");
                        }
                    });
                }
            }
        });
    }

    public void onClick_delPhotoButton() {
        RequestBody requestBody = new FormBody.Builder()
                .add("id", String.valueOf(currentPhoto.getImageID()))
                .add("url", currentPhoto.getImageUrl())
                .build();
        HttpUtil.sendPostRequest(HttpUtil.BASEURL + "DelPhotoServlet", requestBody, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData = response.body().string();
                        JSONObject jsonObject = JSON.parseObject(responseData);
                        int responseNum = jsonObject.getInteger("Result");
                        if (responseNum == DEL_PIC_SUCCESS) {
                            Looper.prepare();
                            Toast.makeText(PicInfoActivity.this, "删除图片成功！", Toast.LENGTH_LONG).show();
                            PicInfoActivity.this.finish();
                            Looper.loop();
                        }
                    }
                });
    }

    public void onClick_editDescriptionButton(final View header) {
        View view = getLayoutInflater().inflate(R.layout.half_dialog_view, null);
        final EditText editText1 = view.findViewById(R.id.editUserInfo);
        editText1.setText(currentPhoto.getDescription());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("输入新的照片描述")//设置对话框的标题
                .setView(view)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newDescription = editText1.getText().toString();
                        changeImageDescriptionAsync(newDescription, header);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void changeImageDescriptionAsync(final String newDescription, final View header) {
        final LoadingDialog loadingDialog = new LoadingDialog.Builder(this).loadText("加载中...").build();
        loadingDialog.show();
        RequestBody requestBody = new FormBody.Builder()
                .add("imgId", String.valueOf(currentPhoto.getImageID()))
                .add("newDescription", newDescription)
                .build();
        String url = HttpUtil.BASEURL+"ChangePhotoDescriptionServlet";
        HttpUtil.sendPostRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("请求错误", e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Looper.prepare();
                String responseData = response.body().string();
                JSONObject jsonObject = JSON.parseObject(responseData);
                int resultNum = jsonObject.getInteger("Result");
                loadingDialog.dismiss();
                if (resultNum == Constant.CHANGE_PIC_DESCRIPTION_SUCCESS) {
                    Toast.makeText(PicInfoActivity.this, "修改照片描述成功！", Toast.LENGTH_LONG).show();
                    TextView textView = header.findViewById(R.id.description);
                    currentPhoto.setDescription(newDescription);
                    textView.setText(newDescription);
                }
                else {
                    Toast.makeText(PicInfoActivity.this, "修改照片描述失败！", Toast.LENGTH_LONG).show();
                }
                Looper.loop();
            }
        });
    }
}
