package insphotob.message;

import insphotob.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageDAO {
    public static boolean addMessage(Message message) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("insert into messageInfo(senderid, receiverid, text, time) values (?,?,?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, message.getSenderID());
            preparedStatement.setInt(2, message.getReceiverID());
            preparedStatement.setString(3, message.getText());
            preparedStatement.setLong(4, message.getTime());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static List<Message> queryMessage(int receiverID) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet;
        sqlStatement.append("SELECT * FROM messageInfo WHERE receiverid=?");
        List<Message> results = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, receiverID);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Message message = new Message();
                message.setMessageID(resultSet.getInt("messageid"));
                message.setReceiverID(receiverID);
                message.setSenderID(resultSet.getInt("senderid"));
                message.setText(resultSet.getString("text"));
                message.setTime(resultSet.getLong("time"));
                results.add(message);
            }
            // 查询到之后就删除
            if (results.size() > 0) {
                preparedStatement = connection.prepareStatement("DELETE FROM messageInfo WHERE receiverid=?");
                preparedStatement.setInt(1, receiverID);
                preparedStatement.executeUpdate();
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        Message message = new Message();
        message.setMessageID(-1);
        results.add(message);
        return results;
    }
}
