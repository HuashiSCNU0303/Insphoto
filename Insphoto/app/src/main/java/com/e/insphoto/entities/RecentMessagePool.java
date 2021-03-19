package com.e.insphoto.entities;

import android.util.Log;

import com.e.insphoto.utils.Constant;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecentMessagePool {
    private static HashMap<Integer, RecentMessage> recentMessages = new HashMap<>();
    public static List<RecentMessage> modifiedMessages = new ArrayList<>();

    public static boolean addRecentMessage(Message message, boolean isFromChat) {
        if (message == null) {
            Log.e("RecentMessagePool", "没有更新！");
            return false;
        }
        int userId = -1;
        if (message.getSenderID() == Constant.currentUser.getID()) {
            // 消息是自己发的
            userId = message.getReceiverID();
        }
        else {
            // 消息是别人发的
            userId = message.getSenderID();
        }
        if (recentMessages.containsKey(userId)) {
            modifyRecentMessage(userId, message, isFromChat);
            return false;
        }
        else {
            Log.e("RecentMessagePool", "新增了一个！");
            RecentMessage recentMessage = new RecentMessage(UserPool.getUser(userId));
            recentMessage.setUnreadCount(isFromChat? 0: 1);
            recentMessage.setLastMessage(message);
            Log.e("3", recentMessage.getId());
            recentMessages.put(userId, recentMessage);
            modifiedMessages.add(recentMessage);
            return true;
        }
    }

    public static void modifyRecentMessage(int userId, Message message, boolean isFromChat) {
        Log.e("RecentMessagePool", "已添加！");
        RecentMessage recentMessage = recentMessages.get(userId);
        recentMessage.setLastMessage(message);
        recentMessage.setUnreadCount(isFromChat? 0: recentMessage.getUnreadCount() + 1);
        modifiedMessages.add(recentMessage);
    }

    public static void clear() {
        modifiedMessages.clear();
        recentMessages.clear();
    }
}
