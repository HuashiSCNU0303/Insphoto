package insphotob.user;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.io.PrintWriter;
import net.sf.json.JSONObject;

@WebServlet(name = "LoginServlet", urlPatterns = "/LoginServlet")
public class LoginServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 设置响应内容类型
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            //获得请求中传来的用户名和密码
            String account = request.getParameter("account").trim();
            String password = request.getParameter("password").trim();
            //密码验证结果
            User user = verifyLogin(account, password);
            JSONObject jsonObject = new JSONObject();
            if (user == null) {
                jsonObject.put("Result", "3");
            }
            else if (user.isSuccessful()) {
                jsonObject.put("Result", "1"); // 登录成功，除了发消息以外，把这个用户的所有个人信息都发过去
                jsonObject.put("UserId", String.valueOf(user.getId()));
            }
            else {
                jsonObject.put("Result", "2");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    // 验证用户名密码是否正确
    private User verifyLogin(String userName, String password) {
        User user = UserDAO.queryUser(userName);
        if (user == null) {
            return null; // 没有这个账户
        }
        else if (password.equals(user.getPassword())) {
            user.setSuccessful(true);
            return user; // 登录成功
        }
        else {
            user.setSuccessful(false);
            return user; // 密码错误
        }
    }
}
