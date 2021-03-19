package insphotob.user;

import insphotob.picture.Comment;
import insphotob.DBManager;
import insphotob.picture.Picture;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.*;

public class UserDAO {
    public static User queryUser(String account) {
        //连接数据库
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        //SQL查询语句
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("select * from userInfo where account=?");
        //设置数据库的字段值
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, account);
            resultSet = preparedStatement.executeQuery();
            User user = new User();
            if (resultSet.next()) {
                user.setAccount(resultSet.getString("account"));
                user.setPassword(resultSet.getString("password"));
                user.setName(resultSet.getString("name"));
                user.setProfileImgUrl(resultSet.getString("profileimgUrl"));
                user.setId(resultSet.getInt("id"));
                return user;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
    }

    public static void register(String account, String password) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("insert into userInfo(account,password,name,profileimgurl,profile) values (?,?,?,?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, account);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, account);
            preparedStatement.setString(4, User.defaultProfileImgUrl); // 放默认头像图片的url地址
            preparedStatement.setString(5, "");
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static boolean changeName(int userId, String newName) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("update userInfo set name=? where id=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, newName);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static boolean changeDescription(int userId, String newDescription) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("update userInfo set profile=? where id=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, newDescription);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static boolean changeProfileImg(int userId, String newUrl) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("update userInfo set profileimgurl=? where id=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, newUrl);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static HashMap<String, String> getUserInfo(int userId) {
        //连接数据库
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        //SQL查询语句
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("select * from userInfo where id=?");
        //设置数据库的字段值
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            HashMap<String, String> userInfo = new HashMap<>();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String profileImgUrl = resultSet.getString("profileimgUrl");
                String account = resultSet.getString("account");
                String description = resultSet.getString("profile");
                userInfo.put("name",name);
                userInfo.put("profileimgurl",profileImgUrl);
                userInfo.put("account", account);
                userInfo.put("description", description);
                return userInfo;
            } else {
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
    }

    public static List<Integer> searchUser(String keyword) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT id FROM userInfo WHERE name LIKE ? ORDER BY id ASC LIMIT 20");
        ResultSet resultSet = null;
        List<Integer> results = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, "%"+keyword+"%");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getInt("id"));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.add(-1);
        return results;
    }

    public static List<Integer> loadMoreUserResult(int biggestUserId, String keyword) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT id FROM userInfo WHERE name LIKE ? AND id>? ORDER BY id ASC LIMIT 10");
        ResultSet resultSet = null;
        List<Integer> results = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, "%"+keyword+"%");
            preparedStatement.setInt(2, biggestUserId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getInt("id"));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.add(-1);
        return results;
    }

    public static boolean addSubscribe(int fromId, int toId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO subscribeInfo(fromid,toid) VALUES(?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, fromId);
            preparedStatement.setInt(2, toId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean delSubscribe(int fromId, int toId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("DELETE FROM subscribeInfo WHERE fromid=? and toid=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, fromId);
            preparedStatement.setInt(2, toId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static List<Integer> queryRecentUserSF(int userId, boolean isSubscribe) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        List<Integer> results = new ArrayList<>();
        try {
            if (isSubscribe) {
                preparedStatement = connection.prepareStatement("SELECT * FROM subscribeInfo WHERE fromid = ? ORDER BY subscribeid ASC LIMIT 20");
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT * FROM subscribeInfo WHERE toid = ? ORDER BY subscribeid ASC LIMIT 20");
            }
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            List<Integer> subscribeIds = new ArrayList<>();
            while (resultSet.next()) {
                results.add(resultSet.getInt(isSubscribe? "toid": "fromid"));
                subscribeIds.add(resultSet.getInt("subscribeid"));
            }
            if (results.size() > 0) {
                subscribeIds.sort(((o1, o2) -> {
                    if (o1 > o2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }));
                results.add(subscribeIds.get(0));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.add(-1);
        return results;
    }

    public static List<Integer> loadMoreUserSF(int biggestUserId, int userId, boolean isSubscribe) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        List<Integer> results = new ArrayList<>();
        try {
            if (isSubscribe) {
                preparedStatement = connection.prepareStatement("SELECT * FROM subscribeInfo WHERE fromid = ? AND subscribeid > ? ORDER BY subscribeid ASC LIMIT 20");
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT * FROM subscribeInfo WHERE toid = ? AND subscribeid > ? ORDER BY subscribeid ASC LIMIT 20");
            }
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, biggestUserId);
            resultSet = preparedStatement.executeQuery();
            List<Integer> subscribeIds = new ArrayList<>();
            while (resultSet.next()) {
                results.add(resultSet.getInt(isSubscribe? "toid": "fromid"));
                subscribeIds.add(resultSet.getInt("subscribeid"));
            }
            if (results.size() > 0) {
                subscribeIds.sort(((o1, o2) -> {
                    if (o1 > o2) {
                        return -1;
                    } else {
                        return 1;
                    }
                }));
                results.add(subscribeIds.get(0));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.add(-1);
        return results;
    }
}

