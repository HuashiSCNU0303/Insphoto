package com.e.insphoto.entities;

import com.stfalcon.chatkit.commons.models.IUser;

import org.parceler.Parcel;

import java.util.Objects;

@Parcel
public class User implements IUser {
    private int ID; // 用户id
    private String account; // 账号
    private String profileName; // 昵称
    private String profileImgUrl; // 头像网络Url（相对地址）
    private String profileImgPath; // 头像本地地址（在加入用户池以后就下载）
    private String description; // 个性签名

    public User() {

    }

    public User(String account, String profileName, String profileImgUrl) {
        this.account = account;
        this.profileName = profileName;
        this.profileImgUrl = profileImgUrl;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getProfileImgPath() {
        return profileImgPath;
    }

    public void setProfileImgPath(String profileImgPath) {
        this.profileImgPath = profileImgPath;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getId() {
        return String.valueOf(ID);
    }

    @Override
    public String getName() {
        return profileName;
    }

    @Override
    public String getAvatar() {
        return profileImgPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return getAccount().equals(user.getAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccount());
    }
}