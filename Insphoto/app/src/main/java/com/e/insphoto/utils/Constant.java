package com.e.insphoto.utils;

import com.e.insphoto.entities.User;

public class Constant {
    public static User currentUser;
    public static int currentChatUserID = -1;

    // 即时聊天服务器URL
    public static final String WS_URL = "ws://47.105.203.231:8080/Insphoto/chatService/";

    public static final int GET_PREVIOUS_MESSAGE_SUCCESS = 350;
    public static final int GET_PREVIOUS_MESSAGE_FAILURE = 351;
    public static final int RECEIVE_MESSAGE = 352;
    public static final int RECEIVER_OFFLINE = 353;

    public static final int CHANGE_DESCRIPTION_SUCCESS = 385;
    public static final int CHANGE_DESCRIPTION_FAILURE = 386;

    public static final int CHANGE_PIC_DESCRIPTION_SUCCESS = 387;
    public static final int CHANGE_PIC_DESCRIPTION_FAILURE = 388;

    public static final int INIT_USER_SF_SUCCESS = 389;
    public static final int INIT_USER_SF_FAILURE = 390;
    public static final int LOADMORE_USER_SF_SUCCESS = 391;
    public static final int LOADMORE_USER_SF_FAILURE = 392;
    public static final int SUBSCRIBE_OPERATION_SUCCESS = 393;
    public static final int SUBSCRIBE_OPERATION_FAILURE = 394;
}
