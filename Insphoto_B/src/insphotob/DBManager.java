package insphotob;

import java.sql.*;
import java.util.logging.*;

public class DBManager {
    public static Connection conn() {
        String url = "jdbc:mysql://localhost:3306/myapp?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf8";
        String userName = "root";
        String userPwd = "123456";
        Connection con = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection(url, userName, userPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    //关闭连接，ResultSet是返回数据库的查询内容。
    public static void closeAll(Connection connection, PreparedStatement statement,
                                ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //没有ResultSet的是关闭修改、增加数据操作（因为不用返回结果）
    public static void closeAll(Connection connection, PreparedStatement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}