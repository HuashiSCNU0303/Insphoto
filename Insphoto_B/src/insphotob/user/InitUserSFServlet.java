package insphotob.user;

import insphotob.picture.PictureDAO;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

@WebServlet(name = "InitUserSFServlet", urlPatterns = "/InitUserSFServlet")
public class InitUserSFServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int userId = Integer.parseInt(request.getParameter("userId").trim());
            String type = request.getParameter("type").trim();
            boolean isSubscribe = true;
            if ("follower".equals(type)) {
                isSubscribe = false;
            }
            List<Integer> results = UserDAO.queryRecentUserSF(userId, isSubscribe);
            JSONObject jsonObject = new JSONObject();
            if (results.contains(-1)) {
                jsonObject.put("Result", "390");
            }
            else {
                jsonObject.put("Result", "389");
                jsonObject.put("UserIds", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
