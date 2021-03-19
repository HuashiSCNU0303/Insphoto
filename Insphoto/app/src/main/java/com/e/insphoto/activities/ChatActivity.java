package com.e.insphoto.activities;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.e.insphoto.R;
import com.e.insphoto.entities.Message;
import com.e.insphoto.entities.RecentMessagePool;
import com.e.insphoto.entities.User;
import com.e.insphoto.services.JWebSocketService;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.JWebSocketClient;
import com.e.insphoto.utils.Util;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.litepal.LitePal;
import org.parceler.Parcels;

import java.util.List;


public class ChatActivity extends Activity implements MessagesListAdapter.SelectionListener, MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener
{
    private User sender, receiver;

    protected final String senderId = String.valueOf(Constant.currentUser.getID());
    protected MessagesListAdapter<Message> messagesAdapter;
    private MessagesList messagesList;

    private JWebSocketClient client;
    private JWebSocketService.JWebSocketClientBinder binder;
    private JWebSocketService jWebSClientService;
    private ChatMessageReceiver chatMessageReceiver;

    private Message latestMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiver = Parcels.unwrap(getIntent().getParcelableExtra("receiver"));
        sender = Constant.currentUser;
        Constant.currentChatUserID = receiver.getID();

        TextView header = findViewById(R.id.chatHeader);
        header.setText(receiver.getProfileName());

        messagesList = findViewById(R.id.messagesList);
        initAdapter();

        MessageInput input = findViewById(R.id.input);
        input.setInputListener(this);

        // 绑定服务
        Intent bindIntent = new Intent(this, JWebSocketService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        // 注册广播
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("UPDATE_MESSAGE_LIST");
        registerReceiver(chatMessageReceiver, filter);

        // 直接从本地数据库中拿聊天记录
        List<Message> messages = LitePal.where("senderID in (?, ?) and receiverID in (?, ?)", sender.getId(), receiver.getId(), sender.getId(), receiver.getId()).find(Message.class);
        Log.e("ChatActivity", "从本地数据库中取聊天记录：" + messages.size());
        if (messages.size() > 0) {
            for (Message message : messages) {
                messagesAdapter.addToStart(message, true);
            }
            RecentMessagePool.addRecentMessage(messages.get(messages.size()-1), true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // receiver解除绑定
        unregisterReceiver(chatMessageReceiver);
        unbindService(serviceConnection);
        Constant.currentChatUserID = -1;
    }

    private void initAdapter() {
        messagesAdapter = new MessagesListAdapter<>(senderId, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Glide.with(ChatActivity.this).load(url).apply(RequestOptions.bitmapTransform(new RoundedCorners(10))).into(imageView);
            }
        });
        messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(messagesAdapter);
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public void onSelectionChanged(int count) {

    }

    @Override
    public boolean onSubmit(CharSequence input) {
        long postTime = System.currentTimeMillis();
        Message message = new Message();
        message.setId_int(2);
        message.setSenderID(sender.getID());
        message.setText(input.toString());
        message.setCreatedAt(Util.convertTimeToDate(postTime));
        message.setReceiverID(receiver.getID());
        if (client != null && client.isOpen()) {
            String jsonString = JSON.toJSONString(message);
            jWebSClientService.sendMsg(jsonString);
        }

        messagesAdapter.addToStart(message, true);
        if (message.save()) {
            Log.e("ChatActivity", "聊天记录保存本地数据库成功！");
        }
        RecentMessagePool.addRecentMessage(message, true);
        return true;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("ChatActivity", "服务与活动成功绑定");
            binder = (JWebSocketService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("ChatActivity", "服务与活动成功断开");
        }
    };

    private class ChatMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message message = Parcels.unwrap(intent.getParcelableExtra("message"));
            messagesAdapter.addToStart(message, true);
            RecentMessagePool.addRecentMessage(message, true);
            message.save();
        }
    }
}