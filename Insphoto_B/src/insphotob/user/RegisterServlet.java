package insphotob.user;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "RegisterServlet", urlPatterns = "/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            //获得请求中传来的用户名和密码
            String account = request.getParameter("account").trim();
            String password = request.getParameter("password").trim();
            JSONObject jsonObject = new JSONObject();
            if (isExist(account)) {
                jsonObject.put("Result", "5");
            }
            else {
                UserDAO.register(account, password);
                jsonObject.put("Result", "4");
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private Boolean isExist(String account) {
        User user = UserDAO.queryUser(account);
        return user != null;
    }
}
