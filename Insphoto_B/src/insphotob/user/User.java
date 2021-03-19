package insphotob.user;

public class User {
    private int id;
    private String password; // 密码
    private String account; // 账户名
    private String name; // 昵称
    private String profileImgUrl; // 头像Url，到时候直接用Glide下载
    private boolean isSuccessful; // 判断密码是否正确
    public static final String defaultProfileImgUrl = "defaultprofile.jpg"; // 默认头像Url

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
