package insphotob.picture;

import insphotob.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PictureDAO {

    public static boolean addPhoto(int userId, String photoUrl, String description) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("insert into photoInfo(photourl, userid, description, likenum, starnum, commentnum) values (?,?,?,0,0,0)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, photoUrl);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, description);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static HashMap<Integer, String> queryRecentPhoto() {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet;
        sqlStatement.append("select id, photourl from photoInfo order by id desc limit 10");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.put(-1, "failed");
        return results;
    }

    public static boolean changeDescription(int imageId, String newDescription) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("update photoInfo set description=? where id=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, newDescription);
            preparedStatement.setInt(2, imageId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
    }

    public static HashMap<Integer, String> queryRecentUserPhoto(int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet;
        sqlStatement.append("select id, photourl from photoInfo WHERE userid=? order by id desc limit 10");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> refreshPhoto(int biggestID) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet;
        sqlStatement.append("select id, photourl from photoInfo where id>? and id<=?");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, biggestID);
            preparedStatement.setInt(2, biggestID+10);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> loadMorePhoto(int smallestID) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("select id, photourl from photoInfo where id<? and id>=?");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, smallestID);
            preparedStatement.setInt(2, smallestID-10);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> loadMoreUserPhoto(int smallestID, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("select id, photourl from photoInfo where id<? and userId=? ORDER BY id DESC LIMIT 10");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, smallestID);
            preparedStatement.setInt(2, userId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> loadMoreUserLSPhoto(int smallestID, int userId, boolean isLike) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        // sqlStatement.append("select id, photourl from photoInfo where id IN (SELECT photoid FROM ? WHERE photoid<? GROUP BY userid HAVING userid=?) ORDER BY id DESC LIMIT 10");
        HashMap<Integer, String> results = new HashMap<>();
        try {
            if (isLike) {
                preparedStatement = connection.prepareStatement("select id, photourl from photoInfo where id IN (SELECT photoid FROM likeInfo WHERE photoid<? AND userid=?) ORDER BY id DESC LIMIT 10");
            }
            else {
                preparedStatement = connection.prepareStatement("select id, photourl from photoInfo where id IN (SELECT photoid FROM starInfo WHERE photoid<? AND userid=?) ORDER BY id DESC LIMIT 10");
            }
            preparedStatement.setInt(1, smallestID);
            preparedStatement.setInt(2, userId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put(-1, "failed");
        return results;
    }

    public static Picture getPicInfo(int id, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("select userid,description,likenum,starnum from photoInfo where id=?");
        try {
            Picture picture = new Picture();
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int poster = resultSet.getInt("userid");
                String description = resultSet.getString("description");
                int likeNum = resultSet.getInt("likenum");
                int starNum = resultSet.getInt("starnum");
                picture.setLikeNum(likeNum);
                picture.setStarNum(starNum);
                picture.setDescription(description);
                picture.setPosterID(poster);
            }
            else {
                return null;
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select * from likeInfo where photoid=? and userid=?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                picture.setLike(true);
            }
            else {
                picture.setLike(false);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select * from starInfo where photoid=? and userid=?");
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                picture.setStar(true);
            }
            else {
                picture.setStar(false);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select commentid,userid,text,time from commentInfo where photoid=?");
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            picture.setComments(new ArrayList<>());
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setComment(resultSet.getString("text"));
                comment.setTime(resultSet.getString("time"));
                comment.setUserID(resultSet.getInt("userid"));
                comment.setId(resultSet.getInt("commentid"));
                picture.addComment(comment);
            }
            picture.setCommentNum(picture.getComments().size());
            return picture;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        return null;
    }

    public static boolean addLikeToPic(int photoId, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO likeInfo(photoid,userid) VALUES(?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE photoInfo SET likenum=likenum+1 WHERE id=?");
            preparedStatement.setInt(1, photoId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean addStarToPic(int photoId, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO starInfo(photoid,userid) VALUES(?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE photoInfo SET starnum=starnum+1 WHERE id=?");
            preparedStatement.setInt(1, photoId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean addCommentToPic(int photoId, int userId, String text, long time) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO commentInfo(photoid,userid,text,time) VALUES(?,?,?,?)");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, text);
            preparedStatement.setLong(4, time);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean delLike(int photoId, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("DELETE FROM likeInfo WHERE photoid=? and userid=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE photoInfo SET likenum=likenum-1 WHERE id=?");
            preparedStatement.setInt(1, photoId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean delStar(int photoId, int userId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("DELETE FROM starInfo WHERE photoid=? and userid=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE photoInfo SET starnum=starnum-1 WHERE id=?");
            preparedStatement.setInt(1, photoId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static boolean delComment(int commentId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("DELETE FROM commentInfo WHERE commentid=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, commentId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static HashMap<String, Integer> getLSCNum(int photoId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("select likenum,starnum FROM photoInfo WHERE id=?");
        HashMap<String, Integer> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int likeNum = resultSet.getInt("likenum");
                int starNum = resultSet.getInt("starnum");
                results.put("likenum", likeNum);
                results.put("starnum", starNum);
            }

            preparedStatement = connection.prepareStatement("select COUNT(commentid) FROM commentInfo GROUP BY photoid HAVING photoid=?");
            preparedStatement.setInt(1,photoId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int commentNum = resultSet.getInt("COUNT(commentid)");
                results.put("commentnum", commentNum);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put("failed", -1);
        return results;
    }

    public static List<Comment> getRecentComments(int photoId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("SELECT * FROM commentInfo WHERE photoid=? ORDER BY commentid ASC LIMIT 10");
        List<Comment> comments = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setComment(resultSet.getString("text"));
                comment.setTime(resultSet.getString("time"));
                comment.setUserID(resultSet.getInt("userid"));
                comment.setId(resultSet.getInt("commentid"));
                comments.add(comment);
            }
            return comments;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        comments.add(new Comment(-1));
        return comments;
    }

    public static List<Comment> loadMoreComments(int photoId, int commentId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("SELECT * FROM commentInfo WHERE photoid=? and commentid>? ORDER BY commentid ASC LIMIT 10");
        List<Comment> comments = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, photoId);
            preparedStatement.setInt(2, commentId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Comment comment = new Comment();
                comment.setComment(resultSet.getString("text"));
                comment.setTime(resultSet.getString("time"));
                comment.setUserID(resultSet.getInt("userid"));
                comment.setId(resultSet.getInt("commentid"));
                comments.add(comment);
            }
            return comments;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        comments.add(new Comment(-1));
        return comments;
    }

    public static boolean delPhoto(int picId) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("DELETE FROM photoInfo WHERE id=?");
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, picId);
            preparedStatement.executeUpdate();
            return true;
        }
        catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        return false;
    }

    public static HashMap<String, Integer> getUserLSSFNum(int userId, int mine) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet = null;
        sqlStatement.append("select COUNT(likeid) FROM likeInfo GROUP BY userid HAVING userid=?");
        HashMap<String, Integer> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int likeNum = resultSet.getInt("COUNT(likeid)");
                results.put("likenum", likeNum);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select COUNT(starid) FROM starInfo GROUP BY userid HAVING userid=?");
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int starNum = resultSet.getInt("COUNT(starid)");
                results.put("starnum", starNum);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select COUNT(subscribeid) FROM subscribeInfo GROUP BY fromid HAVING fromid=?");
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int subscribeNum = resultSet.getInt("COUNT(subscribeid)");
                results.put("subscribenum", subscribeNum);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select COUNT(subscribeid) FROM subscribeInfo GROUP BY toid HAVING toid=?");
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int followerNum = resultSet.getInt("COUNT(subscribeid)");
                results.put("followernum", followerNum);
            }

            resultSet = null;
            preparedStatement = connection.prepareStatement("select subscribeid FROM subscribeInfo WHERE fromid = ? AND toid = ?");
            preparedStatement.setInt(1, mine);
            preparedStatement.setInt(2, userId);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                results.put("hassubscribe", 1);
            }
            else {
                results.put("hassubscribe", 0);
            }

            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put("failed", -1);
        return results;
    }

    public static HashMap<Integer, String> queryRecentUserLSPhoto(int userId, boolean isLike) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        ResultSet resultSet;
        HashMap<Integer, String> results = new HashMap<>();
        try {
            if (isLike) {
                preparedStatement = connection.prepareStatement("select id, photourl from photoInfo WHERE id IN (SELECT photoid FROM likeInfo WHERE userid=?) order by id desc limit 10");
            }
            else {
                preparedStatement = connection.prepareStatement("select id, photourl from photoInfo WHERE id IN (SELECT photoid FROM starInfo WHERE userid=?) order by id desc limit 10");
            }
            preparedStatement.setInt(1, userId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String photoUrl = resultSet.getString("photoUrl");
                results.put(id, photoUrl);
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> searchPhoto(String keyword) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT id,photourl FROM photoInfo WHERE description LIKE ? ORDER BY id DESC LIMIT 10");
        ResultSet resultSet = null;
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, "%"+keyword+"%");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.put(resultSet.getInt("id"), resultSet.getString("photourl"));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put(-1, "failed");
        return results;
    }

    public static HashMap<Integer, String> loadMorePhotoResult(int smallestImgId, String keyword) {
        Connection connection = DBManager.conn();
        PreparedStatement preparedStatement = null;
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT id,photourl FROM photoInfo WHERE description LIKE ? AND id<? ORDER BY id DESC LIMIT 10");
        ResultSet resultSet = null;
        HashMap<Integer, String> results = new HashMap<>();
        try {
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, "%"+keyword+"%");
            preparedStatement.setInt(2, smallestImgId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.put(resultSet.getInt("id"), resultSet.getString("photourl"));
            }
            return results;
        } catch (SQLException ex) {
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }
        results.put(-1, "failed");
        return results;
    }
}
