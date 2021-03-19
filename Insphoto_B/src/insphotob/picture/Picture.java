package insphotob.picture;

import java.util.List;

public class Picture {
    private int imageID, likeNum, starNum, commentNum, posterID;
    private String posterAccount, posterName, posterProfileImg, description;
    private List<Comment> comments;
    private boolean isLike, isStar;

    public Picture() {

    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }

    public int getStarNum() {
        return starNum;
    }

    public void setStarNum(int starNum) {
        this.starNum = starNum;
    }

    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getPosterProfileImg() {
        return posterProfileImg;
    }

    public void setPosterProfileImg(String posterProfileImg) {
        this.posterProfileImg = posterProfileImg;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }

    public String getPosterAccount() {
        return posterAccount;
    }

    public void setPosterAccount(String posterAccount) {
        this.posterAccount = posterAccount;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public int getPosterID() {
        return posterID;
    }

    public void setPosterID(int posterID) {
        this.posterID = posterID;
    }
}
