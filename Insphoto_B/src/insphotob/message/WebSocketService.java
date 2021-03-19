package insphotob.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint("/chatService/{userId}")
public class WebSocketService {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    private static ConcurrentHashMap<Session, WebSocketService> ssMap = new ConcurrentHashMap<Session, WebSocketService>();
    private static ConcurrentHashMap<Integer, Session> idMap = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private int userId;

    @OnOpen
    public void onOpen(@PathParam("userId")String userId, Session session) {
        this.session = session;
        this.userId = Integer.parseInt(userId);
        ssMap.put(session, this);
        addOnlineCount();

        idMap.put(this.userId, session);
        List<Message> messages = MessageDAO.queryMessage(this.userId);
        JSONObject jsonObject = new JSONObject();
        if (messages.size() == 1 &&messages.get(0).getMessageID() == -1) {
            jsonObject.put("Result", "351");
        }
        else {
            jsonObject.put("Messages", messages);
            jsonObject.put("Result", "350");
        }

        WebSocketService tmp = ssMap.get(session);
        try {
            tmp.sendMessage(JSON.toJSONString(jsonObject));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @OnClose
    public void onClose(){
        ssMap.remove(this.session);
        idMap.remove(this.userId);
        subOnlineCount();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        JSONObject jsonObject = JSON.parseObject(message);
        Message receiveMessage = new Message();
        receiveMessage.setTime(jsonObject.getLong("createdAt"));
        receiveMessage.setText(jsonObject.getString("text"));
        receiveMessage.setSenderID(jsonObject.getInteger("senderID"));
        receiveMessage.setReceiverID(jsonObject.getInteger("receiverID"));
        // 对方在线，直接推给对方
        if (idMap.containsKey(receiveMessage.getReceiverID())) {
            WebSocketService tmp = ssMap.get(idMap.get(receiveMessage.getReceiverID()));
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put("Message", receiveMessage);
                messageObject.put("Result", "352");
                tmp.sendMessage(JSON.toJSONString(messageObject));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 对方离线，先存到数据库
        else {
            WebSocketService tmp = ssMap.get(session);
            MessageDAO.addMessage(receiveMessage);
            JSONObject messageObject = new JSONObject();
            messageObject.put("Result", "353");
            try {
                tmp.sendMessage(JSON.toJSONString(messageObject));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException{
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketService.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketService.onlineCount--;
    }
}

