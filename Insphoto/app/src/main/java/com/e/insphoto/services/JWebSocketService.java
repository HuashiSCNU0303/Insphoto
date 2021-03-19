package com.e.insphoto.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.e.insphoto.R;
import com.e.insphoto.entities.Message;
import com.e.insphoto.entities.RecentMessagePool;
import com.e.insphoto.entities.UserPool;
import com.e.insphoto.utils.Constant;
import com.e.insphoto.utils.JWebSocketClient;
import com.e.insphoto.utils.Util;

import org.java_websocket.handshake.ServerHandshake;
import org.litepal.LitePal;
import org.parceler.Parcels;

import java.net.URI;
import java.util.List;

public class JWebSocketService extends Service {

    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;

    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public JWebSocketService getService() {
            return JWebSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 初始化websocket
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);//开启心跳检测

        // 设置前台服务，会出bug，先留着
        /*if (Build.VERSION.SDK_INT < 25) {
            //Android4.3 - Android7.0，隐藏Notification上的图标
            Intent innerIntent = new Intent(this, GrayInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }
        else {
            //Android7.0以上app启动后通知栏会出现一条"正在运行"的通知
            startForeground(GRAY_SERVICE_ID, new Notification());
        }*/

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        closeConnect();
        super.onDestroy();
    }

    public JWebSocketService() {
    }

    private void initSocketClient() {
        URI uri = URI.create(Constant.WS_URL+Constant.currentUser.getID());
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                Log.e("JWebSocketClientService", "收到的消息：" + message);
                JSONObject jsonObject = JSON.parseObject(message);
                int responseNum = jsonObject.getInteger("Result");
                switch (responseNum) {
                    case Constant.GET_PREVIOUS_MESSAGE_SUCCESS: {

                        List<Message> messages_ = LitePal.where("senderID = ? or receiverID = ?", Constant.currentUser.getId(), Constant.currentUser.getId()).find(Message.class);
                        for (Message message_ : messages_) {
                            UserPool.addUser(message_.getSenderID(), getApplicationContext());
                            UserPool.addUser(message_.getReceiverID(), getApplicationContext());
                            RecentMessagePool.addRecentMessage(message_, true);
                        }

                        JSONArray messageArray = jsonObject.getJSONArray("Messages");
                        for (int j = 0; j < messageArray.size(); j++) {
                            JSONObject messageObject = messageArray.getJSONObject(j);
                            Message mess = new Message();
                            mess.setText(messageObject.getString("text"));
                            mess.setCreatedAt(Util.convertTimeToDate(messageObject.getLong("time")));
                            mess.setSenderID(messageObject.getInteger("senderID"));
                            mess.setReceiverID(messageObject.getInteger("receiverID"));
                            UserPool.addUser(mess.getSenderID(), getApplicationContext());
                            Log.e("JWebSocketService", "加载了一条消息："+mess.getText());
                            mess.setId_int(-1);
                            if (LitePal.where("createdAt = ?", String.valueOf(mess.getCreatedAt().getTime())).find(Message.class).size() == 0) {
                                mess.save();
                            }
                            RecentMessagePool.addRecentMessage(mess, false);
                        }

                        break;
                    }
                    case Constant.GET_PREVIOUS_MESSAGE_FAILURE: {
                        break;
                    }
                    case Constant.RECEIVE_MESSAGE: {
                        Message receivedMessage = new Message();
                        jsonObject = jsonObject.getJSONObject("Message");
                        receivedMessage.setCreatedAt(Util.convertTimeToDate(jsonObject.getLong("time")));
                        receivedMessage.setText(jsonObject.getString("text"));
                        receivedMessage.setReceiverID(jsonObject.getInteger("receiverID"));
                        receivedMessage.setSenderID(jsonObject.getInteger("senderID"));
                        receivedMessage.setId_int(-1);
                        Log.e("JWebSocketService", "消息初始化已完成");

                        UserPool.addUser(receivedMessage.getSenderID(), getApplicationContext());

                        if (Constant.currentChatUserID == receivedMessage.getSenderID()) {
                            Log.e("JWebSocketService", "已通知消息列表更新");
                            Intent intent = new Intent();
                            intent.setAction("UPDATE_MESSAGE_LIST");
                            intent.putExtra("message", Parcels.wrap(Message.class, receivedMessage));
                            sendBroadcast(intent);
                        }
                        else {
                            if (LitePal.where("createdAt = ?", String.valueOf(receivedMessage.getCreatedAt().getTime())).find(Message.class).size() == 0) {
                                receivedMessage.save();
                            }
                            Log.e("JWebSocketService", "准备推送");

                            String id = "channel_001";
                            String name = "name";
                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            Notification notification = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
                                notificationManager.createNotificationChannel(mChannel);
                                notification = new Notification.Builder(getApplicationContext())
                                        .setChannelId(id)
                                        .setContentTitle("新消息")
                                        .setContentText("有新消息等待查看")
                                        .setSmallIcon(R.drawable.ic_launcher_round)
                                        .setAutoCancel(true)
                                        .build();
                            }
                            notificationManager.notify(1, notification);
                            Log.e("JWebSocketService", "推送完成");
                            RecentMessagePool.addRecentMessage(receivedMessage, false);
                        }
                        break;
                    }
                    case Constant.RECEIVER_OFFLINE: {
                        break;
                    }
                }
            }

            @Override
            public void onOpen(ServerHandshake handShakeData) {
                super.onOpen(handShakeData);
                Log.e("JWebSocketClientService", "websocket连接成功");
            }
        };
        connect();
    }

    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void sendMsg(String msg) {
        if (null != client) {
            Log.e("JWebSocketClientService", "发送的消息：" + msg);
            client.send(msg);
        }
    }

    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
            mHandler.removeCallbacks(heartBeatRunnable);
        }
    }


    //    -------------------------------------websocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000; //每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }
            }
            else {
                //如果client已为空，重新初始化连接
                client = null;
                initSocketClient();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("JWebSocketClientService", "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
