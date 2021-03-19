package com.e.insphoto.entities;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import org.litepal.annotation.Encrypt;
import org.litepal.crud.LitePalSupport;
import org.parceler.Parcel;

import java.util.Date;

@Parcel
public class Message extends LitePalSupport implements IMessage {
    private int id_int, senderID, receiverID;

    @Encrypt(algorithm = AES)
    private String text;
    private Date createdAt;

    public Message() {

    }
    public Message(String text, Date postTime, int senderID, int receiverID) {
        this.text = text;
        this.createdAt = postTime;
        this.senderID = senderID;
        this.receiverID = receiverID;
    }

    @Override
    public String getId() {
        return String.valueOf(id_int);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return UserPool.getUser(senderID);
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public int getId_int() {
        return id_int;
    }

    public void setId_int(int id_int) {
        this.id_int = id_int;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date postTime) {
        this.createdAt = postTime;
    }

    public int getSenderID() {
        return this.senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return this.receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }
}
