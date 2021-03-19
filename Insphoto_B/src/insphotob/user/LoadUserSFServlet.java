package insphotob.user;

import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "LoadUserSFServlet", urlPatterns = "/LoadUserSFServlet")
public class LoadUserSFServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter out = response.getWriter()) {
            int biggestUserId = Integer.parseInt(request.getParameter("biggestUserID").trim());
            int userId = Integer.parseInt(request.getParameter("userID").trim());
            String type = request.getParameter("type").trim();
            boolean isSubscribe = true;
            if ("follower".equals(type)) {
                isSubscribe = false;
            }
            JSONObject jsonObject = new JSONObject();
            List<Integer> results = UserDAO.loadMoreUserSF(biggestUserId, userId, isSubscribe);
            if (results.contains(-1)) {
                jsonObject.put("Result", "392");
            }
            else {
                jsonObject.put("Result", "391");
                jsonObject.put("UserIds", results);
            }
            out.write(jsonObject.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
