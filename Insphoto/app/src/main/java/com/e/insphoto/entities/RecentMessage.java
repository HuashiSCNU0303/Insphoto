package com.e.insphoto.entities;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

public class RecentMessage implements IDialog {
    private List<User> users; // 发消息去的那个
    private Message lastMessage;
    private int unreadCount;
    public RecentMessage(User user) {
        users = new ArrayList<>();
        users.add(user);
    }
    @Override
    public String getId() {
        return users.get(0).getId();
    }

    @Override
    public String getDialogPhoto() {
        return users.get(0).getAvatar();
    }

    @Override
    public String getDialogName() {
        return users.get(0).getName();
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = (Message) message;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
